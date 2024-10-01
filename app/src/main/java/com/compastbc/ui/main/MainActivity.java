package com.compastbc.ui.main;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.compastbc.R;
import com.compastbc.core.data.db.model.SyncLogs;
import com.compastbc.core.data.db.model.SyncLogsDao;
import com.compastbc.core.data.db.model.TransactionsDao;
import com.compastbc.core.data.network.model.HomeBean;
import com.compastbc.core.utils.AppConstants;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.core.utils.PermissionUtils;
import com.compastbc.nfcprint.nfc.NFCReader;
import com.compastbc.nfcprint.nfc.NFCVerifyCallback;
import com.compastbc.ui.agentpassword.ChangeAgentPassword;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.beneficiary.BeneficiaryActivity;
import com.compastbc.ui.cardactivation.CardActivation;
import com.compastbc.ui.cardbalance.CardBalanceActivity;
import com.compastbc.ui.cardformat.CardFormatActivity;
import com.compastbc.ui.cardrestore.CardRestoreActivity;
import com.compastbc.ui.changepassword.ChangePasswordActivity;
import com.compastbc.ui.login.dialog.LanguageDialog;
import com.compastbc.ui.reports.ReportActivity;
import com.compastbc.ui.reports.submit_report.SubmitTransactionReportActivity;
import com.compastbc.ui.settings.SettingsActivity;
import com.compastbc.ui.synchronization.SynchronisationActivity;
import com.compastbc.ui.transaction.transaction.TransactionActivity;
import com.compastbc.ui.update.UpdateActivity;
import com.compastbc.ui.voidtransaction.VoidTransactionActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseActivity implements MainMvpView, NFCVerifyCallback {

    private static final String TAG = "MainActivity";
    private MainMvpPresenter<MainMvpView> mPresenter;
    private boolean doubleBackPress;
    private NFCReader nfcReader;
    private EditText input;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, MainActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mPresenter = new MainPresenter<>(getDataManager(), this);
        mPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (isNetworkConnected(false)) {
            sendEmail();
        }
    }

    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(R.string.Menus);

        Configuration config = getResources().getConfiguration();
        if (config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
            //in Right To Left layout
            title.setPadding(0, 0, 10, 0);
        } else title.setPadding(10, 0, 0, 0);

        List<HomeBean> homeList = mPresenter.getHomeOptions();
        HomeAdapter homeAdapter = new HomeAdapter(homeList, pos -> {
            switch (homeList.get(pos).getPos()) {
                case AppConstants.TRANSACTION_VIEW:
                    createLog(TAG, "Transaction Selected");
                    nfcReader.onNfcStatusListener("CardTxn", this);
                    break;

                case AppConstants.UPDATE_VIEW:
                    createLog(TAG, "Update Selected");
                    startActivity(UpdateActivity.getStartIntent(this));
                    break;

                case AppConstants.CARD_ACTIVATION_VIEW:
                    createLog(TAG, "Card Activation Selected");
                    Intent intent = CardActivation.getStartIntent(this);
                    startActivity(intent);
                    break;

                case AppConstants.BENEFICIARY_VIEW:
                    createLog(TAG, "Beneficiary Selected");
                    startActivity(BeneficiaryActivity.getStartIntent(this));
                    break;

                case AppConstants.CHANGE_CARD_PIN_VIEW:
                    createLog(TAG, "Change Password Selected");
                    nfcReader.onNfcStatusListener("ChangePass", this);
                    break;

                case AppConstants.CARD_BALANCE_VIEW:
                    createLog(TAG, "Card Balance Selected");
                    nfcReader.onNfcStatusListener("CardBalance", this);
                    break;

                case AppConstants.VOID_TRANSACTION_VIEW:
                    createLog(TAG, "Void Transaction Selected");
                    nfcReader.onNfcStatusListener("VoidTxn", this);
                    break;

                case AppConstants.CHANGE_AGENT_PWD_VIEW:
                    createLog(TAG, "Change Agent Password Selected");
                    startActivity(ChangeAgentPassword.getStartIntent(this));
                    break;

                case AppConstants.SETTINGS_VIEW:
                    createLog(TAG, "Settings Selected");
                    startActivity(SettingsActivity.getStartIntent(this));
                    break;

                case AppConstants.FORMAT_CARD_VIEW:
                    createLog(TAG, "Format Card Selected");
                    nfcReader.onNfcStatusListener("CardFormat", this);
                    break;

                case AppConstants.SYNC_VIEW:
                    long transactionCount = getDataManager().getDaoSession().getTransactionsDao().queryBuilder()
                            .where(TransactionsDao.Properties.Submit.eq("0")).count();
                    if (transactionCount > 0) {
                        sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.submit_before_sync)
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    createLog("Report Activity", "Submit report Selected");
                                    startActivity(SubmitTransactionReportActivity.getStartIntent(this));
                                }).show();
                    } else {
                        createLog(TAG, "Synchronisation Selected");
                        startActivity(SynchronisationActivity.getStartIntent(this));
                    }
                    break;

                case AppConstants.REPORTS_VIEW:
                    createLog(TAG, "Reports Selected");
                    intent = ReportActivity.getStartIntent(this);
                    startActivity(intent);
                    break;

                case AppConstants.CARD_RESTORE_VIEW:
                    createLog(TAG, "Card Restore Selected");
                    startActivity(CardRestoreActivity.getStartIntent(this));
                    break;
            }
        });
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(homeAdapter);

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2")) {
            if (PermissionUtils.requestMultiplePermission(this)) {
                automatedSync();
            }
        }

        nfcReader = NFCReader.getInstance(this);
    }

    private void automatedSync() {
        if (!isUploaded && getDataManager().getConfigurableParameterDetail().isAutomated() /*&& !getDataManager().getConfigurableParameterDetail().isOnline()*/) {
            mPresenter.uploadTransactions(false);
            isUploaded = true;
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2")) {
            MenuItem item;
            if(!getDataManager().getConfigurableParameterDetail().isOnline()){
                item = menu.findItem(R.id.updTrans);
                item.setVisible(true);

                item = menu.findItem(R.id.updTopups);
                item.setVisible(true);

                item = menu.findItem(R.id.updArchives);
                item.setVisible(true);

                item = menu.findItem(R.id.updPending);
                item.setVisible(true);

                item = menu.findItem(R.id.updBenf);
                item.setVisible(true);

                if (getDataManager().getConfigurableParameterDetail().isBiometric()) {
                    item = menu.findItem(R.id.updAgent);
                    item.setVisible(true);
                }
            }
            if (getDataManager().getConfigurableParameterDetail().isActivityLog()) {
                item = menu.findItem(R.id.updActivities);
                item.setVisible(true);
            }

            if (getDataManager().getConfigurableParameterDetail().isAttendanceLog()) {
                item = menu.findItem(R.id.updAttendance);
                item.setVisible(true);
            }

        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.LogOut:
                createLog(TAG, "Logout");
                logOut();
                break;

            case R.id.updTrans:
                createLog(TAG, "Upload Transaction");
                mPresenter.uploadTransactions(true);
                break;

            case R.id.updArchives:
                createLog(TAG, "Upload  Archive Transaction");
                mPresenter.uploadArchiveTransactions(true);
                break;

            case R.id.updPending:
                createLog(TAG, "Upload Pending Sync");
                List<SyncLogs> syncLogsList = new ArrayList<>(getDataManager().getDaoSession().getSyncLogsDao().queryBuilder().where(SyncLogsDao.Properties.Status.eq("pending")).where(SyncLogsDao.Properties.Reason.isNull()).orderAsc(
                        SyncLogsDao.Properties.Received_deviceId).list());
                if (!syncLogsList.isEmpty()) {
                    MaterialDialog dialog = new MaterialDialog.Builder(this)
                            .title(getString(R.string.reason_for_not_sync_data) + " " + syncLogsList.get(0).getReceived_deviceId())
                            .customView(R.layout.input_dialog, true)
                            .positiveText(getString(R.string.Ok))
                            .negativeText(android.R.string.cancel)
                            .autoDismiss(false)
                            .onPositive((dialog1, which) -> {
                                if (input.getText().toString().equalsIgnoreCase("")) {
                                    CommonUtils.showToast(this, getString(R.string.empty_fields), Toast.LENGTH_LONG);
                                } else {
                                    List<SyncLogs> list = new ArrayList<>(getDataManager().getDaoSession().getSyncLogsDao().queryBuilder().where(SyncLogsDao.Properties.Status.eq("pending"), SyncLogsDao.Properties.Reason.isNull(),
                                            SyncLogsDao.Properties.Received_deviceId.eq(syncLogsList.get(0).getReceived_deviceId())).list());
                                    if (list.size() > 0) {
                                        for (int j = 0; j < list.size(); j++) {
                                            list.get(j).setReason(input.getText().toString());
                                            getDataManager().getDaoSession().getSyncLogsDao().insertOrReplace(list.get(j));
                                        }
                                    }
                                    dialog1.dismiss();
                                    mPresenter.uploadPendingSynchronisation(true);
                                }
                            })
                            .onNegative((dialog12, which) -> dialog12.dismiss())
                            .build();
                    input = Objects.requireNonNull(dialog.getCustomView()).findViewById(R.id.input);
                    dialog.show();
                } else {
                    mPresenter.uploadPendingSynchronisation(true);
                }
                break;


            case R.id.updTopups:
                createLog(TAG, "Upload Topup logs");
                mPresenter.uploadTopupLogs(true);
                break;

            case R.id.updAgent:
                createLog(TAG, "Upload Agent");
                mPresenter.uploadAgents(true);
                break;

            case R.id.updBenf:
                createLog(TAG, "Upload Beneficiary");
                mPresenter.uploadBeneficiaries(true);
                break;

            case R.id.updAttendance:
                createLog(TAG, "Upload Attendance logs");
                mPresenter.uploadAttendance(true);
                break;

            case R.id.updActivities:
                createLog(TAG, "Upload Activity logs");
                mPresenter.uploadActivities(true);
                break;

            case R.id.menu_language:
                LanguageDialog.newInstance(langName -> {
                    getDataManager().setLanguage(langName);
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                }).show(getSupportFragmentManager());
                break;

            case R.id.menu_app_update:
                doCheckAppVersion(true, null);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onNfcNotSupported() {
        showMessage(R.string.error_nfc_not_support);
    }

    @Override
    public void onNFcDisable() {
        sweetAlert(R.string.alert, R.string.error_nfc_disable)
                .setConfirmText(getString(R.string.msg_enable_nfc))
                .setConfirmClickListener(sweetAlertDialog -> {
                    sweetAlertDialog.dismissWithAnimation();
                    Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                    startActivity(intent);
                }).setCancelText(getString(R.string.cancel)).setCancelClickListener(SweetAlertDialog::dismissWithAnimation)
                .show();
    }

    @Override
    public void onNfcEnable(String TAG) {
        switch (TAG) {
            case "VoidTxn":
                startActivity(VoidTransactionActivity.getStartIntent(getActivity()));
                break;
            case "CardTxn":
                startActivity(TransactionActivity.getStartIntent(getActivity()));
                break;

            case "ChangePass":
                startActivity(ChangePasswordActivity.getStartIntent(getActivity()));
                break;

            case "CardBalance":
                startActivity(CardBalanceActivity.getStartIntent(getActivity()));
                break;

            case "CardFormat":
                startActivity(CardFormatActivity.getStartIntent(getActivity()));
                break;
        }
    }

    @Override
    public void onBackPressed() {
        Handler handler = new Handler();
        Runnable runnable;
        handler.postDelayed(runnable = () -> doubleBackPress = false, 1000);
        if (doubleBackPress) {
            createLog(TAG, "Back");
            handler.removeCallbacks(runnable);
            super.onBackPressed();
        } else {
            onError(getString(R.string.alert_exit));
            doubleBackPress = true;
        }
    }

    @Override
    public void showSubmitReport() {
        startActivity(SubmitTransactionReportActivity.getStartIntent(this));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == AppConstants.REQUEST_MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0) {
                boolean locationPermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                boolean readPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                boolean writePermission = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                boolean cameraPermission = grantResults[3] == PackageManager.PERMISSION_GRANTED;

                if (locationPermission && readPermission && writePermission && cameraPermission) {
                    automatedSync();
                } else {
                    CommonUtils.showToast(getActivity(), getString(R.string.permission_all), Toast.LENGTH_SHORT);
                }
            }
        }
    }
}
