package com.compastbc.ui.settings;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.compastbc.Compas;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.db.model.PurchasedProducts;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.db.model.TransactionListProducts;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.network.Webservices;
import com.compastbc.core.data.network.model.Configuration;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class SettingsPresenter<V extends SettingsMvpView> extends BasePresenter<V>
        implements SettingsMvpPresenter<V> {

    SettingsPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }

    @Override
    public void onClickNext(String ip, String port) {
        if (ip == null || port == null || ip.isEmpty() || port.isEmpty()) {
            getMvpView().onError(R.string.EmptyInputs);
            getMvpView().enableDisableNextButton(true);
        }
        else {
            if (getMvpView().isNetworkConnected()) {
                getMvpView().showLoading();
                Configuration configuration = getDataManager().getConfigurationDetail();
                configuration.setUrl(ip);
                configuration.setPort(port);
                getDataManager().setConfigurationDetail(configuration);
                Webservices.BASE_URL = Webservices.getBaseUrl();
                getDataManager().changeBaseUrl(Webservices.BASE_URL);
                getAccessToken();
            }
            getMvpView().enableDisableNextButton(true);
        }
    }

    @Override
    public void Update(String ip, String port) {
        if (ip != null && port != null && !ip.isEmpty() && !port.isEmpty()) {
            Configuration configuration = getDataManager().getConfigurationDetail();
            configuration.setPort(port);
            configuration.setUrl(ip);
            getDataManager().setConfigurationDetail(configuration);
            Webservices.BASE_URL = Webservices.getBaseUrl();
            getDataManager().changeBaseUrl(Webservices.BASE_URL);
            getMvpView().sweetAlert(2, R.string.success, R.string.data_updated).setConfirmClickListener(sweetAlertDialog -> getMvpView().successfullyUpdate()).show();
        } else getMvpView().showMessage(R.string.please_provide_input_for_update);
    }

    @Override
    public void getApkType(String accountId) {
        getMvpView().showLoading();
        Map<String, String> map = new HashMap<>();
        map.put("accountId", accountId);
        getDataManager().GetApkType("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        JSONObject object = new JSONObject(response.body().string());
                        if (object.getString("apkType").equalsIgnoreCase("online")) {
                            getMvpView().hideLoading();
                            ConfigurableParameters parameters = getDataManager().getConfigurableParameterDetail();
                            parameters.setOnline(true);
                            getDataManager().setConfigurableParameterDetail(parameters);
                        }/*else {
                            getMvpView().hideLoading();
                            getMvpView().showOnlineApk(getDataManager().getConfigurationDetail().getUsername());
                        }*/
                        syncMasterData();
                    } else {
                        assert response.errorBody() != null;
                        handleApiError(response.errorBody().string());
                    }
                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(e.getMessage());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                getMvpView().hideKeyboard();
                handleApiFailure(call, t);
            }
        });

    }

    private void getAccessToken() {
        HashMap<String, String> map = new HashMap<>();
        map.put("username", getDataManager().getUserName());
        map.put("password", getDataManager().getUserPassword());
        map.put("grant_type", "password");
        getDataManager().GetAccessToken(AppConstants.BASIC_AUTHORISATION, map).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                JSONObject object;
                try {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        object = new JSONObject(response.body().string());
                       /* JSONArray array = object.getJSONArray("role");
                        String role = "";
                        if (array.length() > 0)
                            role = array.get(0).toString();
                        if(role.equalsIgnoreCase("NGO_DEVICE_USER")) {
                       */
                        Configuration configuration = getDataManager().getConfigurationDetail();
                        configuration.setUsername(getDataManager().getUserName());
                        configuration.setPassword(getDataManager().getUserPassword());
                        configuration.setAccess_token(object.getString("access_token"));
                        configuration.setRefresh_token(object.getString("refresh_token"));
                        String accountId = object.getString("accountId");
                        configuration.setAccountId(accountId);
                        getDataManager().setConfigurationDetail(configuration);
                        getApkType(accountId);
                        //syncMasterData();
                        // } else getMvpView().showMessage(R.string.InvalidRole);
                    } else if (response.code() == 400) {
                        getMvpView().hideLoading();
                        getMvpView().showMessage(R.string.UnauthorisedAccess);
                    }
                    else if (response.code() == 404) {
                        getMvpView().hideLoading();
                        getMvpView().showMessage(R.string.error_404);
                    }
                    else {
                        assert response.errorBody() != null;
                        handleApiError(response.errorBody().string());
                    }

                } catch (Exception e) {
                    getMvpView().hideLoading();
                    getMvpView().showMessage(R.string.some_error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handleApiFailure(call, t);
            }
        });
    }

    private void syncMasterData() {
        getMvpView().downloadMasterData(() -> {
            /*if (getDataManager().getConfigurableParameterDetail().isOnline())
                getMvpView().openNextActivity(0);
            else {*/

            if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
                if (getDataManager().getUserDetail().isBioStatus())
                    getMvpView().openNextActivity(2);
                else getMvpView().openNextActivity(1);
            } else {
                getDataManager().setLoggedIn(true);
                getMvpView().openNextActivity(0);
            }
            //}
        });

    }

    public void insertDummyTxnData() {
        getMvpView().showLoading();
        AppLogger.e("DATA Test", "Start");

        new Thread(() -> {
            getDataManager().getDaoSession().getTransactionsDao().deleteAll();
            getDataManager().getDaoSession().getCommoditiesDao().deleteAll();

            long idNum = 112012;
            long serialNum = 12012;
            String bnfName = "Shivanshu";

            List<PurchasedProducts> productsList = new ArrayList<>();
            PurchasedProducts temp1 = new PurchasedProducts();
            temp1.setServiceId("1");
            temp1.setMaxPrice("100");
            temp1.setQuantity("8.0");
            temp1.setTotalPrice("800.0");
            temp1.setUom("1.0-KGs");
            productsList.add(temp1);

            PurchasedProducts temp2 = new PurchasedProducts();
            temp2.setServiceId("3");
            temp2.setMaxPrice("60");
            temp2.setQuantity("10.0");
            temp2.setTotalPrice("600.0");
            temp2.setUom("1.0-KGs");
            productsList.add(temp2);

            PurchasedProducts temp3 = new PurchasedProducts();
            temp3.setServiceId("2");
            temp3.setMaxPrice("360");
            temp3.setQuantity("2.0");
            temp3.setTotalPrice("60.0");
            temp3.setUom("12.0-PCS");
            productsList.add(temp3);


            for (int k = 0; k < 5000; k++) {
                Transactions transactions = new Transactions();
                transactions.setAgentId("3");
                long identityNum = idNum + k;
                transactions.setIdentityNo(String.valueOf(identityNum));
                transactions.setCardNo("4440888" + identityNum);
                transactions.setVoucherId("1");
                transactions.setTotalValueRemaining(String.format(Locale.ENGLISH, "%.2f", Double.parseDouble("0.0")));
                transactions.setUser("svendor");
                transactions.setLocationId("1");
                transactions.setProgramId("1");
                transactions.setProgramName("Fruit Program");
                transactions.setProgramCurrency("INR");
                transactions.setDate(CalenderUtils.getDateTime(CalenderUtils.DATE_FORMAT, Locale.US));
                transactions.setTimeStamp(CalenderUtils.getTimestamp(CalenderUtils.DB_TIMESTAMP_FORMAT));
                transactions.setTransactionType("0");
                transactions.setIsUploaded("0");
                transactions.setVoucherIdNo(String.valueOf(identityNum));
                transactions.setDeviceId("f1d10aa912cc7ddb");
                String strNo = String.valueOf(new Date().getTime()).concat("3");
                Long receiptNo = Long.parseLong(strNo);
                transactions.setReceiptNo(receiptNo);
                transactions.setTopupEndDate(new Date());
                transactions.setTopupStartDate(new Date());
                transactions.setBeneficiaryName(bnfName + k);
                transactions.setLatitude(Compas.LATITUDE);
                transactions.setLongitude(Compas.LONGITUDE);
                double amount = 0.0;
                for (int i = 0; i < productsList.size(); i++) {
                    //save commodities
                    amount = amount + Double.parseDouble(productsList.get(i).getTotalPrice());
                    Services services = getDataManager().getDaoSession().getServicesDao().queryBuilder().where(ServicesDao.Properties.ServiceId.eq(productsList.get(i).getServiceId())).unique();
                    Commodities commodities = new Commodities();
                    commodities.setDate(CalenderUtils.getTimestamp(CalenderUtils.TIMESTAMP_FORMAT));
                    commodities.setTransactionNo(transactions.getReceiptNo().toString());
                    commodities.setUniqueId(new Date().getTime() + i);
                    commodities.setProductName(services.getServiceName());
                    commodities.setProgramId("1");
                    commodities.setCategoryId(services.getCategoryId());
                    commodities.setIdentificationNum(String.valueOf(identityNum));
                    commodities.setProductId(productsList.get(i).getServiceId());
                    commodities.setMaxPrice(productsList.get(i).getMaxPrice());
                    commodities.setBeneficiaryName(bnfName + k);
                    commodities.setQuantityDeducted(productsList.get(i).getQuantity());
                    commodities.setTotalAmountChargedByRetailer(Double.parseDouble(productsList.get(i).getTotalPrice()));
                    commodities.setUom(productsList.get(i).getUom());
                    commodities.setVoidTransaction("0");
                    getDataManager().getDaoSession().getCommoditiesDao().save(commodities);

                    //save archive txn
                    TransactionListProducts transactionListProducts = new TransactionListProducts();
                    transactionListProducts.setTransactionDate(CalenderUtils.getTimestamp());
                    transactionListProducts.setTransactionNo(transactions.getReceiptNo().toString());
                    transactionListProducts.setUniqueid(UUID.randomUUID().toString());
                    transactionListProducts.setProductName(services.getServiceName());
                    transactionListProducts.setProductId(productsList.get(i).getServiceId());
                    transactionListProducts.setBeneficiaryName(bnfName + k);
                    transactionListProducts.setDeviceId("f1d10aa912cc7ddb");
                    transactionListProducts.setProgramId("1");
                    transactionListProducts.setQuantity(productsList.get(i).getQuantity());
                    transactionListProducts.setVal(productsList.get(i).getTotalPrice());
                    transactionListProducts.setUnitOfMeasure(productsList.get(i).getUom());
                    transactionListProducts.setVoidTransaction("0");

                    getDataManager().getDaoSession().getTransactionListProductsDao().save(transactionListProducts);
                }
                transactions.setSubmit("1");
                transactions.setTotalAmountChargedByRetail(String.valueOf(amount));
                transactions.setCardSerialNumber(String.valueOf(serialNum + k));
                getDataManager().getDaoSession().getTransactionsDao().save(transactions);
                AppLogger.e("DATA Test", "Success" + k);
            }

            new Handler(Looper.getMainLooper()).post(() -> {
                AppLogger.e("DATA Test", "Done");
                getMvpView().hideLoading();
            });
        }).start();
    }
}

