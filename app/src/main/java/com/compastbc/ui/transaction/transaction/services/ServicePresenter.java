package com.compastbc.ui.transaction.transaction.services;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.ServiceDetails;
import com.compastbc.core.data.db.model.ServiceDetailsDao;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.ServicePricesDao;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;

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

public class ServicePresenter<V extends ServiceMvpView> extends BasePresenter<V>
        implements ServiceMvpPresenter<V> {


    ServicePresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void getCommodities() {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            Map<String, Integer> map = new HashMap<>();
            map.put("voucherId", Integer.parseInt(getDataManager().getTopupDetails().getVoucherid()));
            map.put("programId", Integer.parseInt(getDataManager().getTopupDetails().getProgrammeid()));
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getCommoditiesByVouchers("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            try {
                                assert response.body() != null;
                                JSONArray object = new JSONArray(response.body().string());
                                if (object.length() > 0) {
                                    List<Services> services = new ArrayList<>();
                                    for (int i = 0; i < object.length(); i++) {
                                        Services service = new Services();
                                        service.setServiceId(object.getJSONObject(i).getString("id"));
                                        service.setServiceName(object.getJSONObject(i).getString("serviceName"));
                                        String image = object.getJSONObject(i).getString("image");
                                        String[] str_img = image.split(",");
                                        service.setServiceImage(str_img[1]);
                                        service.setServiceType(object.getJSONObject(i).getString("compoType"));
                                        services.add(service);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().showServices(services);
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(R.string.NoServices);
                                }
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
            List<ServiceDetails> serviceDetailsList = getDataManager().getDaoSession().getServiceDetailsDao().queryBuilder().where(ServiceDetailsDao.Properties.ProgramId.eq(getDataManager().getTopupDetails().getProgrammeid()), ServiceDetailsDao.Properties.VoucherId.eq(getDataManager().getTopupDetails().getVoucherid())).list();
            if (serviceDetailsList != null && !serviceDetailsList.isEmpty()) {
                List<Services> servicesList = new ArrayList<>();
                for (int i = 0; i < serviceDetailsList.size(); i++) {
                    Services services = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(serviceDetailsList.get(i).getServiceId())).unique();
                    if (services != null) servicesList.add(services);
                }
                if (servicesList.isEmpty()) {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(R.string.NoServices);
                } else {
                    getMvpView().hideLoading();
                    getMvpView().showServices(servicesList);
                }
            } else {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.NoServices);
            }
        }
    }

    @Override
    public void getUoms(String Id) {
        getMvpView().showLoading();
        if (getDataManager().getConfigurableParameterDetail().isOnline()) {
            Map<String, String> map = new HashMap<>();
            map.put("serviceId", Id);
            map.put("programCurrency", getDataManager().getTopupDetails().getProgramCurrency());
            if (getMvpView().isNetworkConnected()) {
                getDataManager().getUomByServiceId("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            try {
                                assert response.body() != null;
                                List<ServicePrices> servicePricesList = new ArrayList<>();
                                JSONArray object = new JSONArray(response.body().string());
                                if (object.length() > 0) {
                                    for (int i = 0; i < object.length(); i++) {
                                        ServicePrices prices = new ServicePrices();
                                        prices.setMaxPrice(Double.parseDouble(object.getJSONObject(i).getString("maxPrice")));
                                        prices.setUom(object.getJSONObject(i).getString("quantity").concat("-").concat(object.getJSONObject(i).getString("uom")));
                                        prices.setServiceId(object.getJSONObject(i).getString("serviceId"));
                                        servicePricesList.add(prices);
                                    }
                                    getMvpView().hideLoading();
                                    getMvpView().showDialog(servicePricesList);
                                } else {
                                    getMvpView().hideLoading();
                                    getMvpView().showMessage(R.string.NotAbleToGetUom);
                                }
                            } catch (Exception e) {
                                getMvpView().hideLoading();
                                getMvpView().showMessage(e.getMessage());
                            }
                        } else if (response.code() == 401) {
                            getMvpView().hideLoading();
                            getMvpView().openActivityOnTokenExpire();
                        } else {
                            try {
                                assert response.errorBody() != null;
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

            }
        } else {
            List<ServicePrices> servicePrices = getDataManager().getDaoSession().getServicePricesDao().queryBuilder().where(ServicePricesDao.Properties.ServiceId.eq(Id),
                    ServicePricesDao.Properties.Currency.eq(getDataManager().getCurrency())).list();
            if (servicePrices != null && !servicePrices.isEmpty()) {
                getMvpView().hideLoading();
                getMvpView().showDialog(servicePrices);
            } else {
                getMvpView().hideLoading();
                getMvpView().showMessage(R.string.NoUoms);
            }
        }
    }
}
