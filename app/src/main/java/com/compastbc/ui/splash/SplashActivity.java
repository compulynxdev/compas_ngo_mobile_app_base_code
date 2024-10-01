package com.compastbc.ui.splash;

import android.content.Intent;
import android.os.Bundle;

import com.compastbc.ui.base.BaseActivity;
import com.compastbc.ui.login.LoginActivity;
import com.compastbc.ui.login.fingerprint.UserFpEnrollActivity;
import com.compastbc.ui.login.userpin.UserPinActivity;

public class SplashActivity extends BaseActivity implements SplashMvpView {

    private SplashMvpPresenter<SplashMvpView> mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPresenter = new SplashPresenter<>(getDataManager());
        mPresenter.onAttach(SplashActivity.this);
        setUp();
    }

    @Override
    public void openLoginActivity() {
        Intent intent = LoginActivity.getStartIntent(SplashActivity.this);
        startActivity(intent);
        finish();
    }

    @Override
    public void openMainActivity(int i) {
        Intent intent;
        switch (i) {
            case 0:
                intent = UserPinActivity.getStartIntent(SplashActivity.this);
                startActivity(intent);
                finish();
                break;

            case 1:
                intent = UserFpEnrollActivity.getStartIntent(SplashActivity.this, "");
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        mPresenter.onDetach();
        super.onDestroy();
    }

    @Override
    protected void setUp() {
    }
}
