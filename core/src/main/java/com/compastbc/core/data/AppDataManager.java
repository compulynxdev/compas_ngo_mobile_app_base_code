package com.compastbc.core.data;

import android.content.Context;

import com.compastbc.core.data.db.AppDBHelper;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.db.model.DaoSession;
import com.compastbc.core.data.network.ApiHelper;
import com.compastbc.core.data.network.AppApiHelper;
import com.compastbc.core.data.network.model.BeneficiaryListResponse;
import com.compastbc.core.data.network.model.Configuration;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.data.network.model.Topups;
import com.compastbc.core.data.prefs.AppPreferencesHelper;
import com.compastbc.core.data.prefs.PreferencesHelper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;


/**
 * Created by hemant
 * Date: 10/4/18.
 */


public final class AppDataManager implements DataManager {

    private static AppDataManager instance;
    private final Gson mGson;
    private final ApiHelper mApiHelper;
    private final PreferencesHelper mPreferencesHelper;
    private final AppDBHelper appDBHelper;
    private Topups topups;


    private AppDataManager(Context context) {
        mPreferencesHelper = new AppPreferencesHelper(context);
        mApiHelper = AppApiHelper.getAppApiInstance();
        appDBHelper = AppDBHelper.getInstance(context);
        mGson = new GsonBuilder().create();
    }

