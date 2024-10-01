package com.compastbc.ui.reports.summary;

import androidx.annotation.NonNull;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.network.model.SummaryReportBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;

import org.json.JSONObject;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SummaryPresenter<V extends SummaryMvpView> extends BasePresenter<V>
        implements SummaryMvpPresenter<V> {
    SummaryPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getAllDetails() {
        getMvpView().showLoading();

        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("macAddress", getDataManager().getDeviceId());
                    object.put("locationId", Integer.parseInt(getDataManager().getUserDetail().getLocationId()));
                    object.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());

                    getDataManager().getCountDetails("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                assert response.body() != null;
                                getMvpView().hideLoading();
                                try {
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    SummaryReportBean summaryReportBean = new SummaryReportBean();
                                    summaryReportBean.ttlCardHolder = Long.parseLong(jsonObject.getString("totalBeneficiaryCount"));
                                    summaryReportBean.ttlTopup = Long.parseLong(jsonObject.getString("totalTopupCount"));
                                    summaryReportBean.ttlTransactions = Long.parseLong(jsonObject.getString("totalTransactionCount"));
                                    summaryReportBean.ttlCommodities = Long.parseLong(jsonObject.getString("totalCommodityCount"));
                                    summaryReportBean.ttlTopupLog = Long.parseLong(jsonObject.getString("totalTopupLogsCount"));
                                    summaryReportBean.ttlBlockCards = Long.parseLong(jsonObject.getString("totalBlockedCardCount"));
                                    getMvpView().showData(summaryReportBean);
                                } catch (Exception e) {
                                    getMvpView().showMessage(e.getMessage());
                                }

                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                getMvpView().hideLoading();
                                assert response.errorBody() != null;
                                try {
                                    JSONObject jsonObject = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(jsonObject.getString("message"));
                                } catch (Exception e) {
                                    getMvpView().showMessage(e.getMessage());
                                }
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            handleApiFailure(call, t);
                        }
                    });

                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(e.getMessage());
                }

            }
        } else {
            SummaryReportBean summaryReportBean = new SummaryReportBean();
            summaryReportBean.ttlBlockCards = getDataManager().getDaoSession().getBlockCardsDao().count();
            summaryReportBean.ttlCardHolder = getDataManager().getDaoSession().getBeneficiaryDao().count();
            summaryReportBean.ttlCommodities = getDataManager().getDaoSession().getServicesDao().count();
            summaryReportBean.ttlTopup = getDataManager().getDaoSession().getTopupsDao().count();
            summaryReportBean.ttlTransactions = getDataManager().getDaoSession().getTransactionsDao().count();
            summaryReportBean.ttlTopupLog = getDataManager().getDaoSession().getTopupLogsDao().count();
            getMvpView().hideLoading();
            getMvpView().showData(summaryReportBean);
        }
    }
}
