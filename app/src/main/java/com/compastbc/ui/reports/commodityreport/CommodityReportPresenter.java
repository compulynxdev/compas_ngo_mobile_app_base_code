package com.compastbc.ui.reports.commodityreport;

import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.network.model.CommodityReportBean;
import com.compastbc.core.data.network.model.ReportModel;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommodityReportPresenter<V extends CommodityReportMvpView> extends BasePresenter<V>
        implements CommodityReportMvpPresenter<V> {

    private final Context context;

    private final DatePickerDialog.OnDateSetListener datePicker = (view, selectedYear, selectedMonth, selectedDay) -> {
        selectedMonth += 1;
        getMvpView().setDate(CalenderUtils.formatDate(selectedDay + "/" + selectedMonth + "/" + selectedYear, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT),
                CalenderUtils.formatDate(selectedDay + "/" + selectedMonth + "/" + selectedYear, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT, Locale.getDefault()));
    };

    CommodityReportPresenter(DataManager dataManager, Context context1) {
        super(dataManager);
        context = context1;
    }

    @Override
    public void onSelectDate() {
        //date 26/06/2018
        Calendar newCalender = Calendar.getInstance();

        int day, month, year;

        day = newCalender.get(Calendar.DAY_OF_MONTH);
        month = newCalender.get(Calendar.MONTH);
        year = newCalender.get(Calendar.YEAR);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, datePicker, year, month, day);
        Date maxDate = CalenderUtils.getDateFormat(CalenderUtils.getCurrentDate(), CalenderUtils.TIMESTAMP_FORMAT);
        assert maxDate != null;
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTime());

        datePickerDialog.show();
    }

    private List<Commodities> getCommodities(Cursor cursor1) {
        List<Commodities> commodities = new ArrayList<>();
        if (cursor1.moveToFirst()) {
            do {
                Commodities commodity = new Commodities();
                commodity.setProgramId(cursor1.getString(cursor1.getColumnIndexOrThrow(CommoditiesDao.Properties.ProgramId.columnName)));
                commodity.setProductId(cursor1.getString(cursor1.getColumnIndexOrThrow(CommoditiesDao.Properties.ProductId.columnName)));
                commodity.setProductName(cursor1.getString(cursor1.getColumnIndexOrThrow(CommoditiesDao.Properties.ProductName.columnName)));
                commodity.setTotalAmountChargedByRetailer(cursor1.getDouble(cursor1.getColumnIndexOrThrow(CommoditiesDao.Properties.TotalAmountChargedByRetailer.columnName)));
                commodities.add(commodity);
            } while (cursor1.moveToNext());
        }
        if (!cursor1.isClosed()) {
            cursor1.close();
        }
        return commodities;
    }

    @Override
    public void getData(String selectedDate) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            if (getMvpView().isNetworkConnected()) {
                try {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("deviceId", getDataManager().getDeviceId());
                    jsonObject.put("locationId",getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("agentId",getDataManager().getUserDetail().getAgentId());
                    jsonObject.put("selectedDate", selectedDate.contains("/") ? CalenderUtils.formatDate(selectedDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : selectedDate);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getCommodityReportData("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                            if (response.code() == 200) {
                                assert response.body() != null;
                                try {
                                    JSONArray array = new JSONArray(response.body().string());
                                    List<CommodityReportBean> beanList = new ArrayList<>();
                                    for (int i = 0; i < array.length(); i++) {
                                        CommodityReportBean bean = new CommodityReportBean();
                                        JSONObject object = array.getJSONObject(i);
                                        bean.setTitle(object.getString("programName"));
                                        bean.setTtlAmt(object.getString("totalAmount"));
                                        bean.setCurrency(object.getString("programCurrency"));
                                        JSONArray commodityArray = object.getJSONArray("commodities");
                                        List<ReportModel> modelList = new ArrayList<>();
                                        for (int j = 0; j < commodityArray.length(); j++) {
                                            ReportModel model = new ReportModel();
                                            JSONObject commodity = commodityArray.getJSONObject(j);
                                            model.setName(commodity.getString("name"));
                                            model.setValue(commodity.getString("amount"));
                                            model.setCurrency(commodity.getString("currency"));
                                            modelList.add(model);
                                        }
                                        bean.setModelList(modelList);
                                        beanList.add(bean);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().setData(beanList);
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
            List<Programs> programsList = getDataManager().getDaoSession().getProgramsDao().queryBuilder().list();
            List<CommodityReportBean> beanList = new ArrayList<>();
            for (int i = 0; i < programsList.size(); i++) {
                String colName = CommoditiesDao.Properties.ProductId.columnName;
                String query = "SELECT * from " + CommoditiesDao.TABLENAME
                        + " where " + CommoditiesDao.Properties.Date.columnName + " = ? and " + CommoditiesDao.Properties.VoidTransaction.columnName + " = ? and "
                        + CommoditiesDao.Properties.ProgramId.columnName + " = ?"
                        + " GROUP BY " + colName;
                Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery(query, new String[]{
                        CalenderUtils.formatDate(selectedDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), "0", programsList.get(i).getProgramId()
                });
                List<Commodities> commodities = getCommodities(cursor1);
                List<ReportModel> modelList = new ArrayList<>();
                double amount = 0.0;
                for (int j = 0; j < commodities.size(); j++) {
                    String id = commodities.get(j).getProductId();
                    Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + CommoditiesDao.Properties.TotalAmountChargedByRetailer.columnName + ") from " +
                                    CommoditiesDao.TABLENAME + " where " + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.Date.columnName + "=? and "
                                    + CommoditiesDao.Properties.VoidTransaction.columnName + " = ? and " + CommoditiesDao.Properties.ProgramId.columnName + " = ?"
                            , new String[]{id,
                                    CalenderUtils.formatDate(selectedDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), "0", programsList.get(i).getProgramId()});
                    if (cursor.moveToFirst()) {
                        ReportModel model = new ReportModel();
                        model.setName(commodities.get(j).getProductName());
                        model.setValue(String.valueOf(cursor.getDouble(0)));
                        model.setCurrency(programsList.get(i).getProgramCurrency());
                        modelList.add(model);
                        amount = amount + Double.parseDouble(String.valueOf(cursor.getDouble(0)));
                    }
                }
                CommodityReportBean bean = new CommodityReportBean();
                bean.setTitle(programsList.get(i).getProgramName());
                bean.setTtlAmt(String.valueOf(amount));
                bean.setCurrency(programsList.get(i).getProgramCurrency());
                bean.setModelList(modelList);
                if (amount != 0.0)
                    beanList.add(bean);
            }
            getMvpView().hideLoading();
            getMvpView().setData(beanList);
        }

    }

}
