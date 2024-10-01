package com.compastbc.ui.synchronization.receive;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.data.db.model.ActivityLog;
import com.compastbc.core.data.db.model.AttendanceLog;
import com.compastbc.core.data.db.model.Beneficiary;
import com.compastbc.core.data.db.model.BeneficiaryBio;
import com.compastbc.core.data.db.model.BeneficiaryDao;
import com.compastbc.core.data.db.model.BlockCards;
import com.compastbc.core.data.db.model.Commodities;
import com.compastbc.core.data.db.model.ConfigurableParameters;
import com.compastbc.core.data.db.model.ExceptionLog;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.db.model.ProgramsDao;
import com.compastbc.core.data.db.model.SyncLogs;
import com.compastbc.core.data.db.model.SyncLogsDao;
import com.compastbc.core.data.db.model.TopupLogs;
import com.compastbc.core.data.db.model.Topups;
import com.compastbc.core.data.db.model.TopupsDao;
import com.compastbc.core.data.db.model.TransactionListProducts;
import com.compastbc.core.data.db.model.Transactions;
import com.compastbc.core.data.db.model.Users;
import com.compastbc.core.data.db.model.UsersDao;
import com.compastbc.core.data.network.model.Details;
import com.compastbc.core.data.network.model.PendingSync;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.synchronization.discovery.Device;
import com.compastbc.synchronization.transfer.TransferService;
import com.compastbc.synchronization.util.Settings;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.main.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class ReceiveActivity extends BaseActivity {

    private ProgressDialog progressDialog;
    private Handler handler;
    private Device device;
    private LocalBroadcastManager localBroadcastManager;
    private Details userDetails;
    private ConfigurableParameters configurableParameterDetail;
    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final File path = new File(Environment.getExternalStorageDirectory() +
                    File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.FILE_NAME));

            File pathAck = new File(Environment.getExternalStorageDirectory() +
                    File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.ACK_FILE_NAME));

            if (!pathAck.exists() && path.exists()) {
                if (verifyDBStatus()) {
                    setDialog(true);

                    new Thread(() -> updateDB(AppUtils.readFileAsString(path))).start();
                }
            } else {
                hideMaterialDialog();
                if (pathAck.exists()) {
                    showAckStatus(AppUtils.readFileAsString(pathAck));
                } else {
                    sweetAlert(SweetAlertDialog.ERROR_TYPE, "", getString(R.string.somethingWentWrong)).show();
                }
            }
        }
    };

    private void setDialog(boolean isLoading) {
        if (isLoading) {
            progressDialog = ProgressDialog.show(this, getString(R.string.verify_receive), getString(R.string.please_wait), true,
                    false, DialogInterface::dismiss);
            progressDialog.setCancelable(false);
        } else progressDialog.dismiss();
    }

    private boolean verifyDBStatus() {
        //for non bio
        //if (configurableParameterDetail.isBiometric()) {
        if (userDetails.getLevel().equalsIgnoreCase("1")) {
            List<Beneficiary> beneficiaries = getDataManager().getDaoSession().getBeneficiaryDao().queryBuilder()
                    .where(BeneficiaryDao.Properties.IsUploaded.eq("0")).list();
            List<Transactions> transactionsList = getDataManager().getDaoSession().getTransactionsDao().queryBuilder().list();
            if (beneficiaries.size() > 0 || transactionsList.size() > 0) {
                new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText(getString(R.string.error))
                        .setContentText(getString(R.string.sync_first))
                        .setConfirmText(getString(R.string.Ok))
                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation)
                        .show();
                return false;

            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive);

        userDetails = getDataManager().getUserDetail();
        configurableParameterDetail = getDataManager().getConfigurableParameterDetail();
        setUp();
    }

    @Override
    protected void setUp() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        TextView tv_title = findViewById(R.id.tvTitle);
        tv_title.setText(R.string.SYNC);
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(view -> onBackPressed());

        handler = new Handler(Looper.getMainLooper());
        boolean isSender = false;

        if (getIntent().hasExtra("sender")) isSender = true;

        // Get local broadcast manager object.
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        localBroadcastManager.registerReceiver(receiver, new IntentFilter("FileReceived"));

        // Launch the transfer service if it isn't already running
        TransferService.startStopService(this, new Settings(this).getBoolean(Settings.Key.BEHAVIOR_RECEIVE));

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, TransferFragment.newInstance(isSender))
                .commit();

        if (isSender) {
            MaterialDialog mDialog = materialDialog(R.string.send, R.string.please_wait);
            mDialog.setCancelable(false);
        }
    }

    private void writeJsonToFile(int flag) {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("mac", CommonUtils.getDeviceId(this));
            jsonObject.put("agentId", getDataManager().getUserDetail().getAgentId());
            if (flag == 0) {
                jsonObject.put("Status", "Success");
                jsonObject.put("Msg", getString(R.string.dataSend));
            } else {
                jsonObject.put("Status", "Failed");
                jsonObject.put("Msg", getString(R.string.somethingWentWrong));
            }

            File folder = new File(Environment.getExternalStorageDirectory() +
                    File.separator.concat(AppConstants.FOLDER_NAME));
            if (!folder.exists()) {
                //noinspection ResultOfMethodCallIgnored
                folder.mkdirs();
            }

            File path = new File(folder, AppConstants.ACK_FILE_NAME);
            AppLogger.e("MyTest", path.getPath());
            FileWriter out = new FileWriter(path);
            out.write(jsonObject.toString());
            out.close();

            if (device != null) {
                final ArrayList<Uri> uriList = new ArrayList<>();//buildUriList();
                uriList.add(Uri.fromFile(path));

                Intent startTransfer = new Intent(this, TransferService.class);
                startTransfer.setAction(TransferService.ACTION_START_TRANSFER);
                startTransfer.putExtra(TransferService.EXTRA_DEVICE, device);
                startTransfer.putParcelableArrayListExtra(TransferService.EXTRA_URIS, uriList);
                startService(startTransfer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void successData(String oppMacId, String oppAgentId) {
        if (userDetails.getLevel().equalsIgnoreCase("1")) {
            getDataManager().getDaoSession().getTopupLogsDao().deleteAll();
            getDataManager().getDaoSession().getTransactionsDao().deleteAll();
            getDataManager().getDaoSession().getTransactionListProductsDao().deleteAll();
            getDataManager().getDaoSession().getCommoditiesDao().deleteAll();
            getDataManager().getDaoSession().getActivityLogDao().deleteAll();
            getDataManager().getDaoSession().getAttendanceLogDao().deleteAll();
            getDataManager().getDaoSession().getExceptionLogDao().deleteAll();
            //for non bio
            // if (configurableParameterDetail.isBiometric()) {
            BeneficiaryDao bnfDao = getDataManager().getDaoSession().getBeneficiaryDao();
            List<Beneficiary> beneficiaries = bnfDao.queryBuilder().list();
            if (beneficiaries.size() > 0) {
                for (int i = 0; i < beneficiaries.size(); i++) {
                    beneficiaries.get(i).setIsUploaded("1");
                    bnfDao.update(beneficiaries.get(i));
                }
            }
            UsersDao userDao = getDataManager().getDaoSession().getUsersDao();
            List<Users> user = userDao.queryBuilder().where(UsersDao.Properties.Isuploaded.eq("0")).list();
            if (user.size() > 0) {
                for (int i = 0; i < user.size(); i++) {
                    user.get(i).setIsuploaded("1");
                    userDao.update(user.get(i));
                }
            }
            // }
        } else if (userDetails.getLevel().equalsIgnoreCase("2")) {
            SyncLogs syncLogs = new SyncLogs();
            //syncLogs.setSend_by(users.get(0).getAgentId());
            syncLogs.setSend_by(getDataManager().getUserDetail().getAgentId());
            syncLogs.setSend_by_deviceId(getDataManager().getDeviceId());
            syncLogs.setSend_date(new Date());

            syncLogs.setReceived_deviceId(oppMacId); //deviceId
            syncLogs.setReceived_by(oppAgentId);  //vendorId
            syncLogs.setStatus("pending");
            syncLogs.setReceived_date(new Date());

            Cursor cursor = getDataManager().getDaoSession().getDatabase().rawQuery("SELECT * from " + TopupsDao.TABLENAME
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


            SyncLogsDao syncLogDao = getDataManager().getDaoSession().getSyncLogsDao();
            for (int i = 0; i < tps.size(); i++) {
                Topups bean = tps.get(i);
                SyncLogs oldSyncLog = syncLogDao.queryBuilder().where(SyncLogsDao.Properties.Received_deviceId.eq(oppMacId), SyncLogsDao.Properties.ProgramId.eq(bean.getProgrammeId())).limit(1).unique();
                if (oldSyncLog != null) {
                    syncLogs.setProgramId(bean.getProgrammeId());
                    syncLogs.setStartDate(bean.getStartDate());
                    syncLogs.setEndDate(bean.getEndDate());
                    syncLogDao.insertOrReplace(syncLogs);
                } else {
                    syncLogs.setProgramId(bean.getProgrammeId());
                    syncLogs.setStartDate(bean.getStartDate());
                    syncLogs.setEndDate(bean.getEndDate());
                    syncLogDao.save(syncLogs);
                }
            }
        }
    }

    private void showAckStatus(String jsonData) {
        try {
            JSONObject ja = new JSONObject(jsonData);
            String status = ja.getString("Status");
            String msg = ja.getString("Msg");
            String oppAgentId = ja.getString("agentId");
            String oppMacId = ja.getString("mac");

            if (status.equalsIgnoreCase("Success")) {
                sweetAlert(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.success), msg).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    navigateToNext();
                }).show();

                new Thread(() -> successData(oppMacId, oppAgentId)).start();
            } else {
                sweetAlert(SweetAlertDialog.ERROR_TYPE, getString(R.string.fail), msg).setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    navigateToNext();
                }).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            sweetAlert(SweetAlertDialog.ERROR_TYPE, getString(R.string.fail), getString(R.string.somethingWentWrong))
                    .setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        navigateToNext();
                    }).show();
        }
    }

    private void updateDB(String jsonData) {
        try {
            JSONObject ja = new JSONObject(jsonData);
            if (ja.length() > 0) {
                if (ja.has("SenderDeviceInfo")) {
                    JSONObject obj = ja.getJSONObject("SenderDeviceInfo");
                    String inetAddress = obj.getString("inet");
                    InetAddress inet = InetAddress.getByName(inetAddress);
                    device = new Device(obj.getString("name"), obj.getString("uuid"), inet, obj.getInt("port"));
                }

                if (userDetails.getLevel().equalsIgnoreCase("2")) {
                    if (ja.has("Transactions")) {
                        Commodities commodities_table;
                        JSONArray txns_array, commodities;
                        List<Transactions> transactions = new ArrayList<>();
                        List<Commodities> commodities1 = new ArrayList<>();

                        txns_array = ja.getJSONArray("Transactions");
                        if (txns_array.length() > 0) {
                            int count = 0;
                            String deviceId = "";
                            /*totalamount = 0;
                            size = String.valueOf(txns_array.length());*/
                            for (int i = 0; i < txns_array.length(); i++) {
                                Transactions txns_table = new Transactions();
                                commodities = new JSONArray(txns_array.getJSONObject(i).getString("commodities"));

                                if (txns_array.getJSONObject(i).has("cardSerialNumber")) {
                                    txns_table.setCardSerialNumber(txns_array.getJSONObject(i).getString("cardSerialNumber"));
                                }

                                txns_table.setVoucherId(txns_array.getJSONObject(i).getString("voucherId"));
                                txns_table.setTransactionType(txns_array.getJSONObject(i).getString("transaction_type"));
                                AppLogger.i("ReceiveActivity", txns_array.getJSONObject(i).getString("transaction_type") + "  sss");
                                txns_table.setReceiptNo(txns_array.getJSONObject(i).getLong("receipt_number"));
                                txns_table.setTotalValueRemaining(txns_array.getJSONObject(i).getString("value_remaining"));
                                txns_table.setCardNo(txns_array.getJSONObject(i).getString("cardNumber"));
                                txns_table.setLocationId(txns_array.getJSONObject(i).getString("locationID"));
                                txns_table.setBeneficiaryName(txns_array.getJSONObject(i).getString("benfname"));
                                txns_table.setIsUploaded("0");
                                txns_table.setVoucherIdNo(txns_array.getJSONObject(i).getString("voucherIdNo"));
                                count = count + 1;
                                //totalamount = totalamount + Float.parseFloat(txns_array.getJSONObject(i).getString("total_amount_charged_by_retailer"));
                                txns_table.setDate(txns_array.getJSONObject(i).getString("date"));
                                txns_table.setTotalAmountChargedByRetail(txns_array.getJSONObject(i).getString("total_amount_charged_by_retailer"));
                                txns_table.setUser(txns_array.getJSONObject(i).getString("user"));
                                txns_table.setLatitude(txns_array.getJSONObject(i).getDouble("latitude"));
                                txns_table.setLongitude(txns_array.getJSONObject(i).getDouble("longitude"));
                                txns_table.setSubmit(txns_array.getJSONObject(i).getString("submit"));
                                txns_table.setProgramName(txns_array.getJSONObject(i).getString("programname"));
                                txns_table.setProgramId(txns_array.getJSONObject(i).getString("programId"));
                                txns_table.setProgramCurrency(txns_array.getJSONObject(i).getString("programCurrency"));
                                txns_table.setTopupStartDate(new Date(txns_array.getJSONObject(i).getLong("startDate")));
                                txns_table.setTopupEndDate(new Date(txns_array.getJSONObject(i).getLong("endDate")));
                                txns_table.setIdentityNo(txns_array.getJSONObject(i).getString("rationNo"));
                                txns_table.setTimeStamp(txns_array.getJSONObject(i).getString("timestamp_transaction_created"));
                                txns_table.setAgentId(txns_array.getJSONObject(i).getString("agentId"));
                                deviceId = txns_array.getJSONObject(i).getString("pos_terminal");
                                txns_table.setDeviceId(deviceId);
                                transactions.add(txns_table);
                                for (int j = 0; j < commodities.length(); j++) {
                                    commodities_table = new Commodities();
                                    commodities_table.setUom(commodities.getJSONObject(j).getString("uom"));
                                    commodities_table.setTransactionNo(txns_array.getJSONObject(i).getString("receipt_number"));
                                    commodities_table.setProductId(commodities.getJSONObject(j).getString("pos_commodity"));
                                    commodities_table.setTransactionNo(commodities.getJSONObject(j).getString("transactionNo"));
                                    commodities_table.setDate(commodities.getJSONObject(j).getString("date"));
                                    commodities_table.setProgramId(commodities.getJSONObject(j).getString("programId"));
                                    commodities_table.setProductName(commodities.getJSONObject(j).getString("productName"));
                                    commodities_table.setMaxPrice(commodities.getJSONObject(j).getString("maxPrice"));
                                    commodities_table.setIdentificationNum(commodities.getJSONObject(j).getString("identityNo"));
                                    commodities_table.setVoidTransaction(commodities.getJSONObject(j).getString("voidTransaction"));
                                    commodities_table.setUniqueId(commodities.getJSONObject(j).getLong("uniqueid"));
                                    commodities_table.setBeneficiaryName(commodities.getJSONObject(j).getString("bnfName"));
                                    commodities_table.setCategoryId(commodities.getJSONObject(j).getString("categoryId"));
                                    commodities_table.setTotalAmountChargedByRetailer(commodities.getJSONObject(j).getDouble("amount_charged_by_retailer"));
                                    commodities_table.setQuantityDeducted(commodities.getJSONObject(j).getString("deducted_quantity"));
                                    commodities1.add(commodities_table);
                                }

                            }
                            getDataManager().getDaoSession().getTransactionsDao().insertOrReplaceInTx(transactions);
                            getDataManager().getDaoSession().getCommoditiesDao().insertOrReplaceInTx(commodities1);

                            if (ja.has("Pending_Sync")) {
                                JSONArray array = ja.getJSONArray("Pending_Sync");
                                List<PendingSync> sync = new ArrayList<>();
                                for (int i = 0; i < array.length(); i++) {
                                    PendingSync sync1 = new PendingSync();
                                    sync1.setProgrammeId(array.getJSONObject(i).getString("programId"));
                                    sync1.setTamt(array.getJSONObject(i).getString("total_amount"));
                                    sync1.setTtxns(array.getJSONObject(i).getString("total_transaction"));
                                    sync1.setProgramCurrency(array.getJSONObject(i).getString("programCurrency"));
                                    sync1.setStartDate(new Date(array.getJSONObject(i).getLong("startDate")));
                                    sync1.setEndDate(new Date(array.getJSONObject(i).getLong("endDate")));
                                    sync.add(sync1);
                                }
                                SyncLogsDao syncLogsDao = getDataManager().getDaoSession().getSyncLogsDao();
                                List<SyncLogs> syncLogs = syncLogsDao.queryBuilder()
                                        .where(SyncLogsDao.Properties.Received_deviceId.eq(deviceId),
                                                SyncLogsDao.Properties.Received_by.eq(txns_array.getJSONObject(0).getString("agentId")))
                                        .list();

                                for (int i = 0; i < sync.size(); i++) {
                                    int sync_count = 0;
                                    if (syncLogs.size() > 0) {
                                        for (int j = 0; j < syncLogs.size(); j++) {
                                            if (sync.get(i).getProgrammeId().equalsIgnoreCase(syncLogs.get(j).getProgramId())) {
                                                sync_count = 1;
                                                syncLogs.get(j).setProgramCurrency(sync.get(i).getProgramCurrency());
                                                syncLogs.get(j).setUpload_by(txns_array.getJSONObject(0).getString("agentId"));
                                                syncLogs.get(j).setUpload_deviceId(deviceId);
                                                syncLogs.get(j).setUpload_date(new Date());
                                                String txnCount = sync.get(i).getTtxns();
                                                int txnFinalCount = txnCount.isEmpty() ? Integer.parseInt(syncLogs.get(j).getTotal_transaction()) : Integer.parseInt(txnCount) + Integer.parseInt(syncLogs.get(j).getTotal_transaction());
                                                syncLogs.get(j).setTotal_transaction(String.valueOf(txnFinalCount));

                                                String txnAmt = sync.get(i).getTamt();
                                                Double txnFinalAmt = txnAmt.isEmpty() ? Double.parseDouble(syncLogs.get(j).getTotal_amount()) : (Double.parseDouble(txnAmt) + Double.parseDouble(syncLogs.get(j).getTotal_amount()));
                                                syncLogs.get(j).setTotal_amount(String.valueOf(txnFinalAmt));
                                                syncLogs.get(j).setStatus("done");
                                                syncLogsDao.save(syncLogs.get(j));
                                                break;
                                            }
                                        }

                                        /*
                                         * When Sync log doesn't contain the program id for the previous cycle.
                                         * Usually for those user who forgot to upload previous cycle or program data that are end.
                                         * */
                                        if (sync_count == 0) {
                                            SyncLogs logs = new SyncLogs();
                                            logs.setUpload_by(txns_array.getJSONObject(0).getString("agentId"));
                                            logs.setUpload_date(new Date());
                                            logs.setUpload_deviceId(deviceId);
                                            logs.setStatus("done");
                                            logs.setProgramCurrency(sync.get(i).getProgramCurrency());
                                            logs.setProgramId(sync.get(i).getProgrammeId());
                                            logs.setStartDate(sync.get(i).getStartDate());
                                            logs.setEndDate(sync.get(i).getEndDate());
                                            logs.setTotal_amount(sync.get(i).getTamt());
                                            logs.setTotal_transaction(sync.get(i).getTtxns());
                                            syncLogsDao.save(logs);
                                        }
                                    } else {
                                        SyncLogs logs = new SyncLogs();
                                        logs.setUpload_by(txns_array.getJSONObject(0).getString("agentId"));
                                        logs.setUpload_date(new Date());
                                        logs.setUpload_deviceId(deviceId);
                                        logs.setStatus("done");
                                        logs.setProgramCurrency(sync.get(i).getProgramCurrency());
                                        logs.setProgramId(sync.get(i).getProgrammeId());
                                        logs.setStartDate(sync.get(i).getStartDate());
                                        logs.setEndDate(sync.get(i).getEndDate());
                                        logs.setTotal_amount(sync.get(i).getTamt());
                                        logs.setTotal_transaction(sync.get(i).getTtxns());
                                        syncLogsDao.save(logs);
                                    }
                                }
                            }
                        }
                    }

                    //if (configurableParameterDetail.isBiometric()) {

                    try {
                        if (ja.has("Beneficiaries")) {
                            JSONArray members = ja.getJSONArray("Beneficiaries");
                            List<Beneficiary> beneficiaries = new ArrayList<>();
                            List<BeneficiaryBio> beneficiaryBioList = new ArrayList<>();

                            for (int i = 0; i < members.length(); i++) {
                                Beneficiary beneficiary_table = new Beneficiary();
                                beneficiary_table.setFirstName(members.getJSONObject(i).getString("firstName"));
                                beneficiary_table.setIdentityNo(members.getJSONObject(i).getString("idPassPortNo").trim());
                                beneficiary_table.setGender(members.getJSONObject(i).getString("gender"));
                                beneficiary_table.setDateOfBirth(members.getJSONObject(i).getString("dateOfBirth"));
                                beneficiary_table.setActivation(members.getJSONObject(i).getString("activation"));
                                beneficiary_table.setCardPin(members.getJSONObject(i).getString("cardPin"));
                                beneficiary_table.setIsUploaded(members.getJSONObject(i).getString("isUploaded"));
                                beneficiary_table.setBio(members.getJSONObject(i).getBoolean("bioStatus"));
                                beneficiary_table.setCardNumber(members.getJSONObject(i).getString("cardNumber"));
                                if (members.getJSONObject(i).has("cardSerialNumber"))
                                    beneficiary_table.setCardSerialNumber(members.getJSONObject(i).getString("cardSerialNumber"));

                                if (members.getJSONObject(i).has("cellPhone"))
                                    beneficiary_table.setMobile(members.getJSONObject(i).getString("cellPhone"));
                                if (members.getJSONObject(i).has("bioVerifyStatus"))
                                    beneficiary_table.setBioVerifyStatus(members.getJSONObject(i).getString("bioVerifyStatus"));
                                try {
                                    if (members.getJSONObject(i).has("createdBy"))
                                        beneficiary_table.setAgentId(members.getJSONObject(i).getString("createdBy"));

                                    if (members.getJSONObject(i).has("deviceId"))
                                        beneficiary_table.setDeviceId(members.getJSONObject(i).getString("deviceId"));
                                    if (members.getJSONObject(i).has("branchId"))
                                        beneficiary_table.setSectionName(members.getJSONObject(i).getString("branchId"));
                                    if (members.getJSONObject(i).has("benfImage"))
                                        beneficiary_table.setImage(members.getJSONObject(i).getString("benfImage"));
                                    if (members.getJSONObject(i).has("HouseNumber"))
                                        beneficiary_table.setAddress(members.getJSONObject(i).getString("HouseNumber"));
                                    if (members.getJSONObject(i).has("memberId"))
                                        beneficiary_table.setId(Long.parseLong(members.getJSONObject(i).getString("memberId")));
                                    if (members.getJSONObject(i).has("memberId"))
                                        beneficiary_table.setBeneficiaryId(members.getJSONObject(i).getString("memberId"));
                                    if (members.getJSONObject(i).has("bioVerifyStatus"))
                                        beneficiary_table.setBioVerifyStatus(members.getJSONObject(i).getString("bioVerifyStatus"));
                                } catch (Exception e) {
                                    e.printStackTrace();

                                }
                                try {
                                    if (configurableParameterDetail.isBiometric()) {
                                        beneficiary_table.setBio(members.getJSONObject(i).getBoolean("bioStatus"));
                                        if (members.getJSONObject(i).getBoolean("bioStatus")) {
                                            BeneficiaryBio beneficiaryBio = new BeneficiaryBio();
                                            beneficiaryBio.setBeneficiaryId(members.getJSONObject(i).getString("idPassPortNo").trim());

                                            if (!members.getJSONObject(i).isNull("fingerPrint")) {
                                                JSONObject fingers = members.getJSONObject(i).getJSONObject("fingerPrint");

                                                if (fingers.has("rightFinger3"))
                                                    beneficiaryBio.setF4(fingers.getString("rightFinger3"));

                                                if (fingers.has("leftFinger2"))
                                                    beneficiaryBio.setF1(fingers.getString("leftFinger2"));

                                                if (fingers.has("leftFinger3"))
                                                    beneficiaryBio.setF2(fingers.getString("leftFinger3"));

                                                if (fingers.has("rightFinger2"))
                                                    beneficiaryBio.setF3(fingers.getString("rightFinger2"));

                                                if (fingers.has("rightThumb"))
                                                    beneficiaryBio.setFprt(fingers.getString("rightThumb"));

                                                if (fingers.has("rightIndex"))
                                                    beneficiaryBio.setFpri(fingers.getString("rightIndex"));

                                                if (fingers.has("leftFinger1"))
                                                    beneficiaryBio.setFplf(fingers.getString("leftFinger1"));

                                                if (fingers.has("leftIndex"))
                                                    beneficiaryBio.setFpli(fingers.getString("leftIndex"));

                                                if (fingers.has("leftThumb"))
                                                    beneficiaryBio.setFplt(fingers.getString("leftThumb"));

                                                if (fingers.has("rightFinger1"))
                                                    beneficiaryBio.setFprf(fingers.getString("rightFinger1"));

                                                beneficiaryBioList.add(beneficiaryBio);
                                            }
                                        }

                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                beneficiaries.add(beneficiary_table);
                            }
                            getDataManager().getDaoSession().getBeneficiaryDao().insertOrReplaceInTx(beneficiaries);
                            getDataManager().getDaoSession().getBeneficiaryBioDao().insertOrReplaceInTx(beneficiaryBioList);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (configurableParameterDetail.isBiometric()) {
                        if (ja.has("Agents")) {
                            JSONArray users = ja.getJSONArray("Agents");
                            List<Users> user = new ArrayList<>();
                            for (int i = 0; i < users.length(); i++) {
                                Users user_table = new Users();
                                user_table.setUsersId(users.getJSONObject(i).getString("userId"));
                                user_table.setUsername(users.getJSONObject(i).getString("userName"));
                                user_table.setPassword(users.getJSONObject(i).getString("password"));
                                user_table.setLevel(users.getJSONObject(i).getString("level"));
                                user_table.setLocationid(users.getJSONObject(i).getString("locationId"));
                                //user_table.setMerchantMaster(users.getJSONObject(i).getString("merchantId"));
                                user_table.setAgentId(users.getJSONObject(i).getString("agentId"));
                                user_table.setBio(users.getJSONObject(i).getBoolean("bioStatus"));
                                user_table.setIsuploaded("0");
                                try {
                                    if (users.getJSONObject(i).getBoolean("bioStatus")) {
                                        if (!users.getJSONObject(i).isNull("fingerPrint")) {
                                            JSONObject fingers = users.getJSONObject(i).getJSONObject("fingerPrint");
                                            if (fingers.has("rightFinger3"))
                                                user_table.setF4(fingers.getString("rightFinger3"));

                                            if (fingers.has("leftFinger2"))
                                                user_table.setF1(fingers.getString("leftFinger2"));

                                            if (fingers.has("leftFinger3"))
                                                user_table.setF2(fingers.getString("leftFinger3"));

                                            if (fingers.has("rightFinger2"))
                                                user_table.setF3(fingers.getString("rightFinger2"));

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
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                user.add(user_table);
                            }
                            getDataManager().getDaoSession().getUsersDao().insertOrReplaceInTx(user);
                        }
                    }
                    // }

                    if (ja.has("TopupsLogs")) {
                        TopupLogs topupslogs_table;
                        List<TopupLogs> logs = new ArrayList<>();
                        JSONArray topupslogs_array = ja.getJSONArray("TopupsLogs");
                        if (topupslogs_array.length() > 0) {
                            for (int i = 0; i < topupslogs_array.length(); i++) {
                                topupslogs_table = new TopupLogs();
                                topupslogs_table.setCardNo(topupslogs_array.getJSONObject(i).getString("cardNumber"));
                                topupslogs_table.setNtopupValue(topupslogs_array.getJSONObject(i).getString("newTopupValue"));
                                topupslogs_table.setNvoucherIdNo(topupslogs_array.getJSONObject(i).getString("newVoucherNo"));
                                topupslogs_table.setNCardBal(topupslogs_array.getJSONObject(i).getString("newCardBal"));
                                topupslogs_table.setOvoucherIdNo(topupslogs_array.getJSONObject(i).getString("oldVoucherNo"));
                                topupslogs_table.setOCardBal(topupslogs_array.getJSONObject(i).getString("oldCardBal"));
                                topupslogs_table.setTopupTime(topupslogs_array.getJSONObject(i).getString("topupTime"));
                                topupslogs_table.setDeviceIdNo(topupslogs_array.getJSONObject(i).getString("deviceId"));
                                topupslogs_table.setProgrammeId(topupslogs_array.getJSONObject(i).getString("programId"));
                                topupslogs_table.setIsUploaded("0");
                                topupslogs_table.setUserName(topupslogs_array.getJSONObject(i).getString("username"));
                                topupslogs_table.setRefNo(topupslogs_array.getJSONObject(i).getString("refno"));
                                logs.add(topupslogs_table);
                            }
                            getDataManager().getDaoSession().getTopupLogsDao().insertOrReplaceInTx(logs);
                        }
                    }

                    if (ja.has("ArchiveTransactions")) {
                        TransactionListProducts txnsarchives_table;
                        JSONArray txnsarchives_array = ja.getJSONArray("ArchiveTransactions");
                        List<TransactionListProducts> log = new ArrayList<>();
                        if (txnsarchives_array.length() > 0) {
                            for (int i = 0; i < txnsarchives_array.length(); i++) {
                                txnsarchives_table = new TransactionListProducts();
                                txnsarchives_table.setProductId(txnsarchives_array.getJSONObject(i).getString("serviceId"));
                                txnsarchives_table.setDeviceId(txnsarchives_array.getJSONObject(i).getString("deviceId"));
                                txnsarchives_table.setUnitOfMeasure(txnsarchives_array.getJSONObject(i).getString("uom"));
                                txnsarchives_table.setProgramId(txnsarchives_array.getJSONObject(i).getString("programId"));
                                txnsarchives_table.setUniqueid(txnsarchives_array.getJSONObject(i).getString("uniqueid"));
                                txnsarchives_table.setVoidTransaction(txnsarchives_array.getJSONObject(i).getString("voidTransaction"));

                                if (txnsarchives_array.getJSONObject(i).has("productName")) {
                                    txnsarchives_table.setProductName(txnsarchives_array.getJSONObject(i).getString("productName"));
                                }
                                txnsarchives_table.setQuantity(txnsarchives_array.getJSONObject(i).getString("quantity"));
                                txnsarchives_table.setBeneficiaryName(txnsarchives_array.getJSONObject(i).getString("benfname"));
                                txnsarchives_table.setVal(txnsarchives_array.getJSONObject(i).getString("value"));
                                txnsarchives_table.setTransactionNo(txnsarchives_array.getJSONObject(i).getString("transactionNo"));
                                txnsarchives_table.setTransactionDate(txnsarchives_array.getJSONObject(i).getString("transactionDate"));
                                log.add(txnsarchives_table);
                            }
                            getDataManager().getDaoSession().getTransactionListProductsDao().insertOrReplaceInTx(log);
                        }
                    }

                    if(ja.has("ExceptionLogs")){
                        JSONArray exceptionArray = ja.getJSONArray("ExceptionLogs");
                        ExceptionLog exceptionLog;
                        List<ExceptionLog> logs = new ArrayList<>();
                        for (int i=0; i<exceptionArray.length() ;i++){
                            exceptionLog = new ExceptionLog();
                            exceptionLog.setDeviceId(exceptionArray.getJSONObject(i).getString("deviceId"));
                            exceptionLog.setDeviceName(exceptionArray.getJSONObject(i).getString("deviceName"));
                            exceptionLog.setCreatedDate(exceptionArray.getJSONObject(i).getString("createdDate"));
                            exceptionLog.setDataObject(exceptionArray.getJSONObject(i).getString("dataObject"));
                            exceptionLog.setScreenName(exceptionArray.getJSONObject(i).getString("screenName"));
                            exceptionLog.setAgentId(exceptionArray.getJSONObject(i).getString("agentId"));
                            logs.add(exceptionLog);
                        }
                        getDataManager().getDaoSession().getExceptionLogDao().insertInTx(logs);
                    }

                    if (ja.has("AttendanceLog")) {
                        AttendanceLog log;
                        List<AttendanceLog> logs = new ArrayList<>();
                        JSONArray attendance_array = ja.getJSONArray("AttendanceLog");
                        for (int i = 0; i < attendance_array.length(); i++) {
                            log = new AttendanceLog();
                            //log.setMerchantMasterId(attendance_array.getJSONObject(i).getString("merchantMasterId"));
                            log.setLocationId(attendance_array.getJSONObject(i).getString("locationId"));
                            log.setLoginDate(attendance_array.getJSONObject(i).getString("loginDate"));
                            log.setUniqueId(attendance_array.getJSONObject(i).getString("uniqueid"));
                            log.setLoginSuccess(attendance_array.getJSONObject(i).getBoolean("loginSuccess"));
                            log.setLatitude(attendance_array.getJSONObject(i).getDouble("latitude"));
                            log.setLongitude(attendance_array.getJSONObject(i).getDouble("longitude"));
                            log.setUsername(attendance_array.getJSONObject(i).getString("userName"));
                            log.setDeviceId(attendance_array.getJSONObject(i).getString("deviceId"));
                            logs.add(log);
                        }
                        getDataManager().getDaoSession().getAttendanceLogDao().insertOrReplaceInTx(logs);
                    }

                    if (ja.has("ActivityLog")) {
                        ActivityLog logs;
                        List<ActivityLog> log = new ArrayList<>();
                        JSONArray activity_array = ja.getJSONArray("ActivityLog");
                        for (int i = 0; i < activity_array.length(); i++) {
                            logs = new ActivityLog();
                            logs.setDeviceId(activity_array.getJSONObject(i).getString("deviceId"));
                            logs.setAction(activity_array.getJSONObject(i).getString("action"));
                            logs.setDate(activity_array.getJSONObject(i).getString("date"));
                            logs.setActivity(activity_array.getJSONObject(i).getString("activity"));
                            logs.setLocationId(activity_array.getJSONObject(i).getString("locationId"));
                            if (activity_array.getJSONObject(i).has("uniqueid"))
                                logs.setUniqueId(activity_array.getJSONObject(i).getString("uniqueid"));
                            else logs.setUniqueId(UUID.randomUUID().toString());
                            //logs.setMerchantId(activity_array.getJSONObject(i).getString("merchantId"));
                            logs.setUserName(activity_array.getJSONObject(i).getString("userName"));
                            logs.setLatitude(activity_array.getJSONObject(i).getDouble("latitude"));
                            logs.setLongitude(activity_array.getJSONObject(i).getDouble("longitude"));
                            log.add(logs);
                        }
                        getDataManager().getDaoSession().getActivityLogDao().insertOrReplaceInTx(log);
                    }

                } else if (userDetails.getLevel().equalsIgnoreCase("1")) {

                    if (ja.has("Topups")) {
                        //old query
                        //Topups.deleteAll(Topups.class);
                        getDataManager().getDaoSession().getTopupsDao().deleteAll();
                        List<Topups> topups = new ArrayList<>();
                        JSONArray topup = ja.getJSONArray("Topups");
                        for (int i = 0; i < topup.length(); i++) {
                            Topups topups_table = new Topups();
                            topups_table.setProgrammeId(topup.getJSONObject(i).getString("programmeId"));
                            Programs program = getDataManager().getDaoSession().getProgramsDao().queryBuilder().where(ProgramsDao.Properties.ProgramId.eq(topups_table.getProgrammeId())).limit(1).unique();
                            if (program != null) {
                                //topups_table.setBengroup(topup.getJSONObject(i).getString("bnfGrpId"));
                                topups_table.setBeneficiaryId(topup.getJSONObject(i).getString("beneficiaryId"));
                                topups_table.setCardNumber(topup.getJSONObject(i).getString("cardNumber"));
                                topups_table.setVoucherId(topup.getJSONObject(i).getString("voucherId"));
                                topups_table.setVoucherValue(topup.getJSONObject(i).getString("voucherValue"));
                                topups_table.setStartDate(new Date(topup.getJSONObject(i).getLong("startDate")));
                                topups_table.setEndDate(new Date(topup.getJSONObject(i).getLong("endDate")));
                                topups_table.setVocherIdNo(topup.getJSONObject(i).getString("voucherIdNo"));
                                topups.add(topups_table);
                            }
                        }
                        getDataManager().getDaoSession().getTopupsDao().insertOrReplaceInTx(topups);
                    }

                    if (ja.has("CardBlock")) {
                        //old query
                        //BlockCards.deleteAll(BlockCards.class);
                        getDataManager().getDaoSession().getBlockCardsDao().deleteAll();
                        List<BlockCards> cardBlocks = new ArrayList<>();
                        JSONArray array = ja.getJSONArray("CardBlock");
                        for (int i = 0; i < array.length(); i++) {
                            BlockCards cards_table = new BlockCards();
                            cards_table.setCardNo(array.getJSONObject(i).getString("cardNumber"));
                            cards_table.setIdentityNo(array.getJSONObject(i).getString("rationNo"));
                            cardBlocks.add(cards_table);
                        }
                        getDataManager().getDaoSession().getBlockCardsDao().insertOrReplaceInTx(cardBlocks);
                    }

                    //if (configurableParameterDetail.isBiometric()) {

                    if (ja.has("Beneficiaries")) {

                        JSONArray members = ja.getJSONArray("Beneficiaries");
                        //old query
                            /*Beneficiary.deleteAll(Beneficiary.class);
                            BeneficiaryBio.deleteAll(BeneficiaryBio.class);*/
                        getDataManager().getDaoSession().getBeneficiaryDao().deleteAll();
                        getDataManager().getDaoSession().getBeneficiaryBioDao().deleteAll();
                        List<Beneficiary> beneficiaries = new ArrayList<>();
                        List<BeneficiaryBio> beneficiaryBioList = new ArrayList<>();
                        for (int i = 0; i < members.length(); i++) {
                            AppLogger.e("Fail POS:", "" + i);
                            Beneficiary beneficiary_table = new Beneficiary();
                            beneficiary_table.setFirstName(members.getJSONObject(i).getString("firstName"));
                            beneficiary_table.setIdentityNo(members.getJSONObject(i).getString("idPassPortNo").trim());
                            beneficiary_table.setGender(members.getJSONObject(i).getString("gender"));
                            beneficiary_table.setDateOfBirth(members.getJSONObject(i).getString("dateOfBirth"));
                            //beneficiary_table.setBeneficiary_group_id(members.getJSONObject(i).getString("bnfGrpId"));
                            beneficiary_table.setActivation(members.getJSONObject(i).getString("activation"));
                            beneficiary_table.setCardPin(members.getJSONObject(i).getString("cardPin"));
                            beneficiary_table.setIsUploaded(members.getJSONObject(i).getString("isUploaded"));
                            beneficiary_table.setBio(members.getJSONObject(i).getBoolean("bioStatus"));
                            if (members.getJSONObject(i).has("cellPhone"))
                                beneficiary_table.setMobile(members.getJSONObject(i).getString("cellPhone"));
                            beneficiary_table.setCardNumber(members.getJSONObject(i).getString("cardNumber"));
                            if (members.getJSONObject(i).has("cardSerialNumber"))
                                beneficiary_table.setCardSerialNumber(members.getJSONObject(i).getString("cardSerialNumber"));

                            if (members.getJSONObject(i).has("bioVerifyStatus"))
                                beneficiary_table.setBioVerifyStatus(members.getJSONObject(i).getString("bioVerifyStatus"));

                            try {
                                if (members.getJSONObject(i).has("createdBy"))
                                    beneficiary_table.setAgentId(members.getJSONObject(i).getString("createdBy"));

                                if (members.getJSONObject(i).has("deviceId"))
                                    beneficiary_table.setDeviceId(members.getJSONObject(i).getString("deviceId"));

                                if (members.getJSONObject(i).has("branchId"))
                                    beneficiary_table.setSectionName(members.getJSONObject(i).getString("branchId"));

                                if (members.getJSONObject(i).has("benfImage"))
                                    beneficiary_table.setImage(members.getJSONObject(i).getString("benfImage"));
                                if (members.getJSONObject(i).has("HouseNumber"))
                                    beneficiary_table.setAddress(members.getJSONObject(i).getString("HouseNumber"));
                                if (members.getJSONObject(i).has("memberId"))
                                    beneficiary_table.setId(Long.parseLong(members.getJSONObject(i).getString("memberId")));
                                if (members.getJSONObject(i).has("memberId"))
                                    beneficiary_table.setBeneficiaryId(members.getJSONObject(i).getString("memberId"));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            try {
                                if (configurableParameterDetail.isBiometric()) {
                                    beneficiary_table.setBio(members.getJSONObject(i).getBoolean("bioStatus"));
                                    if (members.getJSONObject(i).getBoolean("bioStatus")) {
                                        BeneficiaryBio beneficiaryBio = new BeneficiaryBio();
                                        beneficiaryBio.setBeneficiaryId(members.getJSONObject(i).getString("idPassPortNo").trim());

                                        if (!members.getJSONObject(i).isNull("fingerPrint")) {
                                            JSONObject fingers = members.getJSONObject(i).getJSONObject("fingerPrint");
                                            if (fingers.has("rightFinger3"))
                                                beneficiaryBio.setF4(fingers.getString("rightFinger3"));

                                            if (fingers.has("leftFinger2"))
                                                beneficiaryBio.setF1(fingers.getString("leftFinger2"));

                                            if (fingers.has("leftFinger3"))
                                                beneficiaryBio.setF2(fingers.getString("leftFinger3"));

                                            if (fingers.has("rightFinger2"))
                                                beneficiaryBio.setF3(fingers.getString("rightFinger2"));

                                            if (fingers.has("rightThumb"))
                                                beneficiaryBio.setFprt(fingers.getString("rightThumb"));

                                            if (fingers.has("rightIndex"))
                                                beneficiaryBio.setFpri(fingers.getString("rightIndex"));

                                            if (fingers.has("leftFinger1"))
                                                beneficiaryBio.setFplf(fingers.getString("leftFinger1"));

                                            if (fingers.has("leftIndex"))
                                                beneficiaryBio.setFpli(fingers.getString("leftIndex"));

                                            if (fingers.has("leftThumb"))
                                                beneficiaryBio.setFplt(fingers.getString("leftThumb"));

                                            if (fingers.has("rightFinger1"))
                                                beneficiaryBio.setFprf(fingers.getString("rightFinger1"));

                                            beneficiaryBioList.add(beneficiaryBio);
                                        }
                                    }

                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            beneficiaries.add(beneficiary_table);
                        }
                        getDataManager().getDaoSession().getBeneficiaryDao().insertOrReplaceInTx(beneficiaries);
                        getDataManager().getDaoSession().getBeneficiaryBioDao().insertOrReplaceInTx(beneficiaryBioList);
                    }
                }
                //}
                handler.post(() -> {
                    setDialog(false);
                    File path = new File(Environment.getExternalStorageDirectory() +
                            File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.FILE_NAME));
                    AppUtils.deleteFile(path);

                    writeJsonToFile(0);
                    sweetAlert(SweetAlertDialog.SUCCESS_TYPE, getString(R.string.success), getString(R.string.data_receive_success)).setConfirmClickListener(sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        navigateToNext();
                    }).show();
                });
            } else {
                handler.post(() -> {
                    setDialog(false);
                    sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.error), getString(R.string.no_data_found)).show();
                });
            }
        } catch (Exception e) {
            handler.post(() -> {
                setDialog(false);
                writeJsonToFile(1);
                sweetAlert(SweetAlertDialog.ERROR_TYPE, getString(R.string.error), getString(R.string.data_receive_fail))
                        .setConfirmClickListener(sweetAlertDialog -> {
                            sweetAlertDialog.dismissWithAnimation();
                            navigateToNext();
                        }).show();
            });
            e.printStackTrace();
        }
    }

    private void navigateToNext() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void deleteOldFile() {
        File pathVendorData = new File(Environment.getExternalStorageDirectory() +
                File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.FILE_NAME));
        if (pathVendorData.exists()) AppUtils.deleteFile(pathVendorData);

        File pathAckData = new File(Environment.getExternalStorageDirectory() +
                File.separator.concat(AppConstants.FOLDER_NAME).concat("/").concat(AppConstants.ACK_FILE_NAME));
        if (pathAckData.exists())
            AppUtils.deleteFile(pathAckData);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        deleteOldFile();
        localBroadcastManager.unregisterReceiver(receiver);
        TransferService.startStopService(this, false);
        super.onDestroy();
    }
}
