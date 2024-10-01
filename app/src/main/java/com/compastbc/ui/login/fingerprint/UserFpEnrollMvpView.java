package com.compastbc.ui.login.fingerprint;


import com.compastbc.core.base.MvpView;

import java.util.List;

/**
 * Created by hemant sharma on 12/08/19.
 */

public interface UserFpEnrollMvpView extends MvpView {
    void openNextActivity(int flag);

    void showVerifyView(List<String> fps);
}
