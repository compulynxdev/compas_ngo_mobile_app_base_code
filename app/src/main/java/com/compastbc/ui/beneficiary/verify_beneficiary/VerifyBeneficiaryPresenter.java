package com.compastbc.ui.beneficiary.verify_beneficiary;

import android.content.Context;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryBioDao;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.network.model.MemberInfo;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.fingerprint.FingerprintReaderInit;
import com.compastbc.ui.beneficiary.BenfAuthEnum;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyBeneficiaryPresenter<V extends VerifyBeneficiaryMvpView> extends BasePresenter<V> implements VerifyBeneficiaryMvpPresenter<V> {

    private Beneficiary beneficiary;
    private final Context context;

    VerifyBeneficiaryPresenter(Context context, DataManager dataManager) {
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
                        switch (beneficiary.getBioVerifyStatus().toUpperCase()) {
                            case "PENDING":
                                getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.biometric_pending, R.string.biometric_pending_msg)
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().openPreviousActivity();
                                        }).show();
                                break;

                            case "REJECTED":
                                getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.biometric_reject, R.string.biometric_reject_msg)
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().openPreviousActivity();
                                        }).show();
                                break;

                            case "APPROVED":
                                getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.biometrics_already_verify)
                                        .setConfirmClickListener(sweetAlertDialog -> {
                                            getDataManager().setCurrentVerifyBenfInfo(beneficiary);
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().openNextActivity();
                                        }).setCancelText(context.getString(R.string.cancel))
                                        .setCancelClickListener(sweetAlertDialog -> {
                                            sweetAlertDialog.dismissWithAnimation();
                                            getMvpView().openPreviousActivity();
                                        }).show();
                                break;

                            default:
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
                                getMvpView().reEnrollFingerPrint(jsonObject);
                                break;
                        }
                    } else {
                        getMvpView().showMessage(R.string.benf_bio_not_found);
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
                            if (object.getBoolean("bioStatus"))
                                getMvpView().reEnrollFingerPrint(object);

                            else getMvpView().showMessage(R.string.benf_bio_not_found);

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
                            if (object.getBoolean("bioStatus")) {
                                getMvpView().reEnrollFingerPrint(object);
                            } else getMvpView().showMessage(R.string.benf_bio_not_found);

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
    public void doVerifyBenfFp(MemberInfo memberInfo) {
        ConfigurableParameters configParameter = getDataManager().getConfigurableParameterDetail();
        int minimumFinger = configParameter.getMinimumFinger();
        if (configParameter.getFingerPrintActive()) {
            if (AppConstants.fpcount >= minimumFinger) {
                List<String> newFpList = new ArrayList<>();
                newFpList.add(memberInfo.getLeftThumb());
                newFpList.add(memberInfo.getLeftFront());
                newFpList.add(memberInfo.getLeftOne());
                newFpList.add(memberInfo.getLeftTwo());
                newFpList.add(memberInfo.getLeftIndex());

                newFpList.add(memberInfo.getRightThumb());
                newFpList.add(memberInfo.getRightFront());
                newFpList.add(memberInfo.getRightOne());
                newFpList.add(memberInfo.getRightTwo());
                newFpList.add(memberInfo.getRightIndex());

                ArrayList<String> oldFpList = new ArrayList<>();
                BeneficiaryBio beneficiaryBio = getDataManager().getDaoSession().getBeneficiaryBioDao().queryBuilder()
                        .where(BeneficiaryBioDao.Properties.BeneficiaryId.eq(beneficiary.getIdentityNo())).unique();
                if (beneficiaryBio != null) {
                    oldFpList.add(beneficiaryBio.getFpli());
                    oldFpList.add(beneficiaryBio.getFplt());
                    oldFpList.add(beneficiaryBio.getFplf());
                    oldFpList.add(beneficiaryBio.getF1());
                    oldFpList.add(beneficiaryBio.getF2());
                    oldFpList.add(beneficiaryBio.getF3());
                    oldFpList.add(beneficiaryBio.getF4());
                    oldFpList.add(beneficiaryBio.getFpri());
                    oldFpList.add(beneficiaryBio.getFprt());
                    oldFpList.add(beneficiaryBio.getFprf());
                }
                boolean verifyFlag = doVerifyBenfFp(configParameter, minimumFinger, newFpList, oldFpList);
                if (verifyFlag) {
                    beneficiary.setIsUploaded("0");
                    beneficiary.setBio(true);
                    beneficiary.setBioVerifyStatus(String.valueOf(BenfAuthEnum.APPROVED));
                    beneficiary.setDeviceId(getDataManager().getDeviceId());
                    beneficiary.setAgentId(getDataManager().getUserDetail().getAgentId());
                    getDataManager().getDaoSession().getBeneficiaryDao().update(beneficiary);

                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.biometrics_verify)
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                getDataManager().setCurrentVerifyBenfInfo(beneficiary);
                                getMvpView().openNextActivity();
                            }).setCancelText(context.getString(R.string.cancel)).setCancelClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        getMvpView().openPreviousActivity();
                    }).show();
                } else {
                    getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.finger_print_verify)
                            .setConfirmClickListener(sDialog -> {
                                sDialog.dismissWithAnimation();
                                getMvpView().openPreviousActivity();
                            }).show();
                }
            } else {
                getMvpView().showMessage(context.getString(R.string.minimum).concat(" ") + getDataManager().getConfigurableParameterDetail().getMinimumFinger() + " " + context.getString(R.string.fingerprint_should_match));
            }
        } else {
            getMvpView().showMessage(R.string.fingerprintEnrollmentNotConfigured);
        }
    }

    private boolean doVerifyBenfFp(ConfigurableParameters configParameter, int minimumFinger, List<String> newFpList, ArrayList<String> oldFpList) {
        try {
            FingerprintReaderInit fpInstance = FingerprintReaderInit.getInstance(context);
            fpInstance.setMatchPercentage(configParameter.getMatchingPercentage());

            int count = 0;
            if (oldFpList.size() > 0) {
                for (String tmpBnf : newFpList) {
                    for (Object fpString : oldFpList) {
                        boolean isMatch = false;
                        if (tmpBnf != null && fpString != null) {
                            isMatch = fpInstance.verifyFingerPrints(tmpBnf, fpString.toString());
                        }
                        if (isMatch) {
                            count++;
                        } else {
                            AppLogger.e("Test", "No Match");
                        }
                    }
                }
                AppLogger.e("Test", "Match" + count);

                return count >= minimumFinger;
            } else {
                return false;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

}
