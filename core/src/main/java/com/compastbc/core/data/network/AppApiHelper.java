package com.compastbc.core.data.network;


import com.compastbc.core.data.network.model.BeneficiaryListResponse;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by hemant
 * Date: 10/4/18.
 */

public final class AppApiHelper implements ApiHelper {

    private static ApiHelper apiInterface = null;
    private static AppApiHelper apiHelper;
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .build();

    public synchronized static AppApiHelper getAppApiInstance() {
        if (apiHelper == null) {
            apiHelper = new AppApiHelper();
        }
        return apiHelper;
    }

    private static ApiHelper getApiInterface() {
        if (apiInterface == null) {
            apiInterface = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(Webservices.BASE_URL)
                    .client(okHttpClient)
                    .build().create(ApiHelper.class);
        }

        return apiInterface;
    }

    public static void changeBaseUrl(String baseUrl) {
        apiInterface = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .build().create(ApiHelper.class);
    }


    @Override
    public Call<String> UploadPendingSyncLogs(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadPendingSyncLogs(authToken, file);
    }

    @Override
    public Call<ResponseBody> doUploadAgentBio(String authToken, RequestBody partMap) {
        return getApiInterface().doUploadAgentBio(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> doUploadBeneficiary(String authToken, RequestBody partMap) {
        return getApiInterface().doUploadBeneficiary(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> doUploadBeneficiaryFingerPrint(String authToken, RequestBody partMap) {
        return getApiInterface().doUploadBeneficiaryFingerPrint(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getAgentBio(String authToken, Map<String, Integer> partMap) {
        return getApiInterface().getAgentBio(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryByIdno(String authToken, Map<String, String> partMap) {
        return getApiInterface().getBeneficiaryByIdno(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryByIdentityno(String authToken, Map<String, String> partMap) {
        return getApiInterface().getBeneficiaryByIdentityno(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryByCardno(String authToken, Map<String, String> partMap) {
        return getApiInterface().getBeneficiaryByCardno(authToken, partMap);
    }

    @Override
    public Call<BeneficiaryListResponse> getBeneficiaryList(String authToken, String url) {
        return getApiInterface().getBeneficiaryList(authToken, url);
    }

    @Override
    public Call<ResponseBody> doUpdateBeneficiary(String authToken, RequestBody partMap) {
        return getApiInterface().doUpdateBeneficiary(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getCardIsBlocked(String authToken, Map<String, String> partMap) {
        return getApiInterface().getCardIsBlocked(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryTopups(String authToken, Map<String, String> partMap) {
        return getApiInterface().getBeneficiaryTopups(authToken, partMap);
    }

    @Override
    public Call<ResponseBody> getProgramsByTopups(String authToken, RequestBody body) {
        return getApiInterface().getProgramsByTopups(authToken, body);
    }

    @Override
    public Call<ResponseBody> getVouchersByPrograms(String authToken, Map<String, Integer> map) {
        return getApiInterface().getVouchersByPrograms(authToken, map);
    }

    @Override
    public Call<ResponseBody> getCommoditiesByVouchers(String authToken, Map<String, Integer> map) {
        return getApiInterface().getCommoditiesByVouchers(authToken, map);
    }

    @Override
    public Call<ResponseBody> getUomByServiceId(String authToken, Map<String, String> map) {
        return getApiInterface().getUomByServiceId(authToken, map);
    }

    @Override
    public Call<ResponseBody> uploadTransactions(String authToken, RequestBody body) {
        return getApiInterface().uploadTransactions(authToken, body);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryFingerPrint(String authToken, RequestBody body) {
        return getApiInterface().getBeneficiaryFingerPrint(authToken, body);
    }

    @Override
    public Call<ResponseBody> getLastTransaction(String authToken, RequestBody body) {
        return getApiInterface().getLastTransaction(authToken, body);
    }

    @Override
    public Call<ResponseBody> setLastTransaction(String authToken, RequestBody body) {
        return getApiInterface().setLastTransaction(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCountDetails(String authToken, RequestBody body) {
        return getApiInterface().getCountDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getTransactionDetails(String authToken, RequestBody body) {
        return getApiInterface().getTransactionDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getTransactionHistoryDetails(String authToken, RequestBody body) {
        return getApiInterface().getTransactionHistoryDetails(authToken, body);
    }

    @Override
    public Call<ResponseBody> getVoidTransactionDetails(String authToken, RequestBody body) {
        return getApiInterface().getVoidTransactionDetails(authToken, body);
    }


    @Override
    public Call<ResponseBody> GetAccessToken(String authorisation, Map<String, String> partMap) {
        return getApiInterface().GetAccessToken(authorisation, partMap);
    }

    @Override
    public Call<ResponseBody> UpdateMaster(String authToken, RequestBody deviceId) {
        return getApiInterface().UpdateMaster(authToken, deviceId);
    }

    @Override
    public Call<ResponseBody> getCommodityReportData(String authToken, RequestBody body) {
        return getApiInterface().getCommodityReportData(authToken, body);
    }

    @Override
    public Call<ResponseBody> getProgrammesForSales(String authToken, RequestBody body) {
        return getApiInterface().getProgrammesForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCategoriesForSales(String authToken, RequestBody body) {
        return getApiInterface().getCategoriesForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getCommoditiesForSales(String authToken, RequestBody body) {
        return getApiInterface().getCommoditiesForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getUomForSales(String authToken, RequestBody body) {
        return getApiInterface().getUomForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> getBeneficiaryForSales(String authToken, RequestBody body) {
        return getApiInterface().getBeneficiaryForSales(authToken, body);
    }

    @Override
    public Call<ResponseBody> GetApkType(String authorisation, Map<String, String> map) {
        return getApiInterface().GetApkType(authorisation, map);
    }

    @Override
    public Call<ResponseBody> GetBeneficiaryPageable(String authorisation, Map<String, String> map) {
        return getApiInterface().GetBeneficiaryPageable(authorisation, map);
    }

    @Override
    public Call<ResponseBody> GetTopupPageable(String authorisation, RequestBody body) {
        return getApiInterface().GetTopupPageable(authorisation, body);
    }

    @Override
    public Call<ResponseBody> doUpdateApplication() {
        return getApiInterface().doUpdateApplication();
    }

    @Override
    public Call<String> UploadTransactions(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadTransactions(authToken, file);
    }

    @Override
    public Call<String> UploadAttendanceLogs(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadAttendanceLogs(authToken, file);
    }

    @Override
    public Call<String> UploadArchiveLogs(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadArchiveLogs(authToken, file);
    }

    @Override
    public Call<String> UploadTopupLogs(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadTopupLogs(authToken, file);
    }

    @Override
    public Call<String> UploadActivityLogs(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadActivityLogs(authToken, file);
    }

    @Override
    public Call<String> UploadAgents(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadAgents(authToken, file);
    }

    @Override
    public Call<ResponseBody> UploadBeneficiaries(String authToken, MultipartBody.Part file) {
        return getApiInterface().UploadBeneficiaries(authToken, file);
    }

    @Override
    public Call<ResponseBody> changeAgentPassword(String authorisation, RequestBody body) {
        return getApiInterface().changeAgentPassword(authorisation, body);
    }

}
