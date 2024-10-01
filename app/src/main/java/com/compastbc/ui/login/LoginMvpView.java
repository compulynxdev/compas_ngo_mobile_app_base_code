package com.compastbc.ui.login;


import com.compastbc.core.base.MvpView;

/**
 * Created by hemant sharma on 12/08/19.
 */

public interface LoginMvpView extends MvpView {
    void openNextActivity(String userName, int view);
}
