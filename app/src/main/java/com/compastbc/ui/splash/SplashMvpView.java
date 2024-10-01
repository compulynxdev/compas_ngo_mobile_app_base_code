package com.compastbc.ui.splash;


import com.compastbc.core.base.MvpView;

/**
 * Created by hemant sharma on 12/08/19.
 */

public interface SplashMvpView extends MvpView {

    void openLoginActivity();

    void openMainActivity(int i);
}
