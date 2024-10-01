package com.compastbc.ui.login.userpin;

import com.compastbc.R;
import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

public class UserPinPresenter extends BasePresenter<UserPinMvpView> implements UserPinMvpPresenter {

    UserPinPresenter(DataManager dataManager) {
        super(dataManager);
    }

    @Override
    public void verifyInput(String password) {
        if (password.equalsIgnoreCase(getDataManager().getUserDetail().getPassword())) {
            getDataManager().setLoggedIn(true);
            getMvpView().openMainActivity();
        } else {
            getMvpView().showMessage(R.string.invalid_password);
        }
    }
}
