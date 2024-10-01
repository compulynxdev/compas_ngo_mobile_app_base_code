package com.compastbc.ui.transaction.transaction.vouchers;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Vouchers;
import com.compastbc.core.data.db.model.VouchersDao;
import com.compastbc.core.data.network.model.Topups;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VouchersPresenter<V extends VouchersMvpView> extends BasePresenter<V>
        implements VouchersMvpPresenter<V> {

    VouchersPresenter(DataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void getVouchersByProgramId(int programId) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            Map<String, Integer> map = new HashMap<>();
            map.put("programmeId", programId);
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getVouchersByPrograms("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                        if (response.code() == 200) {
                            try {
                                assert response.body() != null;
                                JSONObject object = new JSONObject(response.body().string());
                                List<Vouchers> vouchers = new ArrayList<>();
                                if (object.length() > 0) {
                                    Vouchers voucher = new Vouchers();
                                    voucher.setVoucherId(object.getString("voucherId"));
                                    voucher.setVoucherName(object.getString("voucherName"));
                                    vouchers.add(voucher);
                                }
                                getMvpView().hideLoading();
                                getMvpView().showVouchers(vouchers);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            try {
                                assert response.errorBody() != null;
                                JSONObject object = new JSONObject(response.errorBody().string());
                                getMvpView().hideLoading();
                                getMvpView().showMessage(object.getString("message"));
                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                getMvpView().showMessage(e.getMessage());
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
        } else {
            getMvpView().hideLoading();
            List<Vouchers> vouchersList = getDataManager().getDaoSession().getVouchersDao().queryBuilder().where(VouchersDao.Properties.ProgramId.eq(programId), VouchersDao.Properties.VoucherId.eq(getDataManager().getTopupDetails().getVoucherid())).list();
            if (!vouchersList.isEmpty())
                getMvpView().showVouchers(vouchersList);

            else getMvpView().showMessage(R.string.noVouchersFound);

        }
    }

    @Override
    public void getTopups() {
        getMvpView().showLoading();
        Map<String, String> map = new HashMap<>();
        map.put("cardNo", getDataManager().getTopupDetails().getCardnumber());
        map.put("serialNo", getDataManager().getDeviceId());
        map.put("agentId", getDataManager().getUserDetail().getAgentId());
        if (getMvpView().isNetworkConnected()) {
            getDataManager().getBeneficiaryTopups("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    try {
                        boolean found = false;
                        if (response.code() == 200) {
                            if (response.body() != null) {
                                Topups topups;
                                JSONArray object = new JSONArray(response.body().string());
                                if (object.length() > 0) {
                                    String programId = getDataManager().getTopupDetails().getProgrammeid();
                                    for (int i = 0; i < object.length(); i++) {

                                        if (object.getJSONObject(i).getString("programmeId").equalsIgnoreCase(programId)) {
                                            found = true;
                                            topups = getDataManager().getTopupDetails();
                                            topups.setProgrammeid(object.getJSONObject(i).getString("programmeId"));
                                            topups.setVoucherid(object.getJSONObject(i).getString("voucherId"));
                                            topups.setVouchervalue(object.getJSONObject(i).getString("productPrice"));
                                            topups.setVocheridno(object.getJSONObject(i).getString("voucherIdNumber"));
                                            topups.setProgramCurrency(object.getJSONObject(i).getString("programCurrency"));
                                            topups.setStartDate(object.getJSONObject(i).getString("startDate"));
                                            topups.setEndDate(object.getJSONObject(i).getString("endDate"));
                                            getDataManager().setTopupDetails(topups);
                                        }
                                    }
                                    if (found)
                                        getVouchersByProgramId(Integer.parseInt(programId));

                                    else {
                                        getMvpView().hideLoading();
                                        getMvpView().showMessage(R.string.NoVouchers);
                                    }
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(R.string.NoTopups);
                                }
                            }
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            assert response.errorBody() != null;
                            JSONObject object;
                            object = new JSONObject(response.errorBody().string());
                            getMvpView().hideLoading();
                            getMvpView().showMessage(object.getString("message"));
                        }
                    } catch (Exception e) {
                        getMvpView().hideLoading();
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
}
