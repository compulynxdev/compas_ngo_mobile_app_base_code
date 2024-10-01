package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_uom;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.ServicePricesDao;
import com.compastbc.core.data.network.model.Uom;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SelectUomPresenter<V extends SelectUomMvpView> extends BasePresenter<V>
        implements SelectUomMvpPresenter<V> {

    private final Context context;

    SelectUomPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;

    }

    @Override
    public void getSaleUom(String programId, String commodityId, int offset, String startDate, String endDate) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            JSONObject jsonObject = new JSONObject();
            if (getMvpView().isNetworkConnected()) {
                try {
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    jsonObject.put("commodityId", Integer.parseInt(commodityId));
                    jsonObject.put("programmeId", programId);
                    jsonObject.put("locationId",getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("startDate", startDate.contains("/") ? CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : startDate);
                    jsonObject.put("endDate", endDate.contains("/") ? CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : endDate);
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("page", offset);
                    jsonObject.put("size", AppConstants.LIMIT);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getUomForSales("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                            if (response.code() == 200) {

                                assert response.body() != null;
                                try {
                                    List<Uom> beans = new ArrayList<>();
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    JSONArray array = jsonObject.getJSONArray("content");
                                    for (int i = 0; i < array.length(); i++) {
                                        Uom bean = new Uom();
                                        bean.setUom(array.getJSONObject(i).getString("uom"));
                                        bean.setMaxPrice(array.getJSONObject(i).getString("maxPrice"));
                                        bean.setCurrency(array.getJSONObject(i).getString("programCurrency"));
                                        bean.setCount(array.getJSONObject(i).getString("totalBeneficiary"));
                                        beans.add(bean);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().setData(beans);
                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().sweetAlert(1, "", e.toString()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().dismissDialogView();
                                    }).show();

                                }

                            } else if (response.code() == 401) {
                                getMvpView().hideLoading();
                                getMvpView().openActivityOnTokenExpire();
                            } else {
                                try {
                                    getMvpView().hideLoading();
                                    assert response.errorBody() != null;
                                    JSONObject object = new JSONObject(response.errorBody().string());
                                    getMvpView().sweetAlert(1, "", object.getString("message")).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().dismissDialogView();
                                    }).show();

                                } catch (Exception e) {
                                    getMvpView().hideLoading();
                                    getMvpView().sweetAlert(1, "", e.toString()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                        sweetAlertDialog.dismissWithAnimation();
                                        getMvpView().dismissDialogView();
                                    }).show();

                                    e.printStackTrace();
                                }
                            }

                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            getMvpView().hideLoading();
                            getMvpView().sweetAlert(1, context.getString(R.string.error), t.getMessage() != null && t.getMessage().isEmpty() ? context.getString(R.string.ServerError) : t.getMessage()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                                sweetAlertDialog.dismissWithAnimation();
                                getMvpView().dismissDialogView();
                            }).show();

                        }
                    });
                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().sweetAlert(1, "", e.toString()).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        getMvpView().dismissDialogView();
                    }).show();

                }

            }

        } else {
            String currency = getProgramCurrency(programId);
            List<ServicePrices> servicePricesList = getDataManager().getDaoSession().getServicePricesDao().queryBuilder().where(ServicePricesDao.Properties.ServiceId.eq(commodityId),
                    ServicePricesDao.Properties.Currency.eq(currency))
                    .limit(AppConstants.LIMIT).offset(offset).list();
            List<Uom> beans = new ArrayList<>();
            for (ServicePrices servicePrices : servicePricesList) {
                Uom bean = new Uom();
                bean.setUom(servicePrices.getUom());
                bean.setMaxPrice(String.valueOf(servicePrices.getMaxPrice()));
                bean.setCurrency(currency);

                Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + CommoditiesDao.TABLENAME
                        + " where " + CommoditiesDao.Properties.Date.columnName + " between ? and ? and "
                        + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                        + CommoditiesDao.Properties.Uom.columnName + "=? and " + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                        + " GROUP BY " + CommoditiesDao.Properties.IdentificationNum.columnName, new String[]{
                        CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT),
                        CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), commodityId, programId, servicePrices.getUom(), "0"
                });

                if (cursor1.moveToFirst()) {
                    bean.setCount(String.valueOf(cursor1.getCount()));
                }

                beans.add(bean);
            }

            getMvpView().hideLoading();
            getMvpView().setData(beans);
        }

    }


    @Override
    public Uom getUom(List<Uom> uoms, String uom) {
        getMvpView().showLoading();
        for (int i = 0; i < uoms.size(); i++) {
            if (uoms.get(i).getUom().equalsIgnoreCase(uom)) {
                getMvpView().hideLoading();
                return uoms.get(i);
            }
        }
        getMvpView().hideLoading();
        return null;
    }
}
