package com.compastbc.ui.synchronization;

import android.app.Activity;
import android.database.Cursor;
import android.os.Build;
import android.os.Environment;

import com.compastbc.Compas;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.ActivityLog;
import com.compastbc.core.data.db.model.AttendanceLog;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryBioDao;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.BlockCards;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.CommoditiesDao;
import com.compastbc.core.data.db.model.ExceptionLog;
import com.compastbc.core.data.db.model.TopupLogs;
import com.compastbc.core.data.db.model.Topups;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.db.model.TransactionListProducts;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.synchronization.util.Settings;
import com.compastbc.ui.beneficiary.BenfAuthEnum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializer;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Hemant Sharma on 31-10-19.
 * Divergent software labs pvt. ltd
 */
class SynchronizationData {

    private GsonBuilder gsonBuilder;
    private final Activity activity;
    private List<Transactions> transactions;
    private final Details userDetail;
    private final DataManager dataManager;

    private final SynchronizationDataCallback callback;

    SynchronizationData(DataManager dataManager, Activity activity, SynchronizationDataCallback callback) {
        this.dataManager = dataManager;
        this.activity = activity;
        this.callback = callback;
        userDetail = Compas.getInstance().getDataManager().getUserDetail();
        init();
    }

    private void init() {
        gsonBuilder = new GsonBuilder();
        JSONObject data = new JSONObject();
        try {
            if (userDetail.getLevel().equalsIgnoreCase("2")) {
                String topupsData = getTopUpFromDB();

                if (!topupsData.isEmpty()) {
                    JSONArray topupArray = new JSONArray(topupsData);
                    data.put("Topups", topupArray);
                }

                String cardBlockData = getCardBlockFromDB();
                if (!cardBlockData.isEmpty()) {
                    JSONArray cardBlockArray = new JSONArray(cardBlockData);
                    data.put("CardBlock", cardBlockArray);
                }

                //checks remove for bio and non bio
                //if (dataManager.getConfigurableParameterDetail().isBiometric()) {
                String bnfData = getBeneficiaryWithBioStatusFromDB();

                if (!bnfData.isEmpty()) {
                    JSONArray bnfJsonArray = new JSONArray(bnfData);
                    data.put("Beneficiaries", bnfJsonArray);
                }
                //}
            } else if (userDetail.getLevel().equalsIgnoreCase("1")) {

                String topupLogsData = getTopUpLogsFromDB();
                if (!topupLogsData.isEmpty()) {
                    JSONArray topUpsLogArray = new JSONArray(topupLogsData);
                    data.put("TopupsLogs", topUpsLogArray);
                }

                String attendanceLogsData = getAttendanceLogFromDB();
                if (!attendanceLogsData.isEmpty()) {
                    JSONArray attendanceLogArray = new JSONArray(attendanceLogsData);
                    data.put("AttendanceLog", attendanceLogArray);
                }

                String activityLogData = getActivityLogFromDB();
                if (!activityLogData.isEmpty()) {
                    JSONArray activityLogArray = new JSONArray(activityLogData);
                    data.put("ActivityLog", activityLogArray);
                }

                String transactionsListProductsData = getTransactionsListProductsFromDB();
                if (!transactionsListProductsData.isEmpty()) {
                    JSONArray txnListProductArray = new JSONArray(transactionsListProductsData);
                    data.put("ArchiveTransactions", txnListProductArray);
                }

                String transactionData = getTransactionsFromDB();
                if (!transactionData.isEmpty()) {
                    JSONArray txnArray = new JSONArray(transactionData);
                    data.put("Transactions", txnArray);
                }

                if (!transactions.isEmpty()) {
                    double amount;
                    int count;
                    //old query
                    //List<Topups> tps = Topups.find(Topups.class, null, null, "programmeid", null, null);

                    //GROUP by ProgrammeId
                    //List<Topups> tps = dataManager.getDaoSession().getTopupsDao().queryBuilder().orderAsc(TopupsDao.Properties.ProgrammeId).list();
                    Cursor cursor = dataManager.getDaoSession().getDatabase().rawQuery("SELECT * from " + TopupsDao.TABLENAME
                            + " GROUP BY " + TopupsDao.Properties.ProgrammeId.columnName, new String[]{});

                    List<Topups> tps = new ArrayList<>();
                    if (cursor.moveToFirst()) {
                        do {
                            Topups topups = new Topups();
                            topups.setProgrammeId(cursor.getString(cursor.getColumnIndexOrThrow(TopupsDao.Properties.ProgrammeId.columnName)));
                            topups.setStartDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(TopupsDao.Properties.StartDate.columnName))));
                            topups.setEndDate(new Date(cursor.getLong(cursor.getColumnIndexOrThrow(TopupsDao.Properties.EndDate.columnName))));
                            tps.add(topups);

                        } while (cursor.moveToNext());
                    }
                    if (!cursor.isClosed()) cursor.close();


                    JSONArray array = new JSONArray();
                    String programCurrency = "";
                    for (int i = 0; i < tps.size(); i++) {
                        JSONObject object = new JSONObject();
                        amount = 0.0;
                        count = 0;
                        Topups tmpTopupObj = tps.get(i);
                        for (int j = 0; j < transactions.size(); j++) {
                            Transactions tmpTxnObj = transactions.get(j);
                            if (tmpTopupObj.getProgrammeId().equalsIgnoreCase(tmpTxnObj.getProgramId())) {
                                programCurrency = tmpTxnObj.getProgramCurrency();
                                amount = amount + Double.parseDouble(tmpTxnObj.getTotalAmountChargedByRetail());
                                count++;
                            }
                        }
                        object.put("programId", tmpTopupObj.getProgrammeId());
                        object.put("startDate", tmpTopupObj.getStartDate().getTime());
                        object.put("endDate", tmpTopupObj.getEndDate().getTime());
                        object.put("programCurrency", programCurrency);
                        object.put("total_transaction", count);
                        object.put("total_amount", amount);
                        array.put(object);
                    }
                    data.put("Pending_Sync", array);
                }

