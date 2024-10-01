package com.compastbc.ui.settings;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.compastbc.R;
import com.compastbc.core.utils.AppUtils;
import com.compastbc.core.utils.CommonUtils;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.cardformat.CardFormatActivity;
import com.compastbc.ui.login.LoginActivity;
import com.compastbc.ui.login.fingerprint.UserFpEnrollActivity;
import com.compastbc.ui.main.MainActivity;

import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SettingsActivity extends BaseActivity implements SettingsMvpView, View.OnClickListener {

    private SettingsMvpPresenter<SettingsMvpView> settingsMvpPresenter;
    private EditText ip, port;
    private ImageView img_next;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        settingsMvpPresenter = new SettingsPresenter<>(getDataManager());
        settingsMvpPresenter.onAttach(this);
        setUp();
    }

    @Override
    protected void setUp() {
        TextView tv_title = findViewById(R.id.tvTitle);
        ImageView img_back = findViewById(R.id.img_back);
        TextView deviceId = findViewById(R.id.deviceId);
        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        Button btnUpdate = findViewById(R.id.btn_update);
        img_next = findViewById(R.id.next);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_title.setText(R.string.Settings);
        img_back.setVisibility(View.VISIBLE);

        TextView tv_version = findViewById(R.id.tv_version);
        tv_version.setText(getString(R.string.compas_tbc_v).concat(AppUtils.getAppVersionName(SettingsActivity.this)));

        deviceId.setText(CommonUtils.getDeviceId(SettingsActivity.this));
        getDataManager().setDeviceId(CommonUtils.getDeviceId(SettingsActivity.this));
        img_back.setOnClickListener(this);

        if (!getDataManager().getConfigurationDetail().getUrl().isEmpty()) {
            ip.setText(getDataManager().getConfigurationDetail().getUrl());
            if (getDataManager().getConfigurationDetail().getPort().matches("^[a-zA-Z0-9]*$"))
                port.setText(String.format(Locale.getDefault(), "%s", getDataManager().getConfigurationDetail().getPort()));
            else
                port.setText(String.format(Locale.getDefault(), "%d", Integer.parseInt(getDataManager().getConfigurationDetail().getPort())));
        }
        if (getDataManager().isFirstTime()) {
            img_next.setVisibility(View.VISIBLE);
            img_next.setOnClickListener(this);
        } else {
            TextView tv_app_mode = findViewById(R.id.tv_app_mode);
            tv_app_mode.setVisibility(View.VISIBLE);
            tv_app_mode.setText(getDataManager().getConfigurableParameterDetail().isOnline() ? R.string.app_mode_online : R.string.app_mode_offline);

            btnUpdate.setVisibility(View.VISIBLE);
            btnUpdate.setOnClickListener(this);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);

        if (getDataManager().getUserDetail().getLevel().equalsIgnoreCase("2") && !getDataManager().isFirstTime()) {
            MenuItem item = menu.findItem(R.id.menu_format);
            item.setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_format) {
            createLog("Setting Activity", "CleanFormat");
            show(sweetAlert(SweetAlertDialog.WARNING_TYPE, R.string.alert, R.string.card_clear_format)
                    .setConfirmButton(R.string.dialog_ok, sweetAlertDialog -> {
                        sweetAlertDialog.dismissWithAnimation();
                        Intent intent = CardFormatActivity.getStartIntent(getActivity());
                        intent.putExtra("CleanFormat", true);
                        startActivity(intent);
                    })
                    .setCancelButton(R.string.dialog_cancel, SweetAlertDialog::dismissWithAnimation));
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void openNextActivity(int view) {
        switch (view) {
            case 0:
                createLog("Login", "Login success");
                createAttendanceLog();
                Intent intent = MainActivity.getStartIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
            case 1:
                intent = UserFpEnrollActivity.getStartIntent(this, "enroll");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;

            case 2:
                intent = UserFpEnrollActivity.getStartIntent(this, "");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;

            case 3:
                intent = LoginActivity.getStartIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    public void successfullyUpdate() {
        createLog("Settings Activity", "Successfully updated");
        onBackPressed();
    }

    @Override
    public void showOnlineApk(String userName) {
        showMessage(getString(R.string.This).concat(" ").concat(userName).concat(" ").concat(getString(R.string.user).concat(" ").concat(getString(R.string.apkIsOnline))));
    }

    @Override
    public void enableDisableNextButton(boolean isEnabled) {
        img_next.setEnabled(isEnabled);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                createLog("Settings Activity", "Back");
                onBackPressed();
                break;

            case R.id.next:
                enableDisableNextButton(false);
                createLog("Settings Activity", "Next Clicked");
                settingsMvpPresenter.onClickNext(ip.getText().toString().trim(), AppUtils.replaceNonstandardDigits(String.format(Locale.US, "%s", port.getText().toString().trim())));
                break;

            case R.id.btn_update:
                createLog("Settings Activity", "Update clicked");
                settingsMvpPresenter.Update(ip.getText().toString().trim(), AppUtils.replaceNonstandardDigits(String.format(Locale.US, "%s", port.getText().toString().trim())));
                break;
        }
    }
}
