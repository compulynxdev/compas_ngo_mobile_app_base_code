package com.compastbc.ui.transaction.beneficiary_fp_verification;

import com.compastbc.core.base.MvpPresenter;

public interface BeneficiaryVerifyMvpPresenter<V extends BeneficiaryVerifyMvpView> extends MvpPresenter<V> {

    void onViewLoaded();

    void onClick();

}
