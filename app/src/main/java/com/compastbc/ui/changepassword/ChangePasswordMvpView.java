package com.compastbc.ui.changepassword;

import com.compastbc.core.base.MvpView;

public interface ChangePasswordMvpView extends MvpView {

    void showDetails(String name, String idno);

    void verifyInputs(String newPass, String confirmPass);

    void setUpdate(boolean update);

    void changePasswordSuccess();

    void hideDialog();
}
