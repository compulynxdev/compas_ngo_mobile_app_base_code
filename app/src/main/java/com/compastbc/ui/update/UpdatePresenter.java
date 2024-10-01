package com.compastbc.ui.update;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.BeneficiaryGroups;
import com.compastbc.core.data.db.model.BlockCards;
import com.compastbc.core.data.db.model.Categories;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.db.model.Language;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ServiceDetails;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.Services;
import com.compastbc.core.data.db.model.ServicesDao;
import com.compastbc.core.data.db.model.Topups;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.db.model.Vouchers;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CalenderUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdatePresenter<V extends UpdateMvpView> extends BasePresenter<V>
        implements UpdateMvpPresenter<V> {

    private MaterialDialog materialDialog;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Context context;
    private int offset = 0, topup_offset = 1;

    UpdatePresenter(Context context, DataManager dataManager) {
        super(dataManager);
        this.context = context;
    }

    @Override
    public void Master() {
        materialDialog = getMvpView().materialDialog(R.string.downloadingMaster, R.string.please_wait);
        materialDialog.show();
        RequestBody deviceId = AppUtils.createBody(AppConstants.CONTENT_TYPE_TEXT, getDataManager().getDeviceId());
        getDataManager().UpdateMaster("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), deviceId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                JSONObject object;
                try {
                    if (response.code() == 200) {
                        assert response.body() != null;
                        String responseData = response.body().string();
                        object = new JSONObject(responseData);
                        //Currency
                        if (object.has("currency"))
                            getDataManager().setCurrency(object.getString("currency"));
                        ConfigurableParameters parameters = getDataManager().getConfigurableParameterDetail();

                        if (object.has("attendance_log"))
                            parameters.setAttendanceLog(object.getBoolean("attendance_log"));

                        if (object.has("activity_log"))
                            parameters.setActivityLog(object.getBoolean("activity_log"));

                        if (object.has("carry_forward"))
                            parameters.setCarryForward(object.getBoolean("carry_forward"));

                        if (object.has("void_transaction"))
                            parameters.setVoidTransaction(object.getBoolean("void_transaction"));

                        if (object.has("bio_metric"))
                            parameters.setBiometric(object.getBoolean("biometric"));

                        if (object.has("sale_basket_report"))
                            parameters.setSalesReport(object.getBoolean("sale_basket_report"));

                        if (object.has("automated_background_sync"))
                            parameters.setAutomated(object.getBoolean("automated_background_sync"));

                        if (object.has("ben_id_level_length"))
                            parameters.setIdLength(object.getInt("ben_id_level_length"));
                        else parameters.setIdLength(10);

                       /* if(object.has("mode"))
                            parameters.setOnline(object.getBoolean("mode"));*/

                        if (object.has("biometricDetail")) {
                            JSONObject bioDetail = object.getJSONObject("biometricDetail");

                            if (bioDetail.has("minimumFinger"))
                                parameters.setMinimumFinger(bioDetail.getInt("minimumFinger"));

                            if (bioDetail.has("matchingPercentage"))
                                parameters.setMatchingPercentage(bioDetail.getInt("matchingPercentage"));

                            if (bioDetail.has("fingerPrintActive"))
                                parameters.setFingerPrintActive(bioDetail.getBoolean("fingerPrintActive"));

                            if (bioDetail.has("irisActive"))
                                parameters.setIrisActive(bioDetail.getBoolean("irisActive"));

                            if (bioDetail.has("faceActive"))
                                parameters.setFaceActive(bioDetail.getBoolean("faceActive"));
                        }
                        getDataManager().setConfigurableParameterDetail(parameters);

                        if (object.has("users")) {
                            JSONArray user = object.getJSONArray("users");
                            Details details = new Details();
                            if (user.length() > 0) {
                                if (!getDataManager().getConfigurableParameterDetail().isOnline()) {
                                    //delete all the data and insert new
                                    getDataManager().getDaoSession().getCategoriesDao().deleteAll();
                                    getDataManager().getDaoSession().getBlockCardsDao().deleteAll();
                                    getDataManager().getDaoSession().getProgramsDao().deleteAll();
                                    getDataManager().getDaoSession().getVouchersDao().deleteAll();
                                    getDataManager().getDaoSession().getServiceDetailsDao().deleteAll();
                                    getDataManager().getDaoSession().getServicesDao().deleteAll();
                                    getDataManager().getDaoSession().getServicePricesDao().deleteAll();
                                    getDataManager().getDaoSession().getBeneficiaryGroupsDao().deleteAll();
                                    getDataManager().getDaoSession().getExceptionLogDao().deleteAll();

                                    JSONArray blockCards = object.getJSONArray("blockCards");

                                    List<BlockCards> cardsList = new ArrayList<>();
                                    for (int i = 0; i < blockCards.length(); i++) {
                                        BlockCards cards_table = new BlockCards();
                                        cards_table.setCardNo(blockCards.getJSONObject(i).getString("cardNo"));
                                        cards_table.setIdentityNo(blockCards.getJSONObject(i).getString("rationNo"));
                                        cardsList.add(cards_table);
                                    }
                                    getDataManager().getDaoSession().getBlockCardsDao().insertInTx(cardsList);

                                    JSONArray bnfGroups = object.getJSONArray("bnfGrps");
                                    List<BeneficiaryGroups> groupsList = new ArrayList<>();
                                    if (bnfGroups.length() > 0) {
                                        for (int i = 0; i < bnfGroups.length(); i++) {
                                            BeneficiaryGroups groups = new BeneficiaryGroups();
                                            groups.setBnfGrpId(bnfGroups.getJSONObject(i).getString("bnfGrpId"));
                                            groups.setBnfGrpName(bnfGroups.getJSONObject(i).getString("bnfGrpName"));
                                            groupsList.add(groups);
                                        }
                                        getDataManager().getDaoSession().getBeneficiaryGroupsDao().insertInTx(groupsList);
                                    }

                                    JSONArray programmes = object.getJSONArray("programmes");
                                    JSONArray services = object.getJSONArray("products");

                                    if (services.length() > 0) {
                                        List<Services> servicesList = new ArrayList<>();
                                        for (int i = 0; i < services.length(); i++) {
                                            Services service = new Services();
                                            String[] image = services.getJSONObject(i).getString("image").split(",");
                                            service.setServiceImage(image[1]);
                                            service.setServiceCode(services.getJSONObject(i).getString("productCode"));
                                            service.setServiceId(services.getJSONObject(i).getString("productId"));
                                            service.setServiceName(services.getJSONObject(i).getString("productName"));
                                            service.setServiceType(services.getJSONObject(i).getString("productType"));
                                            service.setCategoryId(services.getJSONObject(i).getString("categoryId"));
                                            if (services.getJSONObject(i).getString("productType").equalsIgnoreCase("commodity")) {
                                                service.setLocationId(services.getJSONObject(i).getString("locationId"));
                                                JSONArray servicePrices = services.getJSONObject(i).getJSONArray("priceDetails");
                                                List<ServicePrices> servicePricesList = new ArrayList<>();
                                                for (int j = 0; j < servicePrices.length(); j++) {
                                                    ServicePrices servicePrices1 = new ServicePrices();
                                                    servicePrices1.setMaxPrice(Double.parseDouble(servicePrices.getJSONObject(j).getString("maxPrice")));
                                                    servicePrices1.setServiceId(services.getJSONObject(i).getString("productId"));
                                                    if (servicePrices.getJSONObject(j).has("currency"))
                                                        servicePrices1.setCurrency(servicePrices.getJSONObject(j).getString("currency"));
                                                    String uom = servicePrices.getJSONObject(j).getString("quantity").concat("-").concat(servicePrices.getJSONObject(j).getString("uom"));
                                                    servicePrices1.setUom(uom);
                                                    servicePricesList.add(servicePrices1);
                                                }
                                                getDataManager().getDaoSession().getServicePricesDao().insertInTx(servicePricesList);
                                            }
                                            servicesList.add(service);
                                        }
                                        getDataManager().getDaoSession().getServicesDao().insertInTx(servicesList);
                                    }

                                    if (programmes.length() > 0) {
                                        List<Programs> programsList = new ArrayList<>();
                                        for (int i = 0; i < programmes.length(); i++) {
                                            Programs programmes_table = new Programs();
                                            programmes_table.setProgramId(programmes.getJSONObject(i).getString("programmeId"));
                                            programmes_table.setProgramName(programmes.getJSONObject(i).getString("programmeName"));
                                            programmes_table.setProductId(programmes.getJSONObject(i).getString("productId"));
                                            if (programmes.getJSONObject(i).has("programCurrency"))
                                                programmes_table.setProgramCurrency(programmes.getJSONObject(i).getString("programCurrency"));
                                            if (programmes.getJSONObject(i).has("vouchers")) {
                                                JSONArray vouchers = programmes.getJSONObject(i).getJSONArray("vouchers");
                                                List<Vouchers> vouchersList = new ArrayList<>();
                                                for (int j = 0; j < vouchers.length(); j++) {
                                                    Vouchers voucher_table = new Vouchers();
                                                    JSONArray products = vouchers.getJSONObject(j).getJSONArray("products");
                                                    voucher_table.setVoucherId(vouchers.getJSONObject(j).getString("voucherId"));
                                                    voucher_table.setVoucherName(vouchers.getJSONObject(j).getString("voucherName"));
                                                    voucher_table.setProgramId(programmes.getJSONObject(i).getString("programmeId"));
                                                    List<ServiceDetails> serviceDetailsList = new ArrayList<>();
                                                    for (int k = 0; k < products.length(); k++) {
                                                        ServiceDetails service_detail_table = new ServiceDetails();
                                                        service_detail_table.setServiceId(products.getJSONObject(k).getString("serviceId"));
                                                        service_detail_table.setVoucherId(vouchers.getJSONObject(j).getString("voucherId"));
                                                        service_detail_table.setProgramId(programmes.getJSONObject(i).getString("programmeId"));
                                                        serviceDetailsList.add(service_detail_table);
                                                    }
                                                    getDataManager().getDaoSession().getServiceDetailsDao().insertInTx(serviceDetailsList);
                                                    vouchersList.add(voucher_table);
                                                }
                                                getDataManager().getDaoSession().getVouchersDao().insertInTx(vouchersList);
                                            }
                                            programsList.add(programmes_table);
                                        }
                                        getDataManager().getDaoSession().getProgramsDao().insertInTx(programsList);
                                    }

                                    JSONArray categories = object.getJSONArray("categories");
                                    if (categories.length() > 0) {
                                        List<Categories> categoriesList = new ArrayList<>();
                                        for (int i = 0; i < categories.length(); i++) {
                                            Categories category = new Categories();
                                            category.setCategoryId(categories.getJSONObject(i).getString("categoryId"));
                                            category.setCategoryName(categories.getJSONObject(i).getString("categoryName"));
                                            category.setProductId(categories.getJSONObject(i).getString("productId"));
                                            categoriesList.add(category);
                                        }
                                        getDataManager().getDaoSession().getCategoriesDao().insertInTx(categoriesList);
                                    }

                                    List<Users> usersList = new ArrayList<>();
                                    for (int i = 0; i < user.length(); i++) {
                                        Users user_table = new Users();
                                        user_table.setUsersId(user.getJSONObject(i).getString("userId"));
                                        user_table.setUsername(user.getJSONObject(i).getString("userName"));
                                        user_table.setPassword(user.getJSONObject(i).getString("password"));
                                        user_table.setLevel(user.getJSONObject(i).getString("level"));
                                        user_table.setLocationid(user.getJSONObject(i).getString("locationId"));
                                        user_table.setAgentId(user.getJSONObject(i).getString("agentId"));
                                        user_table.setLocationName(object.getString("locationName"));
                                        try {
                                            user_table.setBio(user.getJSONObject(i).getBoolean("bioStatus"));
                                            if (user.getJSONObject(i).getBoolean("bioStatus")) {
                                                user_table.setIsuploaded("1");
                                                JSONObject fingers;
                                                fingers = user.getJSONObject(i).getJSONObject("fingerPrint");
                                                if (fingers.has("rightFinger3"))
                                                    user_table.setF1(fingers.getString("rightFinger3"));

                                                if (fingers.has("leftFinger2"))
                                                    user_table.setF2(fingers.getString("leftFinger2"));

                                                if (fingers.has("leftFinger3"))
                                                    user_table.setF3(fingers.getString("leftFinger3"));

                                                if (fingers.has("rightFinger2"))
                                                    user_table.setF4(fingers.getString("rightFinger2"));

                                                if (fingers.has("rightThumb"))
                                                    user_table.setFprt(fingers.getString("rightThumb"));

                                                if (fingers.has("rightIndex"))
                                                    user_table.setFpri(fingers.getString("rightIndex"));

                                                if (fingers.has("leftFinger1"))
                                                    user_table.setFplf(fingers.getString("leftFinger1"));

                                                if (fingers.has("leftIndex"))
                                                    user_table.setFpli(fingers.getString("leftIndex"));

                                                if (fingers.has("leftThumb"))
                                                    user_table.setFplt(fingers.getString("leftThumb"));

                                                if (fingers.has("rightFinger1"))
                                                    user_table.setFprf(fingers.getString("rightFinger1"));

                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        usersList.add(user_table);
                                    }

                                    //don't move from bottom because if parsing successful then only this data override
                                    //save data for offline
                                    getDataManager().getDaoSession().getConfigurableParametersDao().deleteAll();
                                    getDataManager().getDaoSession().getConfigurableParametersDao().insert(parameters);

                                    getDataManager().getDaoSession().getUsersDao().deleteAll();
                                    getDataManager().getDaoSession().getUsersDao().insertInTx(usersList);
                                }

                                //insert languages
                                if (object.has("multiLanguageList")) {
                                    if (!object.isNull("multiLanguageList")) {
                                        List<Language> languages = new ArrayList<>();
                                        JSONArray array = object.getJSONArray("multiLanguageList");
                                        for (int i = 0; i < array.length(); i++) {
                                            Language language = new Language();
                                            language.setLangName(array.getString(i));
                                            language.setLocalisationTitle(array.getString(i));
                                            language.setIsSelected(false);
                                            languages.add(language);
                                        }

                                        getDataManager().getDaoSession().getLanguageDao().deleteAll();
                                        getDataManager().getDaoSession().getLanguageDao().insertInTx(languages);
                                    }
                                }
                                details.setUser(user.getJSONObject(0).getString("userName"));
                                details.setLevel(user.getJSONObject(0).getString("level"));
                                details.setPassword(user.getJSONObject(0).getString("password"));
                                details.setAgentId(user.getJSONObject(0).getString("agentId"));
                                details.setUid(user.getJSONObject(0).getString("userId"));
                                details.setLocationId(user.getJSONObject(0).getString("locationId"));
                                if (object.has("locationName"))
                                    details.setLocationName(object.getString("locationName"));
                                if (object.has("ben_id_level"))
                                    details.setBenIdLevel(object.getString("ben_id_level"));
                                getDataManager().setUserDetail(details);
                                              /*  getDataManager().setFirstTimeStatus(false);
                                                getDataManager().setLoggedIn(false);*/
                                dismissDialog();
                                getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.DataUpdated).setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    getMvpView().openNextActivity();
                                }).show();
                            }

                        } else {
                            dismissDialog();
                            getMvpView().showMessage(R.string.noUsers);
                        }

                    } else {
                        dismissDialog();
                        assert response.errorBody() != null;
                        handleApiError(response.errorBody().string());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    dismissDialog();
                    getMvpView().showMessage(e.toString());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                dismissDialog();
                handleApiFailure(call, t);
            }
        });

    }

    @Override
    public void CardHolders() {
        materialDialog = null;
        showDialog(context.getString(R.string.downloadingBeneficiary), context.getString(R.string.please_wait));
        doDownloadBeneficiary(offset);
    }

    @Override
    public void Topups() {
        materialDialog = null;
        showDialog(context.getString(R.string.downloadTopup), context.getString(R.string.zeroPercent));
        doDownloadTopup(topup_offset);
    }


    private void doInsertTopup(Response<ResponseBody> response) {
        try {
            assert response.body() != null;
            List<Topups> topupsList = new ArrayList<>();
            JSONObject object = new JSONObject(response.body().string());
            if (object.length() > 0) {
                long totalElements = object.getLong("totalElements");
                JSONArray topup = object.getJSONArray("result");
                if (topup.length() > 0) {
                    if (topup_offset == 1) {
                        List<Topups> topupsList1 = getDataManager().getDaoSession().getTopupsDao().queryBuilder().where(TopupsDao.Properties.EndDate.lt(CalenderUtils.getTimestampInDate(CalenderUtils.DATE_FORMAT))).list();
                        getDataManager().getDaoSession().getTopupsDao().deleteInTx(topupsList1);
                    }
                    for (int i = 0; i < topup.length(); i++) {
                        Topups topups = new Topups();
                        topups.setProgrammeId(topup.getJSONObject(i).getString("programmeId"));
                        topups.setBeneficiaryId(topup.getJSONObject(i).getString("beneficiaryId"));
                        topups.setCardNumber(topup.getJSONObject(i).getString("cardNumber"));
                        topups.setVoucherId(topup.getJSONObject(i).getString("voucherId"));
                        topups.setVoucherValue(topup.getJSONObject(i).getString("productPrice"));
                        topups.setStartDate(new Date(topup.getJSONObject(i).getLong("startDate")));
                        topups.setEndDate(new Date(topup.getJSONObject(i).getLong("endDate")));
                        topups.setVocherIdNo(topup.getJSONObject(i).getString("voucherIdNumber"));
                        topupsList.add(topups);
                    }
                    getDataManager().getDaoSession().getTopupsDao().insertOrReplaceInTx(topupsList);
                    long count = getDataManager().getDaoSession().getTopupsDao().count();
                    if (count >= totalElements) {
                        handler.post(this::alertSuccessToserver);
                    } else {
                        double val = count / Double.parseDouble("" + totalElements);
                        String desc = context.getString(R.string.Received).concat(" : ").concat(String.valueOf(count)).concat(" ").concat(context.getString(R.string.Expected)).concat(" :  ").concat(String.valueOf(totalElements)).concat(" ").concat(context.getString(R.string.Progress)).concat("  : " + ((int) (val * 100)) + "%");
                        showDialog(context.getString(R.string.downloadTopup), desc);
                        topup_offset++;
                        doDownloadTopup(topup_offset);
                    }
                } else {
                    handler.post(() -> {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.Notopupsdownloaded));
                    });
                }

            } else {
                handler.post(() -> {
                    dismissDialog();
                    getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.Notopupsdownloaded));
                });
            }
        } catch (Exception e) {
            handler.post(() -> {
                dismissDialog();
                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
            });
        }
    }


    private void doDownloadTopup(int offset) {
        try {
            JSONObject object = new JSONObject();
            object.put("serialNo", getDataManager().getDeviceId());
            object.put("page", offset);
            object.put("size", AppConstants.DOWNLOAD_LIMIT);

            RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());

            getDataManager().GetTopupPageable("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {

                    if (response.code() == 200) {
                        new Thread(() -> doInsertTopup(response)).start();
                    } else {
                        dismissDialog();
                        try {
                            assert response.body() != null;
                            JSONObject object = new JSONObject(Objects.requireNonNull(response.errorBody()).string());
                            if (object.has("message"))
                                getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                            else
                                getMvpView().showMessage(R.string.ServerError);
                        } catch (Exception e) {
                            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                        }
                    }

                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    dismissDialog();
                    handleApiFailure(call, t);
                }
            });
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }

    }

    private void alertSuccessToserver() {
        try {
            JSONObject object = new JSONObject();
            object.put("serialNo", getDataManager().getDeviceId());
            object.put("status", "200");
            RequestBody body = AppUtils.createBody(AppConstants.CONTENT_TYPE_JSON, object.toString());
            getDataManager().GetTopupPageable("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        alertsuccess();
                    } else {
                        dismissDialog();
                        try {
                            assert response.body() != null;
                            JSONObject object = new JSONObject(Objects.requireNonNull(response.errorBody()).string());
                            if (object.has("message"))
                                getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                            else
                                getMvpView().showMessage(R.string.ServerError);
                        } catch (Exception e) {
                            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    dismissDialog();
                    handleApiFailure(call, t);
                }
            });
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }
    }

    private void doDownloadBeneficiary(int page) {
        try {
            if (page == 0) {
                getDataManager().getDaoSession().getBeneficiaryDao().deleteAll();
                getDataManager().getDaoSession().getBeneficiaryBioDao().deleteAll();
                showDialog(context.getString(R.string.downloadingBeneficiary), context.getString(R.string.zeroPercent));
            }
            HashMap<String, String> map = new HashMap<>();
            map.put("page", String.valueOf(page));
            map.put("size", String.valueOf(AppConstants.DOWNLOAD_LIMIT));
            map.put("serialNo", getDataManager().getDeviceId());

            getDataManager().GetBeneficiaryPageable("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), map).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if (response.code() == 200) {
                        new Thread(() -> doInsertBnf(response)).start();
                    } else {
                        dismissDialog();
                        try {
                            assert response.body() != null;
                            JSONObject object = new JSONObject(Objects.requireNonNull(response.errorBody()).string());
                            if (object.has("message"))
                                getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));
                            else
                                getMvpView().showMessage(R.string.alert, R.string.ServerError);

                        } catch (Exception e) {
                            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    dismissDialog();
                    handleApiFailure(call, t);
                }
            });

        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage("Exception : ", e.getMessage());
        }
    }


    private void doInsertBnf(Response<ResponseBody> response) {
        try {
            assert response.body() != null;
            JSONObject object = new JSONObject(response.body().string());
            if (object.length() > 0) {
                List<Beneficiary> bnfList = new ArrayList<>();
                List<BeneficiaryBio> beneficiaryBioList = new ArrayList<>();
                long totalElements = object.getLong("totalElements");
                JSONArray members = object.getJSONArray("bnf");
                if (members.length() > 0) {
                    for (int i = 0; i < members.length(); i++) {
                        Beneficiary beneficiary_table = new Beneficiary();
                        beneficiary_table.setFirstName(members.getJSONObject(i).getString("firstName"));
                        beneficiary_table.setIdentityNo(members.getJSONObject(i).getString("idPassPortNo").trim());
                        if (members.getJSONObject(i).has("physicalAdd"))  //houseNumber
                            beneficiary_table.setAddress(members.getJSONObject(i).getString("physicalAdd"));  //houseNumber
                        if (members.getJSONObject(i).has("branchId"))
                            beneficiary_table.setSectionName(members.getJSONObject(i).getString("branchId"));

                        beneficiary_table.setBeneficiaryId(members.getJSONObject(i).getString("beneficiaryId"));
                        beneficiary_table.setGender(members.getJSONObject(i).getString("gender"));
                        beneficiary_table.setDateOfBirth(members.getJSONObject(i).getString("dateOfBirth"));
                        beneficiary_table.setActivation("0");
                        beneficiary_table.setId(Long.parseLong(members.getJSONObject(i).getString("beneficiaryId")));
                        beneficiary_table.setCardPin(members.getJSONObject(i).getString("cardPin"));
                        beneficiary_table.setMobile(members.getJSONObject(i).getString("cellPhone"));
                        beneficiary_table.setIsUploaded("1");
                        beneficiary_table.setCardNumber(members.getJSONObject(i).getString("cardNumber"));
                        if (members.getJSONObject(i).has("cardSerialNumber"))
                            beneficiary_table.setCardSerialNumber(members.getJSONObject(i).getString("cardSerialNumber"));

                        if (members.getJSONObject(i).has("bioVerifyStatus"))
                            beneficiary_table.setBioVerifyStatus(members.getJSONObject(i).getString("bioVerifyStatus"));
                        try {
                            if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
                                beneficiary_table.setBio(members.getJSONObject(i).getBoolean("bioStatus"));

                                if (members.getJSONObject(i).getBoolean("bioStatus")) {
                                    beneficiary_table.setBio(members.getJSONObject(i).getBoolean("bioStatus"));
                                    try {
                                        beneficiary_table.setImage(members.getJSONObject(i).getString("image"));
                                    } catch (Exception e) {
                                        getMvpView().showMessage(e.getMessage());
                                    }
                                    BeneficiaryBio fingerprints = new BeneficiaryBio();
                                    fingerprints.setBeneficiaryId(members.getJSONObject(i).getString("idPassPortNo").trim());
                                    JSONObject fingers;
                                    fingers = members.getJSONObject(i).getJSONObject("FingerPrints");
                                    try {
                                        if (fingers.has("rightFinger3"))
                                            fingerprints.setF4(fingers.getString("rightFinger3"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("leftFinger2"))
                                            fingerprints.setF1(fingers.getString("leftFinger2"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("leftFinger3"))
                                            fingerprints.setF2(fingers.getString("leftFinger3"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("rightFinger2"))
                                            fingerprints.setF3(fingers.getString("rightFinger2"));

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("rightThumb"))
                                            fingerprints.setFprt(fingers.getString("rightThumb"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("rightIndex"))
                                            fingerprints.setFpri(fingers.getString("rightIndex"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("leftFinger1"))
                                            fingerprints.setFplf(fingers.getString("leftFinger1"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("leftIndex"))
                                            fingerprints.setFpli(fingers.getString("leftIndex"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("leftThumb"))
                                            fingerprints.setFplt(fingers.getString("leftThumb"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        if (fingers.has("rightFinger1"))
                                            fingerprints.setFprf(fingers.getString("rightFinger1"));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    beneficiaryBioList.add(fingerprints);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        bnfList.add(beneficiary_table);
                    }
                    getDataManager().getDaoSession().getBeneficiaryDao().insertOrReplaceInTx(bnfList);
                    getDataManager().getDaoSession().getBeneficiaryBioDao().insertOrReplaceInTx(beneficiaryBioList);
                    long bnfCount = getDataManager().getDaoSession().getBeneficiaryDao().count();
                    if (bnfCount >= totalElements) {
                        handler.post(() -> {
                            dismissDialog();
                            alertsuccess();
                        });
                    } else {
                        double val = bnfCount / Double.parseDouble("" + totalElements);
                        String desc = context.getString(R.string.Received).concat(" : ").concat(String.valueOf(bnfCount)).concat(" ").concat(context.getString(R.string.Expected)).concat(" :  ").concat(String.valueOf(totalElements)).concat(" ").concat(context.getString(R.string.Progress)).concat("  : " + ((int) (val * 100)) + "%");
                        showDialog(context.getString(R.string.downloadingBeneficiary), desc);
                        offset++;
                        doDownloadBeneficiary(offset);
                    }
                } else {
                    handler.post(() -> {
                        dismissDialog();
                        getMvpView().showMessage(R.string.alert, R.string.txt_no_benf);
                    });
                }
            }
        } catch (Exception e) {
            handler.post(() -> {
                dismissDialog();
                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
            });
        }
    }

    private void alertsuccess() {
        dismissDialog();
        getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.data_updated).show();
    }


    private boolean checkForTransactions() {
        long ts = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().count();
        long transactionListProducts = getDataManager().getDaoSession().getTransactionListProductsDao().count();
        return ts == 0 && transactionListProducts == 0;
    }

    private boolean checkForBeneficiary() {
        long beneficiaries = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IsUploaded.eq("0")).count();
        return beneficiaries == 0;
    }

    private boolean checkForAgents() {
        long count = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.Isuploaded.eq("0")).count();
        return count == 0;
    }

    private void dismissDialog() {
        getMvpView().hideLoading();
        if (materialDialog != null) {
            materialDialog.dismiss();
        }
    }

    private void showDialog(String title, String desc) {
        handler.post(() -> {
            if (materialDialog == null) {
                materialDialog = getMvpView().materialDialog(title, desc);
                materialDialog.setCancelable(false);
                materialDialog.show();
            } else {
                materialDialog.setContent(desc);
            }
        });
    }
}
