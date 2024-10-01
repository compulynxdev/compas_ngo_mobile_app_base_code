package com.compastbc.ui.beneficiary;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

class BeneficiaryPresenter<V extends BeneficiaryMvpView> extends BasePresenter<V>
        implements BeneficiaryMvpPresenter<V> {

    BeneficiaryPresenter(DataManager dataManager) {
        super(dataManager);
    }
}
