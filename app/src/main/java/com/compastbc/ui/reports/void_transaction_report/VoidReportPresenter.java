package com.compastbc.ui.reports.void_transaction_report;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoidReportPresenter<V extends VoidReportMvpView> extends BasePresenter<V>
        implements VoidReportMvpPresenter<V> {

    VoidReportPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getTransactionDetails(String startDate, String endDate, int offset) {
        List<TransactionHistory> transactionHistoryList = new ArrayList<>();
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    jsonObject.put("startDate", startDate.contains("/") ? CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : startDate);
                    jsonObject.put("endDate", endDate.contains("/") ? CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : endDate);
                    jsonObject.put("page", offset);
                    jsonObject.put("size", AppConstants.LIMIT);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getVoidTransactionDetails("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                assert response.body() != null;
                                try {
                                    JSONObject object = new JSONObject(response.body().string());
                                    JSONArray array = object.getJSONArray("content");
                                    for (int i = 0; i < array.length(); i++) {
                                        TransactionHistory transactionHistory = new TransactionHistory();
                                        transactionHistory.setReceiptNo(array.getJSONObject(i).getString("receiptNumber"));
                                        transactionHistory.setAmount(array.getJSONObject(i).getString("totalAmount"));
                                        transactionHistory.setBenfName(array.getJSONObject(i).getString("beneficiaryName"));
                                        transactionHistory.setIdentityNo(array.getJSONObject(i).getString("identityNo"));
                                        transactionHistory.setCurrency(array.getJSONObject(i).getString("programCurrency"));
                                        transactionHistory.setDate(CalenderUtils.formatTimestamp(array.getJSONObject(i).getLong("transactionDate"), CalenderUtils.DATE_FORMAT));
                                        transactionHistoryList.add(transactionHistory);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().setData(transactionHistoryList);
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
                                }
                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                getMvpView().hideLoading();
                                assert response.errorBody() != null;
                                try {
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(object.getString("message"));
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
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

            if (startDate.isEmpty() && endDate.isEmpty()) {
                List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.Date.eq(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US)),
                        TransactionsDao.Properties.TransactionType.eq("-1")).limit(AppConstants.LIMIT).offset(offset).list();
                getTransDetails(transactionsList);

            } else if (startDate.isEmpty()) {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.pleaseSelectStartDate);
            } else if (endDate.isEmpty()) {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.pleaseSelectEndDate);
            } else {
                List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().whereOr(TransactionsDao.Properties.Date.eq(CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT)),
                        TransactionsDao.Properties.Date.gt(CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT))
                ).whereOr(TransactionsDao.Properties.Date.eq(CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT)),
                        TransactionsDao.Properties.Date.lt(CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT))
                ).where(TransactionsDao.Properties.TransactionType.eq("-1")).limit(AppConstants.LIMIT).offset(offset).list();
                getTransDetails(transactionsList);

            }
        }
    }

    private void getTransDetails(List<Transactions> transactionsList) {
        List<TransactionHistory> transactionHistoryList = new ArrayList<>();
        for (Transactions transactions : transactionsList) {
            TransactionHistory history = new TransactionHistory();
            String currency = getProgramCurrency(transactions.getProgramId());
            history.setReceiptNo(String.valueOf(transactions.getReceiptNo()));
            history.setBenfName(transactions.getBeneficiaryName());
            history.setIdentityNo(transactions.getIdentityNo());
            history.setAmount(transactions.getTotalAmountChargedByRetail());
            history.setCurrency(currency);
            history.setDate(transactions.getDate());
            transactionHistoryList.add(history);
        }
        getMvpView().hideLoading();
        getMvpView().setData(transactionHistoryList);

    }

}



