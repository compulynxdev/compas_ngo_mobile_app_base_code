package com.compastbc.core.data.network;

import com.compastbc.core.data.network.model.BeneficiaryListResponse;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;


/**
 * Created by hemant
 * Date: 10/4/18.
 */

public interface ApiHelper {

    //login apis
    @FormUrlEncoded
    @POST(Webservices.WEB_ACCESS_TOKEN)
    Call<ResponseBody> GetAccessToken(@Header("authorization") String authorisation, @FieldMap() Map<String, String> partMap);

    @POST(Webservices.WEB_MASTER_URL)
    Call<ResponseBody> UpdateMaster(@Header("authorization") String authToken, @Body RequestBody deviceId);

    @Multipart
    @POST(Webservices.WEB_UPLOAD_PENDING_SYNCS)
    Call<String> UploadPendingSyncLogs(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    //userfpenroll
    @POST(Webservices.WEB_AGENT_BIO_UPDATE)
    Call<ResponseBody> doUploadAgentBio(@Header("authorization") String authToken, @Body RequestBody partMap);

    @FormUrlEncoded
    @POST(Webservices.GET_AGENT_BIO)
    Call<ResponseBody> getAgentBio(@Header("authorization") String authToken, @FieldMap() Map<String, Integer> partMap);

    //beneficiary api
    @Headers("Content-Type: application/json")
    @POST(Webservices.WEB_BNF_ADD)
    Call<ResponseBody> doUploadBeneficiary(@Header("authorization") String authToken, @Body RequestBody partMap);

    @Headers("Content-Type: application/json")
    @POST(Webservices.UPLOAD_BENEFICIARY_FINGERPRINT)
    Call<ResponseBody> doUploadBeneficiaryFingerPrint(@Header("authorization") String authToken, @Body RequestBody partMap);

    @GET(Webservices.GET_BENEFICIARY_BY_IDNO)
    Call<ResponseBody> getBeneficiaryByIdno(@Header("authorization") String authToken, @QueryMap Map<String, String> partMap);

    @GET(Webservices.GET_BENEFICIARY_BY_IDENTITYNUMBER)
    Call<ResponseBody> getBeneficiaryByIdentityno(@Header("authorization") String authToken, @QueryMap Map<String, String> partMap);

    @GET(Webservices.GET_BENEFICIARY_BY_CARDNO)
    Call<ResponseBody> getBeneficiaryByCardno(@Header("authorization") String authToken, @QueryMap() Map<String, String> partMap);

    @GET
    Call<BeneficiaryListResponse> getBeneficiaryList(@Header("authorization") String authToken, @Url String url);

    @Headers("Content-Type: application/json")
    @POST(Webservices.WEB_BNF_UPDATE)
    Call<ResponseBody> doUpdateBeneficiary(@Header("authorization") String authToken, @Body RequestBody partMap);

    //transaction api's
    @GET(Webservices.GET_BLOCK_CARD)
    Call<ResponseBody> getCardIsBlocked(@Header("authorization") String authToken, @QueryMap() Map<String, String> partMap);

    @GET(Webservices.GET_TOPUPS)
    Call<ResponseBody> getBeneficiaryTopups(@Header("authorization") String authToken, @QueryMap() Map<String, String> partMap);

    @POST(Webservices.GET_PROGRAMS_BY_TOPUPS)
    Call<ResponseBody> getProgramsByTopups(@Header("authorization") String authToken, @Body RequestBody body);

    @GET(Webservices.GET_VOUCHERS_BY_PROGRAMS)
    Call<ResponseBody> getVouchersByPrograms(@Header("authorization") String authToken, @QueryMap() Map<String, Integer> map);

    @GET(Webservices.GET_COMMODITIES_BY_VOUCHERS)
    Call<ResponseBody> getCommoditiesByVouchers(@Header("authorization") String authToken, @QueryMap() Map<String, Integer> map);

    @GET(Webservices.GET_UOM_BY_SERVICE_ID)
    Call<ResponseBody> getUomByServiceId(@Header("authorization") String authToken, @QueryMap() Map<String, String> map);

    @POST(Webservices.UploadTransactions)
    Call<ResponseBody> uploadTransactions(@Header("authorization") String authToken, @Body RequestBody body);

    @POST(Webservices.GET_FP_DETAIL)
    Call<ResponseBody> getBeneficiaryFingerPrint(@Header("authorization") String authToken, @Body RequestBody body);

    // void transaction
    @POST(Webservices.FIND_LAST_TRANSACTION)
    Call<ResponseBody> getLastTransaction(@Header("authorization") String authToken, @Body RequestBody body);

    @POST(Webservices.SET_LAST_TRANSACTION)
    Call<ResponseBody> setLastTransaction(@Header("authorization") String authToken, @Body RequestBody body);

    //summary
    @POST(Webservices.GET_COUNT_DETAIL)
    Call<ResponseBody> getCountDetails(@Header("authorization") String authToken, @Body RequestBody body);

    //xreport
    @POST(Webservices.GET_TRANSACTION_DETAIL)
    Call<ResponseBody> getTransactionDetails(@Header("authorization") String authToken, @Body RequestBody body);

    //sales transaction history
    @POST(Webservices.GET_TRANSACTION_HISTORY)
    Call<ResponseBody> getTransactionHistoryDetails(@Header("authorization") String authToken, @Body RequestBody body);

    //void transaction history
    @POST(Webservices.GET_VOID_TRANSACTION)
    Call<ResponseBody> getVoidTransactionDetails(@Header("authorization") String authToken, @Body RequestBody body);

    //daily commodity
    @POST(Webservices.GET_COMMODITY_REPORT_DATA)
    Call<ResponseBody> getCommodityReportData(@Header("authorization") String authToken, @Body RequestBody body);

    //sales basket
    @POST(Webservices.GET_SALE_PROGRAM)
    Call<ResponseBody> getProgrammesForSales(@Header("authorization") String authToken, @Body RequestBody body);

    @POST(Webservices.GET_SALE_CATEGORIES)
    Call<ResponseBody> getCategoriesForSales(@Header("authorization") String authToken, @Body RequestBody body);

    @POST(Webservices.GET_SALE_COMMODITIES)
    Call<ResponseBody> getCommoditiesForSales(@Header("authorization") String authToken, @Body RequestBody body);

    @POST(Webservices.GET_SALE_UOM)
    Call<ResponseBody> getUomForSales(@Header("authorization") String authToken, @Body RequestBody body);

    @POST(Webservices.GET_SALE_BENEFICIARY)
    Call<ResponseBody> getBeneficiaryForSales(@Header("authorization") String authToken, @Body RequestBody body);

    @GET(Webservices.GET_APK_TYPE)
    Call<ResponseBody> GetApkType(@Header("authorization") String authorisation, @QueryMap Map<String, String> map);

    //master calls
    @GET(Webservices.DOWNLOAD_BNF_PAGE)
    Call<ResponseBody> GetBeneficiaryPageable(@Header("authorization") String authorisation, @QueryMap Map<String, String> map);

    @POST(Webservices.DOWNLOAD_BNF_TOPUP)
    Call<ResponseBody> GetTopupPageable(@Header("authorization") String authorisation, @Body RequestBody body);


    @GET(Webservices.WEB_APK_VERSION_CHECK)
    Call<ResponseBody> doUpdateApplication();

    //upload api's
    @Multipart
    @POST(Webservices.WEB_UPLOAD_TRANSACTION)
    Call<String> UploadTransactions(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    @Multipart
    @POST(Webservices.WEB_UPLOAD_ATTENDANCE)
    Call<String> UploadAttendanceLogs(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    @Multipart
    @POST(Webservices.WEB_UPLOAD_ARCHIVE)
    Call<String> UploadArchiveLogs(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    @Multipart
    @POST(Webservices.WEB_UPLOAD_TOPUPLOGS)
    Call<String> UploadTopupLogs(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    @Multipart
    @POST(Webservices.WEB_UPLOAD_ACTIVITIES)
    Call<String> UploadActivityLogs(@Header("authorization") String authToken, @Part MultipartBody.Part file);


    @Multipart
    @POST(Webservices.WEB_UPLOAD_AGENTS)
    Call<String> UploadAgents(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    @Multipart
    @POST(Webservices.WEB_UPLOAD_BENEFICIARY)
    Call<ResponseBody> UploadBeneficiaries(@Header("authorization") String authToken, @Part MultipartBody.Part file);

    @POST(Webservices.CHANGE_AGENT_PASSWORD)
    Call<ResponseBody> changeAgentPassword(@Header("authorization") String authorisation, @Body RequestBody body);

}
