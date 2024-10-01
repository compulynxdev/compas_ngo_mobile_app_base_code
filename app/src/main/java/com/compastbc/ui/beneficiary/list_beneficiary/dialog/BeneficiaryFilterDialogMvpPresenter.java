package com.compastbc.ui.beneficiary.list_beneficiary.dialog;

import com.compastbc.core.base.DialogMvpView;
import com.compastbc.core.base.MvpPresenter;

public interface BeneficiaryFilterDialogMvpPresenter<V extends DialogMvpView> extends MvpPresenter<V> {
    void doVerifyInput();
}
