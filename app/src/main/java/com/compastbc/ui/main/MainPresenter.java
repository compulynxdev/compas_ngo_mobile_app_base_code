package com.compastbc.ui.main;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.ActivityLog;
import com.compastbc.core.data.db.model.AttendanceLog;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryBioDao;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.SyncLogs;
import com.compastbc.core.data.db.model.TopupLogs;
import com.compastbc.core.data.db.model.TransactionListProducts;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.network.model.HomeBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class MainPresenter<V extends MainMvpView> extends BasePresenter<V>
        implements MainMvpPresenter<V> {

    private MaterialDialog materialDialog;
    private final Context context;
    private final GsonBuilder gsonBuilder;
    private final Handler handler = new Handler(Looper.getMainLooper());

    MainPresenter(DataManager dataManager, Context context) {
        super(dataManager);
        this.context = context;
        gsonBuilder = new GsonBuilder();
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }


    @Override
    public List<HomeBean> getHomeOptions() {
        List<HomeBean> homeList = new ArrayList<>();

        //level 2 means (Pos Admin or Master Vendor)

        //AppLogger.e("Test", getDataManager().getUserDetail().getLevel());
        homeList.add(new HomeBean(AppConstants.TRANSACTION_VIEW, R.drawable.ic_txns, context.getString(R.string.Transactions)));

        if (!getDataManager().getConfigurableParameterDetail().isOnline())
            homeList.add(new HomeBean(AppConstants.UPDATE_VIEW, R.drawable.ic_update, context.getString(R.string.Update)));

        homeList.add(new HomeBean(AppConstants.BENEFICIARY_VIEW, R.drawable.ic_beneficiary, context.getString(R.string.Beneficiary)));

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2"))
            homeList.add(new HomeBean(AppConstants.CARD_ACTIVATION_VIEW, R.drawable.ic_debit_card, context.getString(R.string.CardActivation)));

        homeList.add(new HomeBean(AppConstants.CARD_RESTORE_VIEW, R.drawable.ic_restore, context.getString(R.string.title_card_restore)));

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2"))
            homeList.add(new HomeBean(AppConstants.CHANGE_CARD_PIN_VIEW, R.drawable.ic_credit_card, context.getString(R.string.ChangeCardPin)));

        homeList.add(new HomeBean(AppConstants.CARD_BALANCE_VIEW, R.drawable.ic_id_card, context.getString(R.string.CardBalance)));

        if (getDataManager().getConfigurableParameterDetail().isVoidTransaction()) {
            homeList.add(new HomeBean(AppConstants.VOID_TRANSACTION_VIEW, R.drawable.ic_void, context.getString(R.string.VoidTransaction)));
        }

        homeList.add(new HomeBean(AppConstants.CHANGE_AGENT_PWD_VIEW, R.drawable.ic_personal_information, context.getString(R.string.ChangeAgentPassword)));
        homeList.add(new HomeBean(AppConstants.SETTINGS_VIEW, R.drawable.ic_settings, context.getString(R.string.Settings)));

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2"))
            homeList.add(new HomeBean(AppConstants.FORMAT_CARD_VIEW, R.drawable.ic_format, context.getString(R.string.FormatCard)));

        if (!getDataManager().getConfigurableParameterDetail().isOnline())
            homeList.add(new HomeBean(AppConstants.SYNC_VIEW, R.drawable.ic_sync, context.getString(R.string.SYNC)));

        homeList.add(new HomeBean(AppConstants.REPORTS_VIEW, R.drawable.ic_report, context.getString(R.string.Reports)));
        return homeList;
    }

    @Override
    public void uploadTransactions(boolean onClick) {
        if (getMvpView().isNetworkConnected(onClick)) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadingTxns, R.string.please_wait);
                materialDialog.show();
            } else {
                materialDialog = getMvpView().materialDialog(R.string.auto_sync, R.string.please_wait);
                materialDialog.setCancelable(false);
                materialDialog.show();
            }
            //for the first call we need access token
            getMvpView().getAccessToken(() -> {
                long count = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().where(TransactionsDao.Properties.Submit.eq("0")).count();
                if (count == 0) {
                    populateTransactionsAndUpload(onClick, file -> {
                        MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_transactions.zip", "zip", file);
                        getDataManager().UploadTransactions("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.code() == 200) {
                                    if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                                        getDataManager().getDaoSession().getTransactionsDao().deleteAll();
                                        getDataManager().getDaoSession().getCommoditiesDao().deleteAll();
                                        if (onClick) {
                                            dismissDialog();
                                            getMvpView().sweetAlert(2, context.getString(R.string.success), context.getString(R.string.uploadedTransactions)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
                                        } else uploadArchiveTransactions(onClick);
                                    }
                                } else {
                                    try {
                                        dismissDialog();
                                        assert response.errorBody() != null;
                                        handleApiError(response.errorBody().string());
                                    } catch (Exception e) {
                                        getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                dismissDialog();
                                getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                            }
                        });
                    });
                } else if (onClick) {
                    dismissDialog();
                    getMvpView().sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.submitBeforeUpload).setConfirmButton(R.string.Ok, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        getMvpView().showSubmitReport();
                    }).show();
                } else {
                    //automated sync
                    uploadArchiveTransactions(onClick);
                }
            });
        }
    }

    private void populateTransactionsAndUpload(boolean onClick, PopulateDataCallback callback) {
        new Thread(() -> {
            List<Transactions> transactions = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().list();
            if (!transactions.isEmpty()) {
                try {
                    //long  start = System.currentTimeMillis();
                    JsonSerializer<Transactions> serializerTxn = (tmpTxn, typeOfSrc, context) -> {
                        JsonObject jsonObject = new JsonObject();

                        jsonObject.addProperty("voucher", tmpTxn.getVoucherIdNo());
                        jsonObject.addProperty("transaction_type", tmpTxn.getTransactionType());
                        jsonObject.addProperty("cancelled_transaction", 0);
                        jsonObject.addProperty("rationNo", tmpTxn.getIdentityNo());
                        jsonObject.addProperty("receipt_number", tmpTxn.getReceiptNo());
                        jsonObject.addProperty("value_remaining", tmpTxn.getTotalValueRemaining());
                        jsonObject.addProperty("total_amount_charged_by_retailer", String.valueOf(tmpTxn.getTotalAmountChargedByRetail()));
                        jsonObject.addProperty("user", tmpTxn.getUser());
                        jsonObject.addProperty("locationId", tmpTxn.getLocationId());
                        jsonObject.addProperty("cardNumber", tmpTxn.getCardNo());
                        jsonObject.addProperty("programCurrency", tmpTxn.getProgramCurrency());
                        jsonObject.addProperty("timestamp_transaction_created", tmpTxn.getTimeStamp());
                        jsonObject.addProperty("programId", tmpTxn.getProgramId());
                        jsonObject.addProperty("agentId", tmpTxn.getAgentId());
                        jsonObject.addProperty("latitude", tmpTxn.getLatitude());
                        jsonObject.addProperty("startDate", tmpTxn.getTopupStartDate().getTime());
                        jsonObject.addProperty("endDate", tmpTxn.getTopupEndDate().getTime());
                        jsonObject.addProperty("longitude", tmpTxn.getLongitude());
                        jsonObject.addProperty("authentication_type", 0);
                        jsonObject.addProperty("pos_terminal", tmpTxn.getDeviceId());

                        List<Commodities> commoditiesList = getDataManager().getDaoSession().getCommoditiesDao().queryBuilder().where(CommoditiesDao.Properties.TransactionNo.eq(tmpTxn.getReceiptNo())).list();

                        jsonObject.add("commodities", getTxnCommodityObject(commoditiesList));
                        return jsonObject;
                    };
                    gsonBuilder.registerTypeAdapter(Transactions.class, serializerTxn);
                    Gson customGson = gsonBuilder.create();
                    File file = createFile("upload_transactions", customGson.toJson(transactions), null);

                    /*long end = System.currentTimeMillis();
                    AppLogger.e("TimeCalculation: ",(end - start) / 1000f + " seconds");*/

                    handler.post(() -> callback.onSuccess(file));
                } catch (Exception e) {
                    handler.post(() -> {
                        dismissDialog();
                        getMvpView().showMessage(e.toString());
                    });
                }
            } else {
                handler.post(() -> {
                    if (onClick) {
                        dismissDialog();
                        getMvpView().showMessage(R.string.noTxns);
                    } else {
                        //automated sync
                        uploadArchiveTransactions(onClick);
                    }
                });
            }
        }).start();
    }

    private JsonElement getTxnCommodityObject(List<Commodities> commoditiesList) {
        JsonSerializer<Commodities> serializerCommodity = (tmpCommodity, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("pos_commodity", tmpCommodity.getProductId());
            jsonObject.addProperty("uom", tmpCommodity.getUom());
            jsonObject.addProperty("maxPrice", tmpCommodity.getMaxPrice());
            jsonObject.addProperty("uniqueId", tmpCommodity.getUniqueId());
            jsonObject.addProperty("transactionNo", tmpCommodity.getTransactionNo());
            jsonObject.addProperty("quantity_remaining", "0");
            jsonObject.addProperty("amount_charged_by_retailer", String.valueOf(tmpCommodity.getTotalAmountChargedByRetailer()));
            jsonObject.addProperty("deducted_quantity", tmpCommodity.getQuantityDeducted());
            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(Commodities.class, serializerCommodity);
        Gson customGson = gsonBuilder.create();

        return customGson.toJsonTree(commoditiesList);
    }

    @Override
    public void uploadArchiveTransactions(boolean onClick) {
        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadingArchives, R.string.please_wait);
                materialDialog.show();
                getMvpView().getAccessToken(() -> doUploadArchiveTransactions(onClick));
            } else {
                //automated sync
                doUploadArchiveTransactions(onClick);
            }
        }
    }

    private void doUploadArchiveTransactions(boolean onClick) {
        populateArchiveTransactionsAndUpload(onClick, file -> {
            MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_archive_txns.zip", "zip", file);
            getDataManager().UploadArchiveLogs("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                @Override
                public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                    if (response.code() == 200) {
                        if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                            getDataManager().getDaoSession().getTransactionListProductsDao().deleteAll();
                            if (onClick) {
                                dismissDialog();
                                getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.archiveUploaded)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
                            } else uploadTopupLogs(onClick);
                        }
                    } else {
                        try {
                            assert response.errorBody() != null;
                            dismissDialog();
                            JSONObject object = new JSONObject(response.errorBody().string());
                            if (object.has("message"))
                                getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                            else
                                getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                        } catch (Exception e) {
                            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                    dismissDialog();
                    getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                }
            });
        });
    }

    private void populateArchiveTransactionsAndUpload(boolean onClick, PopulateDataCallback callback) {
        new Thread(() -> {
            List<TransactionListProducts> t_archives = getDataManager().getDaoSession().getTransactionListProductsDao().queryBuilder().list();
            if (!t_archives.isEmpty()) {
                JsonObject archives_obj = new JsonObject();
                try {
                    archives_obj.addProperty("masterDeviceId", getDataManager().getDeviceId());

                    JsonSerializer<TransactionListProducts> serializerTxn = (tmpTxnListProduct, typeOfSrc, context) -> {
                        JsonObject jsonObject = new JsonObject();

                        jsonObject.addProperty("serviceId", tmpTxnListProduct.getProductId());
                        jsonObject.addProperty("uom", tmpTxnListProduct.getUnitOfMeasure());
                        jsonObject.addProperty("quantity", tmpTxnListProduct.getQuantity());
                        jsonObject.addProperty("value", tmpTxnListProduct.getVal());
                        jsonObject.addProperty("transactiono", tmpTxnListProduct.getTransactionNo());
                        jsonObject.addProperty("deviceId", tmpTxnListProduct.getDeviceId());
                        jsonObject.addProperty("programmeid", tmpTxnListProduct.getProgramId());
                        jsonObject.addProperty("transactionDate", tmpTxnListProduct.getTransactionDate());

                        return jsonObject;
                    };
                    gsonBuilder.registerTypeAdapter(TransactionListProducts.class, serializerTxn);
                    Gson customGson = gsonBuilder.create();
                    archives_obj.add("transList", customGson.toJsonTree(t_archives));

                    File file = createFile("upload_archive_txns", null, customGson.toJson(archives_obj));

                    handler.post(() -> callback.onSuccess(file));
                } catch (Exception e) {
                    handler.post(() -> {
                        dismissDialog();
                        getMvpView().showMessage(e.toString());
                    });
                }
            } else {
                handler.post(() -> {
                    if (onClick) {
                        dismissDialog();
                        getMvpView().showMessage(R.string.alert, R.string.noarchive);
                    } else {
                        //automated sync
                        uploadTopupLogs(onClick);
                    }
                });
            }
        }).start();
    }

    @Override
    public void uploadTopupLogs(boolean onClick) {

        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadingTopups, R.string.please_wait);
                materialDialog.show();

                getMvpView().getAccessToken(() -> doUploadTopupLogs(onClick));
            } else {
                //automated sync
                doUploadTopupLogs(onClick);
            }
        }

    }

    private void doUploadTopupLogs(boolean onClick) {
        try {
            List<TopupLogs> tlogs = getDataManager().getDaoSession().getTopupLogsDao().queryBuilder().list();
            JSONObject tlogsobj = new JSONObject();
            tlogsobj.put("deviceId", getDataManager().getDeviceId());
            if (!tlogs.isEmpty()) {
                JSONArray logs = new JSONArray();
                for (int i = 0; i < tlogs.size(); i++) {
                    JSONObject obj = new JSONObject();
                    obj.put("cardNo", tlogs.get(i).getCardNo());
                    obj.put("oldVoucherIdNumber", tlogs.get(i).getOvoucherIdNo());
                    obj.put("newVoucherIdNumber", tlogs.get(i).getNvoucherIdNo());
                    obj.put("oldVoucherValue", tlogs.get(i).getOCardBal());
                    obj.put("newVoucherValue", tlogs.get(i).getNtopupValue());
                    obj.put("newCardBalance", tlogs.get(i).getNCardBal());
                    obj.put("topupDate", tlogs.get(i).getTopupTime());
                    obj.put("vendorDeviceId", tlogs.get(i).getDeviceIdNo());
                    obj.put("userName", tlogs.get(i).getUserName());
                    obj.put("programId", tlogs.get(i).getProgrammeId());
                    obj.put("refNo", tlogs.get(i).getRefNo());
                    logs.put(obj);
                }
                tlogsobj.put("topupLogDetails", logs);
                File file = createFile("upload_topups_log", null, tlogsobj.toString());
                MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_topups_log.zip", "zip", file);
                getDataManager().UploadTopupLogs("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                                getDataManager().getDaoSession().getTopupLogsDao().deleteAll();

                                if (onClick) {
                                    dismissDialog();
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.topupUploaded)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
                                } else {
                                    //automated sync
                                    uploadActivities(onClick);
                                }
                            }
                        } else {
                            try {
                                assert response.errorBody() != null;
                                dismissDialog();
                                JSONObject object = new JSONObject(response.errorBody().string());
                                if (object.has("message"))
                                    getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                                else
                                    getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                            } catch (Exception e) {
                                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                    }
                });
            } else {
                if (onClick) {
                    dismissDialog();
                    getMvpView().showMessage(R.string.alert, R.string.notopup);
                } else {
                    //automated sync
                    uploadActivities(onClick);
                }
            }
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }
    }

    @Override
    public void uploadActivities(boolean onClick) {

        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadingActivity, R.string.please_wait);
                materialDialog.show();

                getMvpView().getAccessToken(() -> doUploadActivities(onClick));
            } else {
                //automated sync
                doUploadActivities(onClick);
            }
        }

    }

    private void doUploadActivities(boolean onClick) {
        try {
            List<ActivityLog> activity = getDataManager().getDaoSession().getActivityLogDao().queryBuilder().list();
            JSONArray cs = new JSONArray();

            if (!activity.isEmpty()) {
                for (int i = 0; i < activity.size(); i++) {
                    JSONObject activityobject = new JSONObject();
                    activityobject.put("deviceId", activity.get(i).getDeviceId());
                    activityobject.put("locationMaster", Integer.parseInt(activity.get(i).getLocationId()));
                    activityobject.put("userName", activity.get(i).getUserName());
                    activityobject.put("latitude", activity.get(i).getLatitude());
                    activityobject.put("longitude", activity.get(i).getLongitude());
                    activityobject.put("date", activity.get(i).getDate());
                    activityobject.put("action", activity.get(i).getAction());
                    activityobject.put("activity", activity.get(i).getActivity());
                    cs.put(activityobject);
                }
                File file = createFile("upload_activity", cs.toString(), null);
                MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_activity.zip", "zip", file);
                getDataManager().UploadActivityLogs("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                                getDataManager().getDaoSession().getActivityLogDao().deleteAll();

                                if (onClick) {
                                    dismissDialog();
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.activityUploaded)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
                                } else {
                                    //automated sync
                                    uploadAttendance(onClick);
                                }
                            }
                        } else {
                            try {
                                assert response.errorBody() != null;
                                dismissDialog();
                                JSONObject object = new JSONObject(response.errorBody().string());
                                if (object.has("message"))
                                    getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                                else
                                    getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                            } catch (Exception e) {
                                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                    }
                });

            } else {
                if (onClick) {
                    dismissDialog();
                    getMvpView().showMessage(R.string.alert, R.string.noactivities);
                } else {
                    //automated sync
                    uploadAttendance(onClick);
                }
            }
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }
    }

    @Override
    public void uploadAttendance(boolean onClick) {
        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadingAttendance, R.string.please_wait);
                materialDialog.show();
                getMvpView().getAccessToken(() -> doUploadAttendance(onClick));
            } else {
                //automated sync
                doUploadAttendance(onClick);
            }
        }
    }

    private void doUploadAttendance(boolean onClick) {
        try {
            List<AttendanceLog> attendance = getDataManager().getDaoSession().getAttendanceLogDao().queryBuilder().list();
            JSONArray cs = new JSONArray();
            if (!attendance.isEmpty()) {
                for (int i = 0; i < attendance.size(); i++) {
                    JSONObject attendanceobject = new JSONObject();
                    attendanceobject.put("userName", attendance.get(i).getUsername());
                    attendanceobject.put("deviceId", attendance.get(i).getDeviceId());
                    attendanceobject.put("latitude", attendance.get(i).getLatitude());
                    attendanceobject.put("longitude", attendance.get(i).getLongitude());
                    attendanceobject.put("loginSuccess", attendance.get(i).isLoginSuccess());
                    attendanceobject.put("locationMaster", Integer.parseInt(attendance.get(i).getLocationId()));
                    attendanceobject.put("loginDate", attendance.get(i).getLoginDate());
                    cs.put(attendanceobject);
                }
                File file = createFile("upload_attendance", cs.toString(), null);
                MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_attendance.zip", "zip", file);
                getDataManager().UploadAttendanceLogs("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                                getDataManager().getDaoSession().getAttendanceLogDao().deleteAll();
                                if (onClick) {
                                    dismissDialog();
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.attendanceUploaded)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
                                } else {
                                    //automated sync
                                    uploadAgents(onClick);
                                }
                            }
                        } else {
                            try {
                                assert response.errorBody() != null;
                                dismissDialog();
                                JSONObject object = new JSONObject(response.errorBody().string());
                                if (object.has("message"))
                                    getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                                else
                                    getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                            } catch (Exception e) {
                                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                    }
                });

            } else {
                if (onClick) {
                    dismissDialog();
                    getMvpView().showMessage(R.string.alert, R.string.noattendance);
                } else {
                    //automated sync
                    uploadAgents(onClick);
                }
            }
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }
    }

    @Override
    public void uploadAgents(boolean onClick) {
        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadAgentBiometric, R.string.please_wait);
                materialDialog.show();
                getMvpView().getAccessToken(() -> doUploadAgents(onClick));
            } else {
                //automated sync
                doUploadAgents(onClick);
            }
        }

    }

    private void doUploadAgents(boolean onClick) {
        try {
            List<Users> users = getDataManager().getDaoSession().getUsersDao().queryBuilder().where(UsersDao.Properties.Isuploaded.eq("0")).list();
            JSONArray cs = new JSONArray();
            if (users.size() != 0) {
                for (int i = 0; i < users.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("agentId", users.get(i).getAgentId());
                    object.put("createdBy", users.get(i).getAgentId());
                    object.put("locationId", users.get(i).getLocationid());
                    object.put("deviceId", getDataManager().getDeviceId());
                    JSONObject fingerprint = new JSONObject();
                    fingerprint.put("leftFinger1", users.get(i).getFplf());
                    fingerprint.put("leftFinger2", users.get(i).getF1());
                    fingerprint.put("leftFinger3", users.get(i).getF2());
                    fingerprint.put("rightFinger1", users.get(i).getFplf());
                    fingerprint.put("rightFinger2", users.get(i).getF3());
                    fingerprint.put("rightFinger3", users.get(i).getF4());
                    fingerprint.put("leftIndex", users.get(i).getFpli());
                    fingerprint.put("leftThumb", users.get(i).getFplt());
                    fingerprint.put("rightIndex", users.get(i).getFpri());
                    fingerprint.put("rightThumb", users.get(i).getFprt());
                    object.put("FingerPrints", fingerprint);
                    cs.put(i, object);
                }
                File file = createFile("upload_agent", cs.toString(), null);
                MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_agent.zip", "zip", file);
                getDataManager().UploadAgents("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.code() == 200) {
                            if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                                for (int i = 0; i < users.size(); i++) {
                                    users.get(i).setIsuploaded("1");
                                    getDataManager().getDaoSession().getUsersDao().insertOrReplace(users.get(i));
                                }
                                if (onClick) {
                                    dismissDialog();
                                    getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.agentuploaded)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();
                                } else {
                                    //automated sync
                                    uploadBeneficiaries(onClick);
                                }
                            }
                        } else {
                            try {
                                assert response.errorBody() != null;
                                dismissDialog();
                                JSONObject object = new JSONObject(response.errorBody().string());
                                if (object.has("message"))
                                    getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                                else
                                    getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                            } catch (Exception e) {
                                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                    }
                });

            } else {
                if (onClick) {
                    dismissDialog();
                    getMvpView().showMessage(R.string.alert, R.string.noagents);
                } else {
                    //automated sync
                    uploadBeneficiaries(onClick);
                }
            }
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }
    }

    @Override
    public void uploadBeneficiaries(boolean onClick) {

        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uploadingBeneficiary, R.string.please_wait);
                materialDialog.show();

                getMvpView().getAccessToken(() -> doUploadBeneficiaries(onClick));
            } else {
                //automated sync
                doUploadBeneficiaries(onClick);
            }
        }

    }

    private void doUploadBeneficiaries(boolean onClick) {
        try {
            List<Beneficiary> beneficiaries = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IsUploaded.eq("0")).list();
            JSONArray cs = new JSONArray();
            if (!beneficiaries.isEmpty()) {
                for (int i = 0; i < beneficiaries.size(); i++) {
                    JSONObject object = new JSONObject();
                    object.put("active", true);
                    object.put("locationId", getDataManager().getUserDetail().getLocationId());
                    object.put("memberId", beneficiaries.get(i).getBeneficiaryId());
                    object.put("memberNo", beneficiaries.get(i).getIdentityNo());
                    object.put("idPassPortNo", beneficiaries.get(i).getIdentityNo());
                    object.put("from", "android");
                    if (beneficiaries.get(i).getLastName() != null && !beneficiaries.get(i).getLastName().isEmpty())
                        object.put("firstName", beneficiaries.get(i).getFirstName() .concat(" ").concat(beneficiaries.get(i).getLastName()));
                    else
                        object.put("firstName", beneficiaries.get(i).getFirstName());

                    object.put("lastName", beneficiaries.get(i).getLastName());
                    object.put("height", "1"); //household value
                    object.put("physicalAdd", beneficiaries.get(i).getAddress());
                    object.put("branchId", beneficiaries.get(i).getSectionName());
                    object.put("createdBy", beneficiaries.get(i).getAgentId());
                    object.put("gender", beneficiaries.get(i).getGender());
                    object.put("dateOfBirth", beneficiaries.get(i).getDateOfBirth());
                    object.put("activation", beneficiaries.get(i).getActivation());
                    object.put("isUploaded", beneficiaries.get(i).getIsUploaded());
                    object.put("cardNumber", beneficiaries.get(i).getCardNumber());
                    object.put("agentId", beneficiaries.get(i).getAgentId());
                    object.put("deviceId", beneficiaries.get(i).getDeviceId());
                    object.put("cellPhone", beneficiaries.get(i).getMobile());
                    object.put("cardPin", beneficiaries.get(i).getCardPin());
                    //object.put("cardSerialNumber", beneficiaries.get(i).get());
                    object.put("cardSerialNumber", "12345678");
                    object.put("bioStatus", beneficiaries.get(i).getBio());
                    object.put("bioVerifyStatus", beneficiaries.get(i).getBioVerifyStatus());
                    object.put("benfImage", beneficiaries.get(i).getImage());
                    object.put("benfSignImage", beneficiaries.get(i).getSignature());
                    List<BeneficiaryBio> fingerprints = getDataManager().getDaoSession().getBeneficiaryBioDao().queryBuilder().where(BeneficiaryBioDao.Properties.BeneficiaryId.eq(beneficiaries.get(i).getIdentityNo())).list();
                    if (fingerprints.size() > 0) {
                        JSONObject fingerprint = new JSONObject();
                        fingerprint.put("leftFinger1", fingerprints.get(0).getFplf());
                        fingerprint.put("leftFinger2", fingerprints.get(0).getF1());
                        fingerprint.put("leftFinger3", fingerprints.get(0).getF2());
                        fingerprint.put("rightFinger1", fingerprints.get(0).getFprf());
                        fingerprint.put("rightFinger2", fingerprints.get(0).getF3());
                        fingerprint.put("rightFinger3", fingerprints.get(0).getF4());
                        fingerprint.put("leftIndex", fingerprints.get(0).getFpli());
                        fingerprint.put("leftThumb", fingerprints.get(0).getFplt());
                        fingerprint.put("rightIndex", fingerprints.get(0).getFpri());
                        fingerprint.put("rightThumb", fingerprints.get(0).getFprt());
                        object.put("FingerPrints", fingerprint);
                    }
                    cs.put(i, object);
                }
                File file = createFile("upload_benf", cs.toString(), null);
                MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_benf.zip", "zip", file);
                getDataManager().UploadBeneficiaries("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.code() == 200) {
                            try {
                                assert response.body() != null;
                                String responseStr = response.body().string();
                                JSONObject object = new JSONObject(responseStr);
                                AppLogger.e("object", object.toString());
                                if (object.length() > 0) {
                                    JSONArray memberArray = object.getJSONArray("memberList");
                                    for (int i = 0; i < memberArray.length(); i++) {
                                        Beneficiary beneficiary = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IdentityNo.eq(
                                                memberArray.getJSONObject(i).getString("identityNo")
                                        )).unique();
                                        if (beneficiary != null) {
                                            beneficiary.setBeneficiaryId(memberArray.getJSONObject(i).getString("memberId"));
                                            beneficiary.setIsUploaded("1");
                                            getDataManager().getDaoSession().getBeneficiaryDao().insertOrReplace(beneficiary);
                                        }
                                    }
                                    // for remaining upload beneficiaries
                                    List<Beneficiary> beneficiaryList = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder().where(BeneficiaryDao.Properties.IsUploaded.eq("0")).list();
                                    for (Beneficiary beneficiary : beneficiaryList) {
                                        beneficiary.setIsUploaded("1");
                                        getDataManager().getDaoSession().getBeneficiaryDao().insertOrReplace(beneficiary);
                                    }
                                    dismissDialog();
                                    if (onClick) {
                                        getMvpView().show(getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.benfuploaded)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation));
                                    } else {
                                        getMvpView().show(getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.deviceDataSuccessfullyUpload)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation));
                                    }
                                }
                            } catch (Exception e) {
                                dismissDialog();
                                getMvpView().showMessage(e.toString());
                            }
                        } else {
                            try {
                                assert response.errorBody() != null;
                                dismissDialog();
                                JSONObject object = new JSONObject(response.errorBody().string());
                                if (object.has("message"))
                                    getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                                else
                                    getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                            } catch (Exception e) {
                                getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                    }
                });

            } else {
                dismissDialog();
                if (onClick) {
                    getMvpView().showMessage(R.string.alert, R.string.nobenf);
                } else {
                    //automated sync uploaded
                    getMvpView().show(getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, context.getString(R.string.success), context.getString(R.string.deviceDataSuccessfullyUpload)).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation));
                }
            }
        } catch (Exception e) {
            dismissDialog();
            getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
        }
    }

    @Override
    public void uploadPendingSynchronisation(boolean onClick) {
        if (getMvpView().isNetworkConnected()) {
            if (onClick) {
                materialDialog = getMvpView().materialDialog(R.string.uplodPending, R.string.please_wait);
                materialDialog.show();
            }

            getMvpView().getAccessToken(() -> {
                List<SyncLogs> syncLogs = getDataManager().getDaoSession().getSyncLogsDao().queryBuilder().list();
                JSONArray array = new JSONArray();
                if (syncLogs.size() > 0) {

                    try {
                        for (int i = 0; i < syncLogs.size(); i++) {
                            JSONObject object = new JSONObject();

                            if (syncLogs.get(i).getSend_by() != null) {
                                object.put("sendBy", Integer.parseInt(syncLogs.get(i).getSend_by()));
                                object.put("sendByDeviceId", syncLogs.get(i).getSend_by_deviceId());
                                object.put("sendDate", syncLogs.get(i).getSend_date().getTime());
                            }
                            if (syncLogs.get(i).getReceived_by() != null) {
                                object.put("receivedBy", Integer.parseInt(syncLogs.get(i).getReceived_by()));
                                object.put("receivedByDeviceId", syncLogs.get(i).getReceived_deviceId());
                                object.put("receivedDate", syncLogs.get(i).getReceived_date().getTime());
                            }
                            object.put("startDate", syncLogs.get(i).getStartDate().getTime());
                            object.put("endDate", syncLogs.get(i).getEndDate().getTime());
                            object.put("reason", syncLogs.get(i).getReason());
                            object.put("programId", Integer.parseInt(syncLogs.get(i).getProgramId()));
                            object.put("totalTransaction", Integer.parseInt(syncLogs.get(i).getTotal_transaction()));
                            object.put("totalAmount", Double.parseDouble(syncLogs.get(i).getTotal_amount()));
                            object.put("status", syncLogs.get(i).getStatus());
                            if (syncLogs.get(i).getUpload_by() != null) {
                                object.put("uploadBy", Integer.parseInt(syncLogs.get(i).getUpload_by()));
                                object.put("uploadDeviceId", syncLogs.get(i).getUpload_deviceId());
                                object.put("uploadDate", syncLogs.get(i).getUpload_date().getTime());
                            }

                            array.put(object);
                        }

                        File file = createFile("upload_sync", array.toString(), null);
                        MultipartBody.Part body = AppUtils.prepareFilePart("file", "upload_sync.zip", "zip", file);

                        getDataManager().UploadPendingSyncLogs("bearer " + getDataManager().getConfigurationDetail().getAccess_token(), body).enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                if (response.code() == 200) {
                                    if (Objects.requireNonNull(response.body()).equalsIgnoreCase("200")) {
                                        getDataManager().getDaoSession().getSyncLogsDao().deleteAll();
                                        dismissDialog();
                                        getMvpView().sweetAlert(SweetAlertDialog.SUCCESS_TYPE, R.string.success, R.string.syncSuccess).setConfirmButton(R.string.Ok, SweetAlertDialog::dismissWithAnimation).show();

                                    }
                                } else {
                                    try {
                                        assert response.errorBody() != null;
                                        dismissDialog();
                                        JSONObject object = new JSONObject(response.errorBody().string());
                                        if (object.has("message"))
                                            getMvpView().showMessage(context.getString(R.string.alert), object.getString("message"));

                                        else
                                            getMvpView().showMessage(context.getString(R.string.alert), context.getString(R.string.ServerError));
                                    } catch (Exception e) {
                                        getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                                    }
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                dismissDialog();
                                getMvpView().showMessage(context.getString(R.string.alert), t.getMessage() == null ? context.getString(R.string.ServerError) : t.getMessage());
                            }
                        });

                    } catch (Exception e) {
                        dismissDialog();
                        getMvpView().showMessage(context.getString(R.string.alert), e.getMessage());
                    }
                } else {
                    dismissDialog();
                    getMvpView().showMessage(R.string.alert, R.string.noSyncLogs);
                }
            });
        }

    }

    @Override
    public void dismissDialog() {
        if (materialDialog != null)
            materialDialog.dismiss();
    }

    private File createFile(String fileName, String strArray, String strJsonObject) {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + fileName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            if (strArray != null)
                writeToFile(strArray, folder, fileName);

            else writeToFile(strJsonObject, folder, fileName);
        }

        final File file = new File(folder, fileName + ".zip");
        try {
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] s = new String[1];
        s[0] = folder.getAbsolutePath() + "/" + fileName + ".txt";
        zip(s, file.getAbsolutePath());
        return file;
    }

    private void writeToFile(String data, File folder, String fileName) {
        //Writing 10k data in : 0.742 seconds
        //create a file and write tha data

        //long  start = System.currentTimeMillis();
        FileWriter fr = null;
        BufferedWriter br = null;
        final File file = new File(folder, fileName + ".txt");
        try {
            fr = new FileWriter(file);
            br = new BufferedWriter(fr);

            br.write(data);

            /*FileOutputStream fOut = new FileOutputStream(file);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            myOutWriter.append(data);
            myOutWriter.close();
            fOut.flush();
            fOut.close();*/
        } catch (IOException e) {
            AppLogger.e("Exception", "File write failed: " + e.toString());
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                AppLogger.e("Exception", "Finally File write failed: " + e.toString());
            }
        }
        /*long end = System.currentTimeMillis();
        AppLogger.e("TimeCalculation: ",(end - start) / 1000f + " seconds");*/
    }

    private void zip(String[] _files, String zipFileName) {
        try {
            BufferedInputStream origin;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            int BUFFER = 6 * 1024;
            byte[] data = new byte[BUFFER];
            for (String file : _files) {
                AppLogger.d("Compress", "Adding: " + file);
                FileInputStream fi = new FileInputStream(file);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface PopulateDataCallback {
        void onSuccess(File file);
    }
}