    public synchronized static AppDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new AppDataManager(context);
        }
        return instance;
    }


    @Override
    public boolean isLoggedIn() {
        return mPreferencesHelper.isLoggedIn();
    }

    @Override
    public void setLoggedIn(boolean isLoggedIn) {
        mPreferencesHelper.setLoggedIn(isLoggedIn);
    }

    @Override
    public HashMap<String, String> getHeader() {
        return mPreferencesHelper.getHeader();
    }

    @Override
    public Details getUserDetail() {
        return mPreferencesHelper.getUserDetail();
    }

    @Override
    public void setUserDetail(Details detail) {
        mPreferencesHelper.setUserDetail(detail);
    }

    @Override
    public Configuration getConfigurationDetail() {
        return mPreferencesHelper.getConfigurationDetail();
    }

    @Override
    public void setConfigurationDetail(Configuration configuration) {
        mPreferencesHelper.setConfigurationDetail(configuration);
    }

    @Override
    public void setUser(String userName) {
        mPreferencesHelper.setUser(userName);
    }

    @Override
    public void setPassword(String userPassword) {
        mPreferencesHelper.setPassword(userPassword);
    }

    @Override
    public String getUserName() {
        return mPreferencesHelper.getUserName();
    }

    @Override
    public String getUserPassword() {
        return mPreferencesHelper.getUserPassword();
    }

    @Override
    public boolean isFirstTime() {
        return mPreferencesHelper.isFirstTime();
    }

    @Override
    public void setFirstTimeStatus(boolean isFirst) {
        mPreferencesHelper.setFirstTimeStatus(isFirst);
    }

    @Override
    public Gson getGson() {
        return mGson;
    }

    @Override
    public String getDeviceId() {
        return mPreferencesHelper.getDeviceId();
    }

    @Override
    public void setDeviceId(String deviceId) {
        mPreferencesHelper.setDeviceId(deviceId);
    }

    @Override
    public ConfigurableParameters getConfigurableParameterDetail() {
        return mPreferencesHelper.getConfigurableParameterDetail();
    }

    @Override
    public void setConfigurableParameterDetail(ConfigurableParameters parameters) {
        mPreferencesHelper.setConfigurableParameterDetail(parameters);
    }

    @Override
    public Topups getTopupDetails() {
        return topups;
    }

    @Override
    public void setTopupDetails(Topups topups) {
        this.topups = topups;
    }

    @Override
    public String getLanguage() {
        return mPreferencesHelper.getLanguage();
    }

    @Override
    public void setLanguage(String language) {
        mPreferencesHelper.setLanguage(language);
    }

    @Override
    public Beneficiary getCurrentVerifyBenfInfo() {
        return mPreferencesHelper.getCurrentVerifyBenfInfo();
    }

    @Override
    public void setCurrentVerifyBenfInfo(Beneficiary currentVerifyBenfInfo) {
        mPreferencesHelper.setCurrentVerifyBenfInfo(currentVerifyBenfInfo);
    }

    @Override
    public String getCurrency() {
        return mPreferencesHelper.getCurrency();
    }

    @Override
    public void setCurrency(String currency) {
        mPreferencesHelper.setCurrency(currency);
    }

    @Override
    public Call<String> UploadPendingSyncLogs(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadPendingSyncLogs(authToken, file);
    }

    @Override
    public Call<ResponseBody> doUploadAgentBio(String authToken, RequestBody partMap) {
        return mApiHelper.doUploadAgentBio(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> doUploadBeneficiary(String authToken, RequestBody partMap) {
        return mApiHelper.doUploadBeneficiary(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> doUploadBeneficiaryFingerPrint(String authToken, RequestBody partMap) {
        return mApiHelper.doUploadBeneficiaryFingerPrint(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getAgentBio(String authToken, Map<String, Integer> partMap) {
        return mApiHelper.getAgentBio(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryByIdno(String authToken, Map<String, String> partMap) {
        return mApiHelper.getBeneficiaryByIdno(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryByIdentityno(String authToken, Map<String, String> partMap) {
        return mApiHelper.getBeneficiaryByIdentityno(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryByCardno(String authToken, Map<String, String> partMap) {
        return mApiHelper.getBeneficiaryByCardno(authToken, partMap);
    }

    @Override
    public Call<BeneficiaryListResponse> getBeneficiaryList(String authToken, String url) {
        return mApiHelper.getBeneficiaryList(authToken, url);
    }

    @Override
    public Call<ResponseBody> doUpdateBeneficiary(String authToken, RequestBody partMap) {
        return mApiHelper.doUpdateBeneficiary(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getCardIsBlocked(String authToken, Map<String, String> partMap) {
        return mApiHelper.getCardIsBlocked(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryTopups(String authToken, Map<String, String> partMap) {
        return mApiHelper.getBeneficiaryTopups(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getProgramsByTopups(String authToken, RequestBody body) {
        return mApiHelper.getProgramsByTopups(authToken, body);
    }

    @Override
    public Call<ResponseBody> getVouchersByPrograms(String authToken, Map<String, Integer> map) {
        return mApiHelper.getVouchersByPrograms(authToken, map);
    }

    @Override
    public Call<ResponseBody> getCommoditiesByVouchers(String authToken, Map<String, Integer> map) {
        return mApiHelper.getCommoditiesByVouchers(authToken, map);
    }

    @Override
    public Call<ResponseBody> getUomByServiceId(String authToken, Map<String, String> map) {
        return mApiHelper.getUomByServiceId(authToken, map);
    }

    @Override
    public Call<ResponseBody> uploadTransactions(String authToken, RequestBody body) {
        return mApiHelper.uploadTransactions(authToken, body);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryFingerPrint(String authToken, RequestBody body) {
        return mApiHelper.getBeneficiaryFingerPrint(authToken, body);
    }

    @Override
    public Call<ResponseBody> getLastTransaction(String authToken, RequestBody body) {
        return mApiHelper.getLastTransaction(authToken, body);
    }

    @Override
    public Call<ResponseBody> setLastTransaction(String authToken, RequestBody body) {
        return mApiHelper.setLastTransaction(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCountDetails(String authToken, RequestBody body) {
        return mApiHelper.getCountDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getTransactionDetails(String authToken, RequestBody body) {
        return mApiHelper.getTransactionDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getTransactionHistoryDetails(String authToken, RequestBody body) {
        return mApiHelper.getTransactionHistoryDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getVoidTransactionDetails(String authToken, RequestBody body) {
        return mApiHelper.getVoidTransactionDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCommodityReportData(String authToken, RequestBody body) {
        return mApiHelper.getCommodityReportData(authToken, body);
    }

    @Override
    public Call<ResponseBody> getProgrammesForSales(String authToken, RequestBody body) {
        return mApiHelper.getProgrammesForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCategoriesForSales(String authToken, RequestBody body) {
        return mApiHelper.getCategoriesForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCommoditiesForSales(String authToken, RequestBody body) {
        return mApiHelper.getCommoditiesForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getUomForSales(String authToken, RequestBody body) {
        return mApiHelper.getUomForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryForSales(String authToken, RequestBody body) {
        return mApiHelper.getBeneficiaryForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> GetApkType(String authorisation, Map<String, String> map) {
        return mApiHelper.GetApkType(authorisation, map);
    }

    @Override
    public Call<ResponseBody> doUpdateApplication() {
        return mApiHelper.doUpdateApplication();
    }

    @Override
    public Call<String> UploadTransactions(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadTransactions(authToken, file);
    }

    @Override
    public Call<String> UploadAttendanceLogs(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadAttendanceLogs(authToken, file);
    }

    @Override
    public Call<String> UploadArchiveLogs(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadArchiveLogs(authToken, file);
    }

    @Override
    public Call<String> UploadTopupLogs(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadTopupLogs(authToken, file);
    }

    @Override
    public Call<String> UploadActivityLogs(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadActivityLogs(authToken, file);
    }

    @Override
    public Call<String> UploadAgents(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadAgents(authToken, file);
    }

    @Override
    public Call<ResponseBody> UploadBeneficiaries(String authToken, MultipartBody.Part file) {
        return mApiHelper.UploadBeneficiaries(authToken, file);
    }

    @Override
    public Call<ResponseBody> changeAgentPassword(String authorisation, RequestBody body) {
        return mApiHelper.changeAgentPassword(authorisation, body);
    }

    @Override
    public Call<ResponseBody> GetAccessToken(String authorisation, Map<String, String> partMap) {
        return mApiHelper.GetAccessToken(authorisation, partMap);
    }

    @Override
    public Call<ResponseBody> UpdateMaster(String authToken, RequestBody deviceId) {
        return mApiHelper.UpdateMaster(authToken, deviceId);
    }

    @Override
    public Call<ResponseBody> GetBeneficiaryPageable(String authorisation, Map<String, String> map) {
        return mApiHelper.GetBeneficiaryPageable(authorisation, map);
    }

    @Override
    public Call<ResponseBody> GetTopupPageable(String authorisation, RequestBody body) {
        return mApiHelper.GetTopupPageable(authorisation, body);
    }

    @Override
    public void changeBaseUrl(String baseUrl) {
        AppApiHelper.changeBaseUrl(baseUrl);
    }

    @Override
    public DaoSession getDaoSession() {
        return appDBHelper.getDaoSession();
    }
}
