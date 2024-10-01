package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_commodity;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.network.model.SalesCommodityBean;
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

public class SelectCommodityPresenter<V extends SelectCommodityMvpView> extends BasePresenter<V>
        implements SelectCommodityMvpPresenter<V> {
    private final Context context;

    SelectCommodityPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void getSaleCommodities(String programId, String productId, String categoryId, int offset, String startDate, String endDate) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            JSONObject jsonObject = new JSONObject();
            if (getMvpView().isNetworkConnected()) {
                try {
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    jsonObject.put("productId", Integer.parseInt(productId));
                    jsonObject.put("programmeId", programId);
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("locationId",getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("startDate", startDate.contains("/") ? CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : startDate);
                    jsonObject.put("endDate", endDate.contains("/") ? CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.DATE_FORMAT) : endDate);
                    jsonObject.put("categoryId", Integer.parseInt(categoryId));
                    jsonObject.put("page", offset);
                    jsonObject.put("size", AppConstants.LIMIT);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getCommoditiesForSales("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                            if (response.code() == 200) {

                                assert response.body() != null;
                                try {
                                    List<SalesCommodityBean> beans = new ArrayList<>();
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    JSONArray array = jsonObject.getJSONArray("content");
                                    for (int i = 0; i < array.length(); i++) {
                                        SalesCommodityBean bean = new SalesCommodityBean();
                                        bean.setCommodityName(array.getJSONObject(i).getString("commodityName"));
                                        bean.setCommodityId(array.getJSONObject(i).getString("commodityId"));
                                        bean.setTotalAmount(array.getJSONObject(i).getString("totalAmount"));
                                        bean.setCommodityType(array.getJSONObject(i).getString("commodityType"));
                                        bean.setCurrency(array.getJSONObject(i).getString("programCurrency"));
                                        bean.setBeneficiaryCount(array.getJSONObject(i).getString("totalBeneficiary"));
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
            List<Services> servicesList = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.CategoryId.eq(categoryId)).limit(AppConstants.LIMIT).offset(offset).list();
            List<SalesCommodityBean> beans = new ArrayList<>();
            String currency = getProgramCurrency(programId);
            for (Services services : servicesList) {

                SalesCommodityBean bean = new SalesCommodityBean();
                bean.setCommodityName(services.getServiceName());
                bean.setCommodityId(services.getServiceId());
                bean.setCommodityType(services.getServiceType());

                Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + CommoditiesDao.Properties.TotalAmountChargedByRetailer.columnName + ") from " +
                                CommoditiesDao.TABLENAME + " where " + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.Date.columnName + " between ? and ? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                                + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                        , new String[]{services.getServiceId(),
                                CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT),
                                CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), programId, "0"});

                if (cursor.moveToFirst()) {
                    bean.setTotalAmount(String.valueOf(cursor.getDouble(0)));
                    bean.setCurrency(currency);
                }

                Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + CommoditiesDao.TABLENAME
                        + " where " + CommoditiesDao.Properties.Date.columnName + " between ? and ? and "
                        + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                        + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                        + " GROUP BY " + CommoditiesDao.Properties.IdentificationNum.columnName, new String[]{
                        CalenderUtils.formatDate(startDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT),
                        CalenderUtils.formatDate(endDate, CalenderUtils.TIMESTAMP_FORMAT, CalenderUtils.TIMESTAMP_FORMAT), services.getServiceId(), programId, "0"
                });

                if (cursor1.moveToFirst()) {
                    bean.setBeneficiaryCount(String.valueOf(cursor1.getCount()));
                }
                beans.add(bean);
            }
            getMvpView().hideLoading();
            getMvpView().setData(beans);
        }
    }

    @Override
    public SalesCommodityBean getCommodity(List<SalesCommodityBean> beanList, String id) {
        getMvpView().showLoading();
        for (int i = 0; i < beanList.size(); i++) {
            if (beanList.get(i).getCommodityId().equalsIgnoreCase(id)) {
                getMvpView().hideLoading();
                return beanList.get(i);
            }
        }
        getMvpView().hideLoading();
        return null;
    }

    @Override
    public String getCashCurrency(String programmeId) {
        return getProgramCurrency(programmeId);
    }
}
