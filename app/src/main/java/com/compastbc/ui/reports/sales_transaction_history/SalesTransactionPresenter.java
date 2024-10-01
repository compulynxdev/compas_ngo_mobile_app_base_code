package com.compastbc.ui.reports.sales_transaction_history;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SalesTransactionPresenter<V extends SalesTransactionMvpView> extends BasePresenter<V>
        implements SalesTransactionMvpPresenter<V> {

    SalesTransactionPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void setupSearch(EditText etSearch) {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().isEmpty())
                    getMvpView().doSearch(0, "");
                else if (editable.toString().length() > 2) {
                    getMvpView().doSearch(0, editable.toString().trim());
                }
            }
        });
    }

    @Override
    public void getTransactionDetails(String search, String startDate, String endDate, int offset) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("locationId",getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    jsonObject.put("startDate", startDate.contains("/") ? CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : startDate);
                    jsonObject.put("endDate", endDate.contains("/") ? CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : endDate);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getTransactionHistoryDetails("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.code() == 200) {
                                assert response.body() != null;
                                try {
                                    List<TransactionHistory> transactionHistoryList = new ArrayList<>();
                                    JSONArray array = new JSONArray(response.body().string());
                                    for (int i = 0; i < array.length(); i++) {
                                        TransactionHistory transactionHistory = new TransactionHistory();
                                        JSONObject obj = array.getJSONObject(i);
                                        transactionHistory.setReceiptNo(obj.getString("receiptNumber"));
                                        transactionHistory.setAmount(obj.getString("amount"));
                                        transactionHistory.setCommodityName(obj.getString("commodityName"));
                                        transactionHistory.setBenfName(obj.getString("beneficiaryName"));
                                        transactionHistory.setUom(obj.getString("uom"));
                                        transactionHistory.setCurrency(obj.getString("programCurrency"));
                                        transactionHistory.setDate(CalenderUtils.formatDate(new Date(obj.getLong("createdOn")),CalenderUtils.TIMESTAMP_FORMAT));
                                        transactionHistory.setQuantity(String.valueOf(obj.getLong("quantity")));
                                        transactionHistory.setTransactionType(obj.getString("transactionType"));
                                        transactionHistory.setIdentityNo(obj.getString("identityNo"));
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
            getTransactionHistoryFromDb(search, startDate, endDate, offset);
        }

    }

    private void getTransactionHistoryFromDb(String search, String startDate, String endDate, int offset) {
        if (startDate.isEmpty() && endDate.isEmpty()) {
            List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.Date.eq(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US)))
                    .limit(AppConstants.LIMIT).offset(offset).list();
            getTransDetails(transactionsList);

        } else if (startDate.isEmpty()) {
            getMvpView().hideLoading();
            getMvpView().showMessage(R.string.pleaseSelectStartDate);
        } else if (endDate.isEmpty()) {
            getMvpView().hideLoading();
            getMvpView().showMessage(R.string.pleaseSelectEndDate);
        } else {
            List<Transactions> transactionsList;
            if (search.isEmpty()) {
                transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().whereOr(TransactionsDao.Properties.Date.eq(CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT)),
                        TransactionsDao.Properties.Date.gt(CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT))
                ).whereOr(TransactionsDao.Properties.Date.eq(CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT)),
                        TransactionsDao.Properties.Date.lt(CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT))
                ).limit(AppConstants.LIMIT).offset(offset).list();
            } else {
                transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().whereOr(TransactionsDao.Properties.Date.eq(CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT)),
                        TransactionsDao.Properties.Date.gt(CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT))
                ).whereOr(TransactionsDao.Properties.Date.eq(CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT)),
                        TransactionsDao.Properties.Date.lt(CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT))
                ).whereOr(TransactionsDao.Properties.IdentityNo.like("%" + search + "%"),
                        TransactionsDao.Properties.ReceiptNo.like("%" + search + "%")).limit(AppConstants.LIMIT).offset(offset).list();
            }

            getTransDetails(transactionsList);
        }
    }

    private void getTransDetails(List<Transactions> transactionsList) {
        List<TransactionHistory> histories = new ArrayList<>();
        for (int i = 0; i < transactionsList.size(); i++) {
            String currency = getProgramCurrency(transactionsList.get(i).getProgramId());
            List<Commodities> commodities = getDataManager().getDaoSession().getCommoditiesDao().queryBuilder().where(CommoditiesDao.Properties.TransactionNo.eq(transactionsList.get(i).getReceiptNo())).list();
            for (int j = 0; j < commodities.size(); j++) {
                TransactionHistory history = new TransactionHistory();
                history.setAmount(String.valueOf(commodities.get(j).getTotalAmountChargedByRetailer()));
                history.setCurrency(currency);
                history.setReceiptNo(commodities.get(j).getTransactionNo());
                history.setBenfName(commodities.get(j).getBeneficiaryName());
                history.setCommodityName(commodities.get(j).getProductName());
                history.setIdentityNo(transactionsList.get(i).getIdentityNo());
                history.setCardSerialNumber(transactionsList.get(i).getCardSerialNumber());
                history.setQuantity(commodities.get(j).getQuantityDeducted());
                history.setTransactionType(transactionsList.get(i).getTransactionType());
                history.setUom(commodities.get(j).getUom());
                history.setDate(commodities.get(j).getDate());
                histories.add(history);
            }
        }
        getMvpView().hideLoading();
        getMvpView().setData(histories);
    }
}
