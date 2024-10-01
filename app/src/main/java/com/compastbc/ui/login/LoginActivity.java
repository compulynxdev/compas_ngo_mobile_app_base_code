package com.compastbc.ui.login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.compastbc.R;
import com.compastbc.core.utils.PermissionUtils;
import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.login.dialog.DeviceInfoDialog;
import com.compastbc.ui.login.dialog.LanguageDialog;
import com.compastbc.ui.login.fingerprint.UserFpEnrollActivity;
import com.compastbc.ui.main.MainActivity;
import com.compastbc.ui.settings.SettingsActivity;

public class LoginActivity extends BaseActivity implements LoginMvpView, View.OnClickListener {

    private LoginMvpPresenter<LoginMvpView> mPresenter;
    private EditText et_username, et_password;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, LoginActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mPresenter = new LoginPresenter<>(getDataManager());
        mPresenter.onAttach(this);
        setUp();

        //requestLocationPermission
        PermissionUtils.requestMultiplePermission(this);
    }

    @Override
    protected void setUp() {
        et_username = findViewById(R.id.et_username);
        et_password = findViewById(R.id.et_password);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.img_lang).setOnClickListener(this);
        findViewById(R.id.img_info).setOnClickListener(this);
    }

    @Override
    public void openNextActivity(String userName, int view) {
        switch (view) {
            case 0:
                Intent intent = SettingsActivity.getStartIntent(this);
                getDataManager().setUser(userName);
                startActivity(intent);
                break;

            case 1:
                intent = UserFpEnrollActivity.getStartIntent(this, "Enroll");
                startActivity(intent);
                break;

            case 2:
                intent = UserFpEnrollActivity.getStartIntent(this, "");
                startActivity(intent);
                //todo remove fp bypass login
                //below by pass code
               /* getDataManager().setLoggedIn(true);
                Intent i= MainActivity.getStartIntent(getActivity());
                startActivity(i);*/
                break;

            case 3:
                createLog("Login", "Login success");
                createAttendanceLog();
                intent = MainActivity.getStartIntent(this);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_next) {
            hideKeyboard();
            mPresenter.onNextClick(et_username.getText().toString().trim(), et_password.getText().toString().trim());
        } else if (view.getId() == R.id.img_lang) {
            LanguageDialog.newInstance(langName -> {
                getDataManager().setLanguage(langName);
                finish();
                overridePendingTransition(0, 0);
                startActivity(getIntent());
                overridePendingTransition(0, 0);
            }).show(getSupportFragmentManager());
        } else if (view.getId() == R.id.img_info) {
            DeviceInfoDialog.newInstance().show(getSupportFragmentManager());
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

}
