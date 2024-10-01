package com.compastbc.ui.reports.xreport;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.XReportBean;
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

public class XReportPresenter<V extends XReportMvpView> extends BasePresenter<V>
        implements XReportMvpPresenter<V> {

    XReportPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getXreportData() {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("locationId", getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getTransactionDetails("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            getMvpView().hideLoading();
                            if (response.code() == 200) {
                                assert response.body() != null;
                                try {
                                    List<XReportBean> reportBeanList = new ArrayList<>();
                                    JSONArray array = new JSONArray(response.body().string());
                                    for (int i = 0; i < array.length(); i++) {
                                        JSONObject object = array.getJSONObject(i);
                                        XReportBean xReportBean = new XReportBean();
                                        xReportBean.transactionAmount = object.getString("totalTransactionAmount");
                                        xReportBean.transactionCount = object.getString("totalTransactionCount");
                                        xReportBean.voidAmount = object.getString("totalVoidTransactionAmount");
                                        xReportBean.currencyType = object.getString("programCurrency");
                                        xReportBean.voidCount = object.getString("totalVoidTransactionCount");
                                        reportBeanList.add(xReportBean);
                                    }
                                    getMvpView().showData(reportBeanList);
                                } catch (Exception e) {
                                    getMvpView().showMessage(e.getMessage());
                                }
                            } else if (response.code() == 401) {
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                assert response.errorBody() != null;
                                try {
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    getMvpView().showMessage(object.getString("message"));
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
            getDataFromDb();
        }
    }

    private void getDataFromDb() {
        String colName = ProgramsDao.Properties.ProgramCurrency.columnName;
        String query = "SELECT * from " + ProgramsDao.TABLENAME
                + " GROUP BY " + colName;
        Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery(query, new String[]{});
        List<Programs> programsList = getProgramList(cursor1);
        List<XReportBean> reportBeanList = new ArrayList<>();
        for (Programs programs : programsList) {
            XReportBean xReportBean = new XReportBean();
            List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.TransactionType.eq("0"),
                    TransactionsDao.Properties.Date.eq(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US)),
                    TransactionsDao.Properties.ProgramCurrency.eq(programs.getProgramCurrency())).list();
            List<Transactions> voidTransactions = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.TransactionType.eq("-1"),
                    TransactionsDao.Properties.Date.eq(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US)),
                    TransactionsDao.Properties.ProgramCurrency.eq(programs.getProgramCurrency())).list();
            double amount = 0;

            xReportBean.currencyType = programs.getProgramCurrency();
            if (transactionsList != null && transactionsList.size() > 0) {
                xReportBean.transactionCount = String.valueOf(transactionsList.size());
                for (int i = 0; i < transactionsList.size(); i++) {
                    amount = amount + Double.parseDouble(transactionsList.get(i).getTotalAmountChargedByRetail());
                }
                xReportBean.transactionAmount = String.format(Locale.ENGLISH, "%.2f", amount);
            } else {
                xReportBean.transactionAmount = String.format(Locale.ENGLISH, "%.2f", amount);
                xReportBean.transactionCount = "0";
            }
            if (voidTransactions != null && voidTransactions.size() > 0) {
                xReportBean.voidCount = String.valueOf(voidTransactions.size());
                amount = 0;
                for (int i = 0; i < voidTransactions.size(); i++) {
                    amount = amount + Double.parseDouble(voidTransactions.get(i).getTotalAmountChargedByRetail());
                }
                xReportBean.voidAmount = String.format(Locale.ENGLISH, "%.2f", amount);
            } else {
                amount = 0;
                xReportBean.voidAmount = String.format(Locale.ENGLISH, "%.2f", amount);
                xReportBean.voidCount = "0";
            }
            if (!xReportBean.voidCount.equalsIgnoreCase("0") || !xReportBean.transactionCount.equalsIgnoreCase("0"))
                reportBeanList.add(xReportBean);
        }
        getMvpView().hideLoading();
        getMvpView().showData(reportBeanList);

    }

    private List<Programs> getProgramList(Cursor cursor1) {
        List<Programs> programsList = new ArrayList<>();
        if (cursor1.moveToFirst()) {
            do {
                Programs programs = new Programs();
                programs.setProductId(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProductId.columnName)));
                programs.setProgramName(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramName.columnName)));
                programs.setProgramCurrency(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramCurrency.columnName)));
                programs.setProgramId(cursor1.getString(cursor1.getColumnIndexOrThrow(ProgramsDao.Properties.ProgramId.columnName)));
                programsList.add(programs);
            } while (cursor1.moveToNext());
        }
        if (!cursor1.isClosed()) {
            cursor1.close();
        }
        return programsList;
    }
}
