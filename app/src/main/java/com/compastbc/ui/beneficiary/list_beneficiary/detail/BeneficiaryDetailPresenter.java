package com.compastbc.ui.beneficiary.list_beneficiary.detail;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;

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

/**
 * Created by Hemant Sharma on 10-10-19.
 * Divergent software labs pvt. ltd
 */
public class BeneficiaryDetailPresenter<V extends BeneficiaryDetailMvpView> extends BasePresenter<V>
        implements BeneficiaryDetailMvpPresenter<V> {

    private BeneficiaryListResponse.ContentBean data;
    private Beneficiary beneficiary;
    private final Context context;

    BeneficiaryDetailPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void getBnfData(Intent intent) {
        if (intent.hasExtra("EditMode")) {
            boolean isEdit = intent.getBooleanExtra("EditMode", false);
            if (intent.hasExtra("BnfDetail")) {
                data = intent.getParcelableExtra("BnfDetail");
                if (isEdit) getMvpView().updateEditUi(data);
                else getMvpView().updateUi(data);
            } else if (intent.hasExtra("IdentityNo")) {
                String identityNo = intent.getStringExtra("IdentityNo");
                if (getDataManager().getConfigurableParameterDetail().getOnline()) {
                    findBeneficiary(identityNo);
                } else {
                    beneficiary = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IdentityNo.eq(identityNo)).unique();
                    if (isEdit) getMvpView().updateEditUiFromDB(beneficiary);
                    else getMvpView().updateUiFromDB(beneficiary);
                }
            }
        }
    }

    private void findBeneficiary(String idno) {
        getMvpView().showLoading();
        Map<String, String> map = new HashMap<>();
        map.put("identityNo", idno);
        map.put("locationId",getDataManager().getUserDetail().getLocationId());
        if (getMvpView().isNetworkConnected()) {
            getDataManager().getBeneficiaryByIdentityno("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    getMvpView().hideLoading();
                    try {
                        getMvpView().hideLoading();
                        if (response.code() == 200) {
                            assert response.body() != null;
                            JSONObject object = new JSONObject(response.body().string());
                            if (object.length() > 0) {
                                data = new BeneficiaryListResponse.ContentBean();
                                data.setDateOfBirth(object.getString("dateOfBirth"));
                                data.setIdPassPortNo(object.getString("identityNo"));
                                data.setCardno(object.getString("cardNumber"));
                                data.setGender(object.getString("gender"));
                                data.setBioStatus(object.getBoolean("bioStatus"));
                                data.setFirstName(object.getString("firstName"));
                                data.setMemberId(object.getInt("memberId"));
                                data.setPhysicalAdd(object.getString("address"));
                                getMvpView().updateUi(data);
                            } else getMvpView().showMessage(R.string.NoBenfFound);

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
    public void updateBeneficiary(String name, String address, String mob, String gender) {
        if (verifyInput(name, address, gender)) {
            if (getDataManager().getConfigurableParameterDetail().getOnline()) {
                if (getMvpView().isNetworkConnected()) {
                    uploadData(name, address, mob, gender);
                }
            } else {
                beneficiary.setFirstName(name);
                beneficiary.setAddress(address);
                beneficiary.setMobile(mob);
                beneficiary.setGender(gender);
                beneficiary.setIsUploaded("0");
                getDataManager().getDaoSession().getBeneficiaryDao().save(beneficiary);
                SweetAlertDialog alert = getMvpView().sweetAlert(2, R.string.success, R.string.beneficiary_update)
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            getMvpView().openNextActivity(beneficiary.getIdentityNo());
                        });
                alert.setCancelable(false);
                alert.show();
            }
        }
    }

    private void uploadData(String name, String address, String mob, String gender) {
        getMvpView().showLoading();
        JSONObject object = new JSONObject();
        try {
            object.put("firstName", name);
            object.put("gender", gender);
            object.put("physicalAdd", address);
            object.put("dateOfBirth", data.getDateOfBirth());
            object.put("memberId", data.getMemberId());
            object.put("idPassPortNo", data.getIdPassPortNo());
            object.put("mobileNo", mob);
            object.put("benfImage", data.getBenfImage());
            object.put("cardno", data.getCardno());
            object.put("bioStatus", data.isBioStatus());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());

        if (getMvpView().isNetworkConnected()) {
            getDataManager().doUpdateBeneficiary("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            getMvpView().hideLoading();
                            try {
                                if (response.code() == 200) {
                                    assert response.body() != null;
                                    data.setFirstName(name);
                                    data.setPhysicalAdd(address);
                                    data.setMobileNo(mob);
                                    data.setGender(gender);

                                    SweetAlertDialog alert = getMvpView().sweetAlert(2, R.string.success, R.string.beneficiary_update)
                                            .setConfirmClickListener(sweetAlertDialog -> {
                                                sweetAlertDialog.dismissWithAnimation();
                                                getMvpView().openNextActivity(data);
                                            });
                                    alert.setCancelable(false);
                                    alert.show();
                                } else if (response.code() == 401)
                                    getMvpView().openActivityOnTokenExpire();

                                else {
                                    assert response.errorBody() != null;
                                    JSONObject object;
                                    object = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(object.has("message") ? object.getString("message") : context.getString(R.string.some_error));
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

    private boolean verifyInput(String name, String address, String gender) {
        if (name.isEmpty()) {
            getMvpView().showMessage(R.string.please_enter_name);
            return false;
        } else if (address.isEmpty()) {
            getMvpView().showMessage(R.string.please_provide_address);
            return false;
        } else if (gender.isEmpty()) {
            getMvpView().showMessage(R.string.please_select_gender);
            return false;
        } else return true;
    }
}
