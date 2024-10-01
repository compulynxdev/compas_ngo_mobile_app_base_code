package com.compastbc.ui.main.appupdate;


import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

/**
 * Created by Hemant on 26/08/19.
 */

class AppUpdateDialogPresenter<V extends AppUpdateDialogMvpView> extends BasePresenter<V>
        implements AppUpdateDialogMvpPresenter<V> {

    public static final String TAG = "AppUpdateDialogPresenter";

    private final boolean isRatingSecondaryActionShown = false;

    AppUpdateDialogPresenter(DataManager dataManager) {
        super(dataManager);
    }

}