                String exceptionLogsData = getExceptionsFromDB();
                if (!exceptionLogsData.isEmpty()) {
                    JSONArray exceptionsArray = new JSONArray(exceptionLogsData);
                    data.put("ExceptionLogs", exceptionsArray);
                }

                    //old query
                    //List<Beneficiary> beneficiaries = Beneficiary.find(Beneficiary.class, "isuploaded=?", "0");
                    List<Beneficiary> beneficiaries = dataManager.getDaoSession().getBeneficiaryDao().queryBuilder()
                            .where(BeneficiaryDao.Properties.IsUploaded.eq("0")).list();
                    JSONArray cs = new JSONArray();
                    int size = beneficiaries.size();
                    if (size > 0) {
                        for (int i = 0; i < size; i++) {
                            JSONObject object = new JSONObject();
                            Beneficiary tmpObj = beneficiaries.get(i);
                            object.put("active", true);
                            object.put("locationId", userDetail.getLocationId());
                            object.put("memberId", tmpObj.getBeneficiaryId());
                            object.put("idPassPortNo", tmpObj.getIdentityNo());
                            String name = tmpObj.getLastName();
                            object.put("firstName", tmpObj.getFirstName().concat(" ").concat(name == null ? "" : name).trim());
                            object.put("HouseNumber", tmpObj.getAddress());
                            object.put("branchId", tmpObj.getSectionName());
                            object.put("createdBy", tmpObj.getAgentId());
                            object.put("gender", tmpObj.getGender());
                            object.put("dateOfBirth", tmpObj.getDateOfBirth());
                            object.put("activation", tmpObj.getActivation());
                            object.put("isUploaded", tmpObj.getIsUploaded());
                            object.put("cardNumber", tmpObj.getCardNumber());
                            object.put("agentId", tmpObj.getAgentId());
                            object.put("deviceId", tmpObj.getDeviceId());
                            object.put("cellPhone", tmpObj.getMobile());
                            object.put("cardPin", tmpObj.getCardPin());
                            object.put("bioStatus", tmpObj.getBio());
                            object.put("bioVerifyStatus", tmpObj.getBioVerifyStatus());
                            object.put("cardSerialNumber",tmpObj.getCardSerialNumber());
                            object.put("benfImage", tmpObj.getImage());
                            //old query
                            //List<BeneficiaryBio> fingerprints = BeneficiaryBio.find(BeneficiaryBio.class, "beneficiaryid=?", tmpObj.getNational_id());
                            List<BeneficiaryBio> fingerprints = dataManager.getDaoSession().getBeneficiaryBioDao().queryBuilder()
                                    .where(BeneficiaryBioDao.Properties.BeneficiaryId.eq(tmpObj.getIdentityNo())).list();
                            if (fingerprints.size() > 0) {
                                JSONObject fingerprint = new JSONObject();
                                BeneficiaryBio tmpFpObj = fingerprints.get(0);
                                fingerprint.put("leftFinger1", tmpFpObj.getFplf());
                                fingerprint.put("leftFinger2", tmpFpObj.getF1());
                                fingerprint.put("leftFinger3", tmpFpObj.getF2());
                                fingerprint.put("rightFinger1", tmpFpObj.getFprf());
                                fingerprint.put("rightFinger3", tmpFpObj.getF4());
                                fingerprint.put("leftIndex", tmpFpObj.getFpli());
                                fingerprint.put("leftThumb", tmpFpObj.getFplt());
                                fingerprint.put("rightFinger2", tmpFpObj.getF3());
                                fingerprint.put("rightIndex", tmpFpObj.getFpri());
                                fingerprint.put("rightThumb", tmpFpObj.getFprt());
                                object.put("fingerPrint", fingerprint);
                            }
                            cs.put(i, object);
                        }
                        data.put("Beneficiaries", cs);
                    }
                //check validate for bio
                if (dataManager.getConfigurableParameterDetail().isBiometric()) {

                    //old query
                    //List<Users> users = Users.find(Users.class, "isuploaded = ?", "0");
                    List<Users> users = dataManager.getDaoSession().getUsersDao().queryBuilder()
                            .where(UsersDao.Properties.Isuploaded.eq("0")).list();
                    if (users.size() > 0) {
                        cs = new JSONArray();
                        for (int i = 0; i < users.size(); i++) {
                            JSONObject object = new JSONObject();
                            Users tmpObj = users.get(i);
                            object.put("agentId", tmpObj.getAgentId());
                            object.put("createdBy", tmpObj.getAgentId());
                            object.put("userName", tmpObj.getUsername());
                            object.put("userId", tmpObj.getUsersId());
                            object.put("password", tmpObj.getPassword());
                            object.put("bioStatus", tmpObj.getBio());
                            object.put("level", tmpObj.getLevel());
                            object.put("locationId", tmpObj.getLocationid());
                            object.put("deviceId", dataManager.getDeviceId());
                            JSONObject fingerprint = new JSONObject();
                            fingerprint.put("leftFinger1", tmpObj.getFplf());
                            fingerprint.put("leftFinger2", tmpObj.getF1());
                            fingerprint.put("leftFinger3", tmpObj.getF2());
                            fingerprint.put("rightFinger1", tmpObj.getFprf());
                            fingerprint.put("rightFinger2", tmpObj.getF3());
                            fingerprint.put("rightFinger3", tmpObj.getF4());
                            fingerprint.put("leftIndex", tmpObj.getFpli());
                            fingerprint.put("leftThumb", tmpObj.getFplt());
                            fingerprint.put("rightIndex", tmpObj.getFpri());
                            fingerprint.put("rightThumb", tmpObj.getFprt());
                            object.put("fingerPrint", fingerprint);
                            cs.put(i, object);
                        }
                        data.put("Agents", cs);
                    }
                }
            }
            if (data.length() <= 0) {
                if (callback != null) callback.onNoDataFound();
            } else {
                InetAddress inetAddress = Settings.getInetAddress();
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("inet", inetAddress == null ? "" : inetAddress.getHostAddress());
                tmpObj.put("port", AppConstants.PORT);
                tmpObj.put("name", Build.MODEL);
                tmpObj.put("uuid", String.format(Locale.US, "{%s}", UUID.randomUUID().toString()));
                data.put("SenderDeviceInfo", tmpObj);
                AppLogger.e("MyTest", tmpObj.toString());
                writeUsingBufferedWriter(data);
            }

        } catch (Exception e) {
            AppLogger.e("MyTest", e.getMessage() + "");
            e.printStackTrace();
            if (callback != null) callback.onException();
        }
    }

    private String getTransactionsFromDB() {
        //old query
        //transactions = Transactions.find(Transactions.class, "isuploaded =?", "0");
        transactions = dataManager.getDaoSession().getTransactionsDao().queryBuilder()
                .where(TransactionsDao.Properties.IsUploaded.eq("0")).list();

        if (transactions.isEmpty()) return "";

        JsonSerializer<Transactions> serializer = (tmpTransactions, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("cardSerialNumber", tmpTransactions.getCardSerialNumber());
            jsonObject.addProperty("voucherId", tmpTransactions.getVoucherId());
            jsonObject.addProperty("transaction_type", tmpTransactions.getTransactionType());
            jsonObject.addProperty("programname", tmpTransactions.getProgramName());
            jsonObject.addProperty("programId", tmpTransactions.getProgramId());
            jsonObject.addProperty("programCurrency", tmpTransactions.getProgramCurrency());
            jsonObject.addProperty("latitude", tmpTransactions.getLatitude());
            jsonObject.addProperty("longitude", tmpTransactions.getLongitude());
            jsonObject.addProperty("startDate", tmpTransactions.getTopupStartDate().getTime());
            jsonObject.addProperty("endDate", tmpTransactions.getTopupEndDate().getTime());
            jsonObject.addProperty("voucherIdNo", tmpTransactions.getVoucherIdNo());
            jsonObject.addProperty("submit", tmpTransactions.getSubmit());
            jsonObject.addProperty("benfname", tmpTransactions.getBeneficiaryName());
            jsonObject.addProperty("cancelled_transaction", 0);
            jsonObject.addProperty("receipt_number", tmpTransactions.getReceiptNo());
            jsonObject.addProperty("value_remaining", tmpTransactions.getTotalValueRemaining());
            jsonObject.addProperty("total_amount_charged_by_retailer", tmpTransactions.getTotalAmountChargedByRetail());
            jsonObject.addProperty("user", tmpTransactions.getUser());
            jsonObject.addProperty("rationNo", tmpTransactions.getIdentityNo());
            jsonObject.addProperty("cardNumber", tmpTransactions.getCardNo());
            jsonObject.addProperty("agentId", tmpTransactions.getAgentId());
            jsonObject.addProperty("date", tmpTransactions.getDate());
            jsonObject.addProperty("locationID", tmpTransactions.getLocationId());
            jsonObject.addProperty("timestamp_transaction_created", tmpTransactions.getTimeStamp());
            jsonObject.addProperty("authentication_type", 0);
            jsonObject.addProperty("pos_terminal", tmpTransactions.getDeviceId());

            //old query
            //List<Commodities> tops = Commodities.find(Commodities.class, "transactionNo =?", tmpTransactions.getReceiptno().toString());
            List<Commodities> tops = dataManager.getDaoSession().getCommoditiesDao().queryBuilder()
                    .where(CommoditiesDao.Properties.TransactionNo.eq(tmpTransactions.getReceiptNo())).list();
            JsonArray commodityArray = new JsonArray();
            for (Commodities tmpObj : tops) {
                JsonObject subObject = new JsonObject();
                subObject.addProperty("pos_commodity", tmpObj.getProductId());
                subObject.addProperty("uniqueid", tmpObj.getUniqueId());
                subObject.addProperty("uom", tmpObj.getUom());
                subObject.addProperty("programId", tmpObj.getProgramId());
                subObject.addProperty("transactionNo", tmpObj.getTransactionNo());
                subObject.addProperty("productName", tmpObj.getProductName());
                subObject.addProperty("maxPrice",tmpObj.getMaxPrice());
                subObject.addProperty("categoryId", tmpObj.getCategoryId());
                subObject.addProperty("identityNo", tmpObj.getIdentificationNum());
                subObject.addProperty("voidTransaction", tmpObj.getVoidTransaction());
                subObject.addProperty("bnfName", tmpObj.getBeneficiaryName());
                subObject.addProperty("date", tmpObj.getDate());
                subObject.addProperty("amount_charged_by_retailer", tmpObj.getTotalAmountChargedByRetailer());
                subObject.addProperty("deducted_quantity", tmpObj.getQuantityDeducted());

                commodityArray.add(subObject);
            }
            jsonObject.add("commodities", commodityArray);

            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(Transactions.class, serializer);
        Gson customGson = gsonBuilder.create();
        return customGson.toJson(transactions);
    }

    private String getTransactionsListProductsFromDB() {
        final String android_id = CommonUtils.getDeviceId(activity);

        //old query
        //List<TransactionListProducts> transactionsListProducts = TransactionListProducts.listAll(TransactionListProducts.class);
        List<TransactionListProducts> transactionsListProducts = dataManager.getDaoSession().getTransactionListProductsDao().queryBuilder().list();
        if (transactionsListProducts.isEmpty()) return "";

        JsonSerializer<TransactionListProducts> serializer = (tmpTransactionsListProducts, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("deviceId", android_id);
            jsonObject.addProperty("serviceId", tmpTransactionsListProducts.getProductId());
            jsonObject.addProperty("benfname", tmpTransactionsListProducts.getBeneficiaryName());
            jsonObject.addProperty("uom", tmpTransactionsListProducts.getUnitOfMeasure());
            jsonObject.addProperty("productName", tmpTransactionsListProducts.getProductName());
            jsonObject.addProperty("quantity", tmpTransactionsListProducts.getQuantity());
            jsonObject.addProperty("value", tmpTransactionsListProducts.getVal());
            jsonObject.addProperty("uniqueid", tmpTransactionsListProducts.getUniqueid());
            jsonObject.addProperty("voidTransaction", tmpTransactionsListProducts.getVoidTransaction());
            jsonObject.addProperty("programId", tmpTransactionsListProducts.getProgramId());
            jsonObject.addProperty("transactionDate", tmpTransactionsListProducts.getTransactionDate());
            jsonObject.addProperty("transactionNo", tmpTransactionsListProducts.getTransactionNo());

            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(TransactionListProducts.class, serializer);
        Gson customGson = gsonBuilder.create();
        return customGson.toJson(transactionsListProducts);
    }

    private String getActivityLogFromDB() {
        //old query
        //List<ActivityLog> activityLogList = ActivityLog.listAll(ActivityLog.class);
        List<ActivityLog> activityLogList = dataManager.getDaoSession().getActivityLogDao().queryBuilder().list();
        if (activityLogList.isEmpty()) return "";

        JsonSerializer<ActivityLog> serializer = (tmpActivityLog, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("locationId", tmpActivityLog.getLocationId());
            jsonObject.addProperty("userName", tmpActivityLog.getUserName());
            jsonObject.addProperty("date", tmpActivityLog.getDate());
            jsonObject.addProperty("uniqueid", tmpActivityLog.getUniqueId());
            jsonObject.addProperty("activity", tmpActivityLog.getActivity());
            jsonObject.addProperty("deviceId", tmpActivityLog.getDeviceId());
            jsonObject.addProperty("action", tmpActivityLog.getAction());
            jsonObject.addProperty("latitude", tmpActivityLog.getLatitude());
            jsonObject.addProperty("longitude", tmpActivityLog.getLongitude());

            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(ActivityLog.class, serializer);
        Gson customGson = gsonBuilder.create();
        return customGson.toJson(activityLogList);
    }

    private String getAttendanceLogFromDB() {
        //old query
        //List<AttendanceLog> logsList = AttendanceLog.listAll(AttendanceLog.class);
        List<AttendanceLog> logsList = dataManager.getDaoSession().getAttendanceLogDao().queryBuilder().list();

        if (logsList.isEmpty()) return "";

        JsonSerializer<AttendanceLog> serializer = (tmpAttendanceLog, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("locationId", tmpAttendanceLog.getLocationId());
            jsonObject.addProperty("loginDate", tmpAttendanceLog.getLoginDate());
            jsonObject.addProperty("uniqueid", tmpAttendanceLog.getUniqueId());
            jsonObject.addProperty("loginSuccess", tmpAttendanceLog.isLoginSuccess());
            jsonObject.addProperty("latitude", tmpAttendanceLog.getLatitude());
            jsonObject.addProperty("userName", tmpAttendanceLog.getUsername());
            jsonObject.addProperty("longitude", tmpAttendanceLog.getLongitude());
            jsonObject.addProperty("deviceId", tmpAttendanceLog.getDeviceId());

            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(AttendanceLog.class, serializer);
        Gson customGson = gsonBuilder.create();
        return customGson.toJson(logsList);
    }

    private String getTopUpLogsFromDB() {
        //old query
        //List<TopupsLogs> topupsLogsList = TopupsLogs.listAll(TopupsLogs.class);
        List<TopupLogs> topupsLogsList = dataManager.getDaoSession().getTopupLogsDao().queryBuilder().list();

        if (topupsLogsList.isEmpty()) return "";

        JsonSerializer<TopupLogs> serializer = (tmpTopupLog, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("cardNumber", tmpTopupLog.getCardNo());
            jsonObject.addProperty("newTopupValue", tmpTopupLog.getNtopupValue());
            jsonObject.addProperty("newVoucherNo", tmpTopupLog.getNvoucherIdNo());
            jsonObject.addProperty("newCardBal", tmpTopupLog.getNCardBal());
            jsonObject.addProperty("oldVoucherNo", tmpTopupLog.getOvoucherIdNo());
            jsonObject.addProperty("oldCardBal", tmpTopupLog.getOCardBal());
            jsonObject.addProperty("topupTime", tmpTopupLog.getTopupTime());
            jsonObject.addProperty("deviceId", tmpTopupLog.getDeviceIdNo());
            jsonObject.addProperty("refno", tmpTopupLog.getRefNo());
            jsonObject.addProperty("programId", tmpTopupLog.getProgrammeId());
            jsonObject.addProperty("username", tmpTopupLog.getUserName());

            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(TopupLogs.class, serializer);
        Gson customGson = gsonBuilder.create();
        return customGson.toJson(topupsLogsList);
    }

    private String getBeneficiaryWithBioStatusFromDB() {
        //old query
        //List<Beneficiary> beneficiariesList = Beneficiary.find(Beneficiary.class, "isuploaded=? or bio_verify_status=?", "1", String.valueOf(BenfAuthEnum.APPROVED));
        List<Beneficiary> beneficiariesList = dataManager.getDaoSession().getBeneficiaryDao().queryBuilder()
                .whereOr(BeneficiaryDao.Properties.IsUploaded.eq("1"), BeneficiaryDao.Properties.BioVerifyStatus.eq(String.valueOf(BenfAuthEnum.APPROVED))).list();
        if (beneficiariesList.isEmpty()) return "";

        JsonSerializer<Beneficiary> serializerBnf = (tmpBnf, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("active", true);
            jsonObject.addProperty("locationId", userDetail.getLocationId());
            jsonObject.addProperty("memberId", tmpBnf.getBeneficiaryId());
            jsonObject.addProperty("idPassPortNo", tmpBnf.getIdentityNo());
            String name = tmpBnf.getLastName();
            jsonObject.addProperty("firstName", tmpBnf.getFirstName().concat(" ").concat(name == null ? "" : name).trim());
            jsonObject.addProperty("HouseNumber", tmpBnf.getAddress());
            jsonObject.addProperty("branchId", tmpBnf.getSectionName());
            jsonObject.addProperty("createdBy", tmpBnf.getAgentId());
            jsonObject.addProperty("gender", tmpBnf.getGender());
            jsonObject.addProperty("dateOfBirth", tmpBnf.getDateOfBirth());
            jsonObject.addProperty("activation", tmpBnf.getActivation());
            jsonObject.addProperty("isUploaded", "1");
            jsonObject.addProperty("cardNumber", tmpBnf.getCardNumber());
            jsonObject.addProperty("cellPhone", tmpBnf.getMobile());
            jsonObject.addProperty("agentId", tmpBnf.getAgentId());
            jsonObject.addProperty("deviceId", tmpBnf.getDeviceId());
            jsonObject.addProperty("cardPin", tmpBnf.getCardPin());
            jsonObject.addProperty("bioStatus", tmpBnf.getBio());
            jsonObject.addProperty("bioVerifyStatus", tmpBnf.getBioVerifyStatus());
            jsonObject.addProperty("cardSerialNumber",tmpBnf.getCardSerialNumber());
            jsonObject.addProperty("benfImage", tmpBnf.getImage());
            jsonObject.addProperty("dob", tmpBnf.getDateOfBirth());

            //old query
            //List<BeneficiaryBio> fingerprints = BeneficiaryBio.find(BeneficiaryBio.class, "beneficiaryid=?", tmpBnf.getNational_id());
            List<BeneficiaryBio> fingerprints = dataManager.getDaoSession().getBeneficiaryBioDao().queryBuilder()
                    .where(BeneficiaryBioDao.Properties.BeneficiaryId.eq(tmpBnf.getIdentityNo())).list();
            if (fingerprints.size() > 0) {
                jsonObject.add("fingerPrint", getFingerprintObject(fingerprints.get(0)));
            }

            return jsonObject;
        };

        gsonBuilder.registerTypeAdapter(Beneficiary.class, serializerBnf);
        Gson customGson = gsonBuilder.create();

        return customGson.toJson(beneficiariesList);
    }

    private JsonElement getFingerprintObject(BeneficiaryBio tmpFingerprints) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("leftFinger1", tmpFingerprints.getFplf());
        jsonObject.addProperty("leftFinger2", tmpFingerprints.getF1());
        jsonObject.addProperty("leftFinger3", tmpFingerprints.getF2());
        jsonObject.addProperty("rightFinger1", tmpFingerprints.getFprf());
        jsonObject.addProperty("rightFinger3", tmpFingerprints.getF4());
        jsonObject.addProperty("leftIndex", tmpFingerprints.getFpli());
        jsonObject.addProperty("leftThumb", tmpFingerprints.getFplt());
        jsonObject.addProperty("rightFinger2", tmpFingerprints.getF3());
        jsonObject.addProperty("rightIndex", tmpFingerprints.getFpri());
        jsonObject.addProperty("rightThumb", tmpFingerprints.getFprt());

        return jsonObject;
    }

    private String getCardBlockFromDB() {
        //old query
        //List<CardBlock> cardBlocksList = CardBlock.listAll(CardBlock.class);
        List<BlockCards> cardBlocksList = dataManager.getDaoSession().getBlockCardsDao().queryBuilder().list();
        if (cardBlocksList.isEmpty()) return "";

        JsonSerializer<BlockCards> serializerCardBlock = (tmpCardBlock, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("rationNo", tmpCardBlock.getIdentityNo());
            jsonObject.addProperty("cardNumber", tmpCardBlock.getCardNo());
            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(BlockCards.class, serializerCardBlock);
        Gson customGson = gsonBuilder.create();

        return customGson.toJson(cardBlocksList);
    }

    private String getTopUpFromDB() {
        //old query
        //List<Topups> topupsList = Topups.listAll(Topups.class);
        List<Topups> topupsList = dataManager.getDaoSession().getTopupsDao().queryBuilder().list();
        if (topupsList.isEmpty()) return "";

        JsonSerializer<Topups> serializer = (tmpTopup, typeOfSrc, context) -> {
            JsonObject jsonObject = new JsonObject();

            jsonObject.addProperty("beneficiaryId", tmpTopup.getBeneficiaryId());
            jsonObject.addProperty("cardNumber", tmpTopup.getCardNumber());
            jsonObject.addProperty("voucherValue", tmpTopup.getVoucherValue());
            jsonObject.addProperty("programmeId", tmpTopup.getProgrammeId());
            jsonObject.addProperty("voucherIdNo", tmpTopup.getVocherIdNo());
            jsonObject.addProperty("voucherId", tmpTopup.getVoucherId());
            jsonObject.addProperty("startDate", tmpTopup.getStartDate().getTime());
            jsonObject.addProperty("endDate", tmpTopup.getEndDate().getTime());

            return jsonObject;
        };
        gsonBuilder.registerTypeAdapter(Topups.class, serializer);
        Gson customGson = gsonBuilder.create();
        return customGson.toJson(topupsList);
    }

    //Exception Logs
    private String getExceptionsFromDB(){
        List<ExceptionLog> exceptionLogs = dataManager.getDaoSession().getExceptionLogDao().queryBuilder().list();
        if (exceptionLogs.isEmpty()) return "";

        Gson customGson = gsonBuilder.create();
        return customGson.toJson(exceptionLogs);
    }

    /**
     * Use BufferedWriter when number of write operations are more
     * It uses internal buffer to reduce real IO operations and saves time
     *
     * @param data master and vendor data
     */
    private void writeUsingBufferedWriter(JSONObject data) {
        //long  start = System.currentTimeMillis();
        FileWriter fr = null;
        BufferedWriter br = null;
        try {
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator.concat(AppConstants.FOLDER_NAME));
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File path = new File(folder, AppConstants.FILE_NAME);
            AppLogger.e("MyTest", path.getPath());
            fr = new FileWriter(path);
            br = new BufferedWriter(fr);

            br.write(data.toString());

            /*long end = System.currentTimeMillis();
            AppLogger.e("TimeCalculation: ",(end - start) / 1000f + " seconds");*/
            if (callback != null) callback.onDataPathReceive(path);
        } catch (IOException e) {
            if (callback != null) callback.onException();
            e.printStackTrace();
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
            }
        }
    }

    /**
     * Use FileWriter when number of write operations are less
     *
     * @param data master and vendor data
     */
    @SuppressWarnings("unused")
    private void writeUsingFileWriter(JSONObject data) {
        try {
            long start = System.currentTimeMillis();
            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator.concat(AppConstants.FOLDER_NAME));
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File path = new File(folder, AppConstants.FILE_NAME);
            AppLogger.e("MyTest", path.getPath());
            FileWriter out = new FileWriter(path);
            out.write(data.toString());
            out.close();

            long end = System.currentTimeMillis();
            AppLogger.e("TimeCalculation: ", (end - start) / 1000f + " seconds");
            if (callback != null) callback.onDataPathReceive(path);
        } catch (IOException e) {
            e.printStackTrace();
            if (callback != null) callback.onException();
        }
    }

    public interface SynchronizationDataCallback {
        void onDataPathReceive(File filePath);

        void onNoDataFound();

        void onException();
    }
}
