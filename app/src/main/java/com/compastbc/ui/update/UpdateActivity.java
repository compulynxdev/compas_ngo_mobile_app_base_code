package com.compastbc.ui.update;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.data.network.Webservices;
import com.compastbc.core.utils.AppLogger;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.login.LoginActivity;
import com.compastbc.ui.main.appupdate.AppUpdateDialog;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class UpdateActivity extends BaseActivity implements UpdateMvpView, View.OnClickListener {

    private UpdateMvpPresenter<UpdateMvpView> presenter;
    private boolean isDataAvailableForSync;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, UpdateActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        presenter = new UpdatePresenter<>(this, getDataManager());
        presenter.onAttach(this);
        isDataAvailableForSync = checkDBForSync();
        setUp();
    }

    @Override
    protected void setUp() {
        TextView title = findViewById(R.id.tvTitle);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title.setText(getString(R.string.Update));
        ImageView img_back = findViewById(R.id.img_back);
        img_back.setVisibility(View.VISIBLE);
        img_back.setOnClickListener(this);

        Button btnMaster, btnCardHolders, btnTopups;
        btnMaster = findViewById(R.id.btn_master);
        btnMaster.setOnClickListener(this);
        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2")) {
            btnCardHolders = findViewById(R.id.btn_benficiaries);
            btnCardHolders.setVisibility(View.VISIBLE);
            btnCardHolders.setOnClickListener(this);
            btnTopups = findViewById(R.id.btn_topups);
            btnTopups.setVisibility(View.VISIBLE);
            btnTopups.setOnClickListener(this);
        }
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.img_back:
                onBackPressed();
                break;

            case R.id.btn_master:
                createLog("Update", "Master");
                if (isNetworkConnected()) {
                    if (isDataAvailableForSync) {
                        doCheckAppVersion(false, new AppUpdateCallback() {
                            @Override
                            public void onShowAppUpdateUI(String appName, Bundle bundle) {
                                showAppUpdateUI(appName, bundle);
                            }

                            @Override
                            public void onCallNextApi() {
                                getAccessToken(() -> presenter.Master());
                            }

                            @Override
                            public void onFail() {
                                getAccessToken(() -> presenter.Master());
                            }
                        });
                    } else {
                        sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.Pleasesyncdatawithserver))
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                    }
                }
                break;

            case R.id.btn_benficiaries:
                createLog("Update", "Beneficiary");
                if (isNetworkConnected()) {
                    if (isDataAvailableForSync) {
                        doCheckAppVersion(false, new AppUpdateCallback() {
                            @Override
                            public void onShowAppUpdateUI(String appName, Bundle bundle) {
                                showAppUpdateUI(appName, bundle);
                            }

                            @Override
                            public void onCallNextApi() {
                                getAccessToken(() -> presenter.CardHolders());
                            }

                            @Override
                            public void onFail() {
                                getAccessToken(() -> presenter.CardHolders());
                            }
                        });
                    } else {
                        sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.Pleasesyncdatawithserver))
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                    }
                }
                break;

            case R.id.btn_topups:
                createLog("Update", "Topups");
                if (isNetworkConnected()) {
                    if (isDataAvailableForSync) {
                        doCheckAppVersion(false, new AppUpdateCallback() {
                            @Override
                            public void onShowAppUpdateUI(String appName, Bundle bundle) {
                                showAppUpdateUI(appName, bundle);
                            }

                            @Override
                            public void onCallNextApi() {
                                getAccessToken(() -> presenter.Topups());
                            }

                            @Override
                            public void onFail() {
                                getAccessToken(() -> presenter.Topups());
                            }
                        });
                    } else {
                        sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), "Please sync data with server before updating.")
                                .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
                    }
                }
                break;
        }

    }

    @Override
    public void openNextActivity() {
        startActivity(LoginActivity.getStartIntent(this));
        finish();
    }

    private void showAppUpdateUI(String appName, Bundle bundle) {
        AppUpdateDialog.newInstance(bundle, () -> {
            String finalAppPath = Webservices.getApkDownloadPath(appName);
            AppLogger.e("AppUpdate", finalAppPath);
            if (finalAppPath.contains(".apk"))
                beginAppDownload(finalAppPath, appName);
            else {
                sweetAlert(SweetAlertDialog.WARNING_TYPE, getString(R.string.alert), getString(R.string.alert_apk_issue))
                        .setConfirmClickListener(SweetAlertDialog::dismissWithAnimation).show();
            }
        }).show(getSupportFragmentManager(), "");
    }
}
