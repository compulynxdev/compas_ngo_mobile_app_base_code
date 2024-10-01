package com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.network.model.SalesBeneficiary;
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

public class BeneficiaryUomPresenter<V extends BeneficiaryUomMvpView> extends BasePresenter<V>
        implements BeneficiaryUomMvpPresenter<V> {

    BeneficiaryUomPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getBeneficiaryDetails(String programId, String commodityId, String uom, int offset) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            JSONObject jsonObject = new JSONObject();
            if (getMvpView().isNetworkConnected()) {
                try {
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    jsonObject.put("commodityId", Integer.parseInt(commodityId));
                    jsonObject.put("programmeId", programId);
                    jsonObject.put("uom", uom);
                    jsonObject.put("locationId",getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("page", offset);
                    jsonObject.put("size", AppConstants.LIMIT);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getBeneficiaryForSales("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                            if (response.code() == 200) {

                                assert response.body() != null;
                                try {
                                    List<SalesBeneficiary> beans = new ArrayList<>();
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    JSONArray array = jsonObject.getJSONArray("content");
                                    for (int i = 0; i < array.length(); i++) {
                                        SalesBeneficiary bean = new SalesBeneficiary();
                                        bean.setIdentityNo(array.getJSONObject(i).getString("identityNo"));
                                        bean.setName(array.getJSONObject(i).getString("beneficiaryName"));
                                        bean.setCurrency(array.getJSONObject(i).getString("programCurrency"));
                                        bean.setQuantity(String.valueOf(array.getJSONObject(i).getLong("quantity")));
                                        bean.setValue(array.getJSONObject(i).getString("totalAmount"));
                                        beans.add(bean);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().setData(beans);
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(e.getMessage());
                                }

                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                try {
                                    getMvpView().hideLoading();
                                    assert response.errorBody() != null;
                                    JSONObject object = new JSONObject(response.errorBody().string());
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
                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(e.getMessage());
                }

            }
        } else {
            List<SalesBeneficiary> beans = new ArrayList<>();
            Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + CommoditiesDao.TABLENAME
                    + " where " + CommoditiesDao.Properties.Date.columnName + " = ? and "
                    + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                    + CommoditiesDao.Properties.Uom.columnName + "=? and " + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                    + " GROUP BY " + CommoditiesDao.Properties.IdentificationNum.columnName + " LIMIT " + AppConstants.LIMIT + " OFFSET " + offset, new String[]{
                    CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.US), commodityId, programId, uom, "0"
            });

            String currency = getProgramCurrency(programId);
            List<Commodities> commoditiesList = getCommoditiesByIdentity(cursor);
            for (Commodities commodities : commoditiesList) {
                SalesBeneficiary bean = new SalesBeneficiary();
                bean.setIdentityNo(commodities.getIdentificationNum());
                bean.setName(commodities.getBeneficiaryName());

                Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + CommoditiesDao.Properties.TotalAmountChargedByRetailer.columnName + "), sum (" + CommoditiesDao.Properties.QuantityDeducted.columnName + " )"
                                + " from " + CommoditiesDao.TABLENAME
                                + " where " + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.Date.columnName + "=? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                                + CommoditiesDao.Properties.Uom.columnName + "=? and " + CommoditiesDao.Properties.IdentificationNum.columnName + "=? and " + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                        , new String[]{commodityId,
                                CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.US), programId, uom, commodities.getIdentificationNum(), "0"});

                if (cursor1.moveToFirst()) {
                    bean.setValue(String.valueOf(cursor1.getDouble(0)));
                    bean.setCurrency(currency);
                    bean.setQuantity(String.valueOf(cursor1.getLong(1)));
                }
                beans.add(bean);
            }
            getMvpView().hideLoading();
            getMvpView().setData(beans);

        }
    }

    private List<Commodities> getCommoditiesByIdentity(Cursor cursor) {

        List<Commodities> commodities = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Commodities commodity = new Commodities();
                commodity.setIdentificationNum(cursor.getString(cursor.getColumnIndexOrThrow(CommoditiesDao.Properties.IdentificationNum.columnName)));
                commodity.setTotalAmountChargedByRetailer(Double.parseDouble(cursor.getString(cursor.getColumnIndexOrThrow(CommoditiesDao.Properties.TotalAmountChargedByRetailer.columnName))));
                commodity.setBeneficiaryName(cursor.getString(cursor.getColumnIndexOrThrow(CommoditiesDao.Properties.BeneficiaryName.columnName)));
                commodity.setProductId(cursor.getString(cursor.getColumnIndexOrThrow(CommoditiesDao.Properties.ProductId.columnName)));
                commodities.add(commodity);
            } while (cursor.moveToNext());

            if (!cursor.isClosed()) cursor.close();
        }
        return commodities;
    }
}
