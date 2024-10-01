package com.compastbc.ui.login.pin;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

/**
 * Created by hemant sharma on 12/08/19.
 */

public class PinPresenter<V extends PinMvpView> extends BasePresenter<V>
        implements PinMvpPresenter<V> {

    PinPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void onAttach(V mvpView) {
        super.onAttach(mvpView);
    }


    @Override
    public void verifyInput(String userName, String userPassword) {
        if (userPassword != null && !userPassword.isEmpty()) {
            if (getDataManager().isFirstTime()) {
                getMvpView().openConfigureActivity();
            } else if (getDataManager().getUserDetail().getPassword().equalsIgnoreCase(userPassword)) {
                getDataManager().setLoggedIn(true);
                getMvpView().openLoginActivity();
            } else {
                getMvpView().onError(R.string.InvalidPassword);
            }
        } else
            getMvpView().onError(R.string.Enter_Pin);
    }
}
