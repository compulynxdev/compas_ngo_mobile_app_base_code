package com.compastbc.ui.settings;


import com.compastbc.core.base.MvpView;

/**
 * Created by hemant sharma on 12/08/19.
 */

public interface SettingsMvpView extends MvpView {
    void openNextActivity(int view);

    void successfullyUpdate();

    void showOnlineApk(String userName);

    void enableDisableNextButton(boolean isEnabled);
}
