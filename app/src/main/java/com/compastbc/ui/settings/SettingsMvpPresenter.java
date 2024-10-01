package com.compastbc.ui.settings;


import com.compastbc.core.base.MvpPresenter;

/**
 * Created by hemant sharma on 12/08/19.
 */

public interface SettingsMvpPresenter<V extends SettingsMvpView> extends MvpPresenter<V> {

    void onClickNext(String ip, String port);

    void Update(String ip, String port);

    void getApkType(String accountId);

    void insertDummyTxnData();

}
