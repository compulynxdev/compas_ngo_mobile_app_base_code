package com.compastbc.ui.beneficiary.enroll_beneficiary;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.ui.beneficiary.BenfAuthEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnrollBeneficiaryPresenter<V extends EnrollBeneficiaryMvpView> extends BasePresenter<V>
        implements EnrollBeneficiaryMvpPresenter<V> {

    private final Context context;
    private Beneficiary beneficiary;

    EnrollBeneficiaryPresenter(Context context, DataManager dataManager) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void searchBeneficiary(String searchCriteria, String inputText) {
        if (searchCriteria != null && inputText != null && !searchCriteria.isEmpty() && !inputText.isEmpty()) {
            if (getDataManager().getConfigurableParameterDetail().isOnline()) {
                if (getMvpView().isNetworkConnected()) {
                    getMvpView().showLoading();
                    if (searchCriteria.equalsIgnoreCase("rationNo"))
                        searchBeneficiaryByIdNo(inputText);
                    else
                        searchBeneficiaryByCardNo(inputText);
                }
            } else {
                beneficiary = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(searchCriteria.equalsIgnoreCase("rationNo") ? BeneficiaryDao.Properties.IdentityNo.eq(inputText) : BeneficiaryDao.Properties.CardNumber.eq(inputText)).unique();

                if (beneficiary != null) {
                    if (beneficiary.getBio()) {
                        getMvpView().showMessage(R.string.BiometricAlreadyCaptured);
                    } else {
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("identityNo", beneficiary.getIdentityNo());
                            jsonObject.put("memberId", beneficiary.getMemberNumber());
                            jsonObject.put("firstName", beneficiary.getFirstName());
                            jsonObject.put("dateOfBirth", beneficiary.getDateOfBirth());
                            jsonObject.put("cardNumber", beneficiary.getCardNumber());
                            jsonObject.put("address", beneficiary.getAddress());
                            jsonObject.put("gender", beneficiary.getGender());
                            jsonObject.put("memberId", "");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        getMvpView().showBeneficiaryDetails(jsonObject);
                    }
                } else {
                    getMvpView().showMessage(searchCriteria.equalsIgnoreCase("rationNo") ? R.string.idNotExist : R.string.cardNotExist);
                }
            }
        } else if (searchCriteria == null || searchCriteria.isEmpty())
            getMvpView().showMessage(R.string.PleaseSelectCriteria);

        else getMvpView().showMessage(R.string.PleaseEnterInput);

    }

    @Override
    public void searchBeneficiaryByIdNo(String input) {
        Map<String, String> map = new HashMap<>();
        map.put("identityNo", input);
        map.put("locationId",getDataManager().getUserDetail().getLocationId());

        if (getMvpView().isNetworkConnected()) {
            getDataManager().getBeneficiaryByIdno("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    getMvpView().hideLoading();
                    try {
                        if (response.code() == 200) {
                            assert response.body() != null;
                            JSONObject object = new JSONObject(response.body().string());
                            if (!object.getBoolean("bioStatus"))
                                getMvpView().showBeneficiaryDetails(object);

                            else getMvpView().showMessage(R.string.BiometricAlreadyCaptured);

                        } else if (response.code() == 401)
                            getMvpView().openActivityOnTokenExpire();

                        else {
                            assert response.errorBody() != null;
                            JSONObject object;
                            object = new JSONObject(response.errorBody().string());
                            getMvpView().showMessage(object.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        getMvpView().showMessage(e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    handleApiFailure(call, t);
                }
            });

        }
    }

    @Override
    public void searchBeneficiaryByCardNo(String input) {
        getMvpView().showLoading();
        Map<String, String> map = new HashMap<>();
        map.put("cardNo", input);
        map.put("locationId",getDataManager().getUserDetail().getLocationId());
        if (getMvpView().isNetworkConnected()) {
            getDataManager().getBeneficiaryByCardno("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    getMvpView().hideLoading();
                    try {
                        if (response.code() == 200) {
                            assert response.body() != null;
                            JSONObject object = new JSONObject(response.body().string());
                            if (!object.getBoolean("bioStatus"))
                                getMvpView().showBeneficiaryDetails(object);

                            else getMvpView().showMessage(R.string.BiometricAlreadyCaptured);

                        } else if (response.code() == 401) {
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            assert response.errorBody() != null;
                            JSONObject object;
                            object = new JSONObject(response.errorBody().string());
                            getMvpView().showMessage(object.getString("message"));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        getMvpView().showMessage(e.getMessage());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    handleApiFailure(call, t);
                }
            });

        } else {
            getMvpView().hideLoading();
            getMvpView().showMessage(R.string.noInternet);
        }
    }

    @Override
    public void saveBeneficiaryBiometric(String idno, String memberId, Bitmap bitmap, MemberInfo memberInfo) {
        if (bitmap != null && memberInfo != null && idno != null) {
            if (getDataManager().getConfigurableParameterDetail().isOnline()) {
                getMvpView().showLoading();
                JSONObject object = new JSONObject();
                try {
                    object.put("idPassPortNo", idno);
                    object.put("createdBy", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    object.put("deviceId", getDataManager().getDeviceId());
                    object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    object.put("memberId", Integer.parseInt(memberId));
                    object.put("benfImage", bitmap.toString());
                    object.put("bioStatus", true);
                    JSONObject fingerprint = new JSONObject();
                    fingerprint.put("leftFinger1", memberInfo.getLeftFront());
                    fingerprint.put("leftFinger2", memberInfo.getLeftOne());
                    fingerprint.put("leftFinger3", memberInfo.getLeftTwo());
                    fingerprint.put("rightFinger1", memberInfo.getRightFront());
                    fingerprint.put("rightFinger2", memberInfo.getRightOne());
                    fingerprint.put("rightFinger3", memberInfo.getRightTwo());
                    fingerprint.put("leftIndex", memberInfo.getLeftIndex());
                    fingerprint.put("leftThumb", memberInfo.getLeftThumb());
                    fingerprint.put("rightIndex", memberInfo.getRightIndex());
                    fingerprint.put("rightThumb", memberInfo.getRightThumb());
                    object.put("fingerPrints", fingerprint);
                    getMvpView().hideLoading();
                    if (fingerprint.length() >= getDataManager().getConfigurableParameterDetail().getMinimumFinger())
                        UploadBeneficiaryBiometric(object);

                    else
                        getMvpView().showMessage(getDataManager().getConfigurableParameterDetail().getMinimumFinger() + " " + context.getString(R.string.MinimumFingerError));

                } catch (JSONException e) {
                    getMvpView().hideLoading();
                    e.printStackTrace();
                }
            } else {
                if (getDataManager().getConfigurableParameterDetail().getFingerPrintActive()) {
                    if (AppConstants.fpcount >= getDataManager().getConfigurableParameterDetail().getMinimumFinger()) {
                        BeneficiaryBio beneficiaryBioBean = new BeneficiaryBio();
                        //the beneficiary should give minimum four fingerprints
                        if (memberInfo.getLeftThumb() != null) {
                            beneficiaryBioBean.setFplt(memberInfo.getLeftThumb());
                        }
                        if (memberInfo.getLeftFront() != null) {
                            beneficiaryBioBean.setFplf(memberInfo.getLeftFront());
                        }
                        if (memberInfo.getLeftOne() != null) {
                            beneficiaryBioBean.setF1(memberInfo.getLeftOne());
                        }
                        if (memberInfo.getLeftTwo() != null) {
                            beneficiaryBioBean.setF2(memberInfo.getLeftTwo());
                        }
                        if (memberInfo.getLeftIndex() != null) {
                            beneficiaryBioBean.setFpli(memberInfo.getLeftIndex());
                        }

                        if (memberInfo.getRightThumb() != null) {
                            beneficiaryBioBean.setFprt(memberInfo.getRightThumb());
                        }

                        if (memberInfo.getRightFront() != null) {
                            beneficiaryBioBean.setFprf(memberInfo.getRightFront());
                        }

                        if (memberInfo.getRightOne() != null) {
                            beneficiaryBioBean.setF3(memberInfo.getRightOne());
                        }

                        if (memberInfo.getRightTwo() != null) {
                            beneficiaryBioBean.setF4(memberInfo.getRightTwo());
                        }

                        if (memberInfo.getRightIndex() != null) {
                            beneficiaryBioBean.setFpri(memberInfo.getRightIndex());
                        }

                        beneficiaryBioBean.setBeneficiaryId(beneficiary.getIdentityNo());

                        getDataManager().getDaoSession().getBeneficiaryBioDao().save(beneficiaryBioBean);
                        beneficiary.setIsUploaded("0");
                        beneficiary.setImage(bitmap.toString());
                        beneficiary.setBio(true);
                        beneficiary.setBioVerifyStatus(String.valueOf(BenfAuthEnum.PENDING));
                        beneficiary.setDeviceId(CommonUtils.getDeviceId(context));
                        beneficiary.setAgentId(getDataManager().getUserDetail().getAgentId());
                        getDataManager().getDaoSession().getBeneficiaryDao().update(beneficiary);

                        getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.successBiometricCapture).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            getMvpView().createLog("Enroll Beneficiary", "Bio Captured");
                            getMvpView().openNextActivity();
                        }).show();

                    } else {
                        getMvpView().showMessage(context.getString(R.string.minimum).concat(" ") + getDataManager().getConfigurableParameterDetail().getMinimumFinger() + " " + context.getString(R.string.fingerprint_should_match));
                    }
                } else {
                    getMvpView().showMessage(R.string.fingerprintEnrollmentNotConfigured);
                }
            }
        } else if (bitmap == null) {
            getMvpView().showMessage(R.string.PleaseCaptureImage);
        } else if (memberInfo == null)
            getMvpView().showMessage(R.string.PleaseCaptureFingerPrints);
    }

    @Override
    public void UploadBeneficiaryBiometric(JSONObject object) {
        getMvpView().showLoading();
        RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());

        if (getMvpView().isNetworkConnected()) {
            getDataManager().doUploadBeneficiaryFingerPrint("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    getMvpView().hideLoading();
                    if (response.code() == 200) {
                        getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.BiometricUploaded).setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            getMvpView().openNextActivity();
                        }).show();
                    } else if (response.code() == 401) {
                        getMvpView().hideLoading();
                        getMvpView().openActivityOnTokenExpire();
                    } else {
                        assert response.errorBody() != null;
                        try {
                            getMvpView().hideLoading();
                            JSONObject object1 = new JSONObject(response.errorBody().string());
                            if (object1.has("message"))
                                getMvpView().showMessage(object1.getString("message"));
                        } catch (Exception e) {
                            getMvpView().hideLoading();
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    handleApiFailure(call, t);
                }
            });

        }

    }
}
