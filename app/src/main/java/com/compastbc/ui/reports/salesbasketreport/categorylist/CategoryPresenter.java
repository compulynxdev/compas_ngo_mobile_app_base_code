package com.compastbc.ui.reports.salesbasketreport.categorylist;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Categories;
import com.compastbc.core.data.db.model.CategoriesDao;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.network.model.SalesCategoryBean;
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

public class CategoryPresenter<V extends CategoryMvpView> extends BasePresenter<V>
        implements CategoryMvpPresenter<V> {

    CategoryPresenter(DataManager dataManager) {
        super(dataManager);
    }


    @Override
    public void getSaleCatrgories(String programId, String productId, int offset) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            JSONObject jsonObject = new JSONObject();
            if (getMvpView().isNetworkConnected()) {
                try {
                    jsonObject.put("agentId", Integer.parseInt(getDataManager().getUserDetail().getAgentId()));
                    jsonObject.put("productId", Integer.parseInt(productId));
                    jsonObject.put("programmeId", programId);
                    jsonObject.put("locationId",getDataManager().getUserDetail().getLocationId());
                    jsonObject.put("macAddress", getDataManager().getDeviceId());
                    jsonObject.put("page", offset);
                    jsonObject.put("size", AppConstants.LIMIT);
                    RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, jsonObject.toString());
                    getDataManager().getCategoriesForSales("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                            if (response.code() == 200) {

                                assert response.body() != null;
                                try {
                                    List<SalesCategoryBean> beans = new ArrayList<>();
                                    JSONObject jsonObject = new JSONObject(response.body().string());
                                    JSONArray array = jsonObject.getJSONArray("content");
                                    for (int i = 0; i < array.length(); i++) {
                                        SalesCategoryBean bean = new SalesCategoryBean();
                                        bean.setCategoryName(array.getJSONObject(i).getString("categoryName"));
                                        bean.setCategoryId(array.getJSONObject(i).getString("categoryId"));
                                        bean.setCurrency(array.getJSONObject(i).getString("programCurrency"));
                                        bean.setTotalAmount(array.getJSONObject(i).getString("totalAmount"));
                                        bean.setBeneficiaryCount(array.getJSONObject(i).getString("totalBeneficiary"));
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
            List<Categories> categoriesList = getDataManager().getDaoSession().getCategoriesDao().queryBuilder().where(CategoriesDao.Properties.ProductId.eq(productId)).limit(AppConstants.LIMIT)
                    .offset(offset).list();
            List<SalesCategoryBean> beans = new ArrayList<>();
            for (Categories category : categoriesList) {
                SalesCategoryBean bean = new SalesCategoryBean();
                bean.setCategoryName(category.getCategoryName());
                bean.setCategoryId(category.getCategoryId());
                double amount = 0.0;
                int count = 0;

                List<Services> servicesList = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(
                        ServicesDao.Properties.CategoryId.eq(category.getCategoryId())).list();

                for (Services services : servicesList) {
                    Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT sum(" + CommoditiesDao.Properties.TotalAmountChargedByRetailer.columnName + ") from " +
                                    CommoditiesDao.TABLENAME + " where " + CommoditiesDao.Properties.ProductId.columnName + "=? and " + CommoditiesDao.Properties.Date.columnName + "=? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                                    + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                            , new String[]{services.getServiceId(),
                                    CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.US), programId, "0"});

                    if (cursor.moveToFirst()) {
                        amount = amount + cursor.getDouble(0);
                    }
                }
                Cursor cursor1 = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + CommoditiesDao.TABLENAME
                        + " where " + CommoditiesDao.Properties.Date.columnName + " = ? and "
                        + CommoditiesDao.Properties.CategoryId.columnName + "=? and " + CommoditiesDao.Properties.ProgramId.columnName + "=? and "
                        + CommoditiesDao.Properties.VoidTransaction.columnName + " =? "
                        + " GROUP BY " + CommoditiesDao.Properties.IdentificationNum.columnName, new String[]{
                        CalenderUtils.getDateTime(CalenderUtils.TIMESTAMP_FORMAT, Locale.US), category.getCategoryId(), programId, "0"
                });

                if (cursor1.moveToFirst()) {
                    count = cursor1.getCount();
                }
                String currency = getProgramCurrency(programId);
                bean.setTotalAmount(String.valueOf(amount));
                bean.setCurrency(currency);
                bean.setBeneficiaryCount(String.valueOf(count));
                beans.add(bean);
            }
            getMvpView().hideLoading();
            getMvpView().setData(beans);
        }
    }

    @Override
    public SalesCategoryBean getCategory(List<SalesCategoryBean> salesCategoryBeans, String id) {
        SalesCategoryBean bean;
        getMvpView().showLoading();
        for (int i = 0; i < salesCategoryBeans.size(); i++) {
            if (salesCategoryBeans.get(i).getCategoryId().equalsIgnoreCase(id)) {
                bean = salesCategoryBeans.get(i);
                getMvpView().hideLoading();
                return bean;
            }
        }
        getMvpView().hideLoading();
        return null;
    }
}
