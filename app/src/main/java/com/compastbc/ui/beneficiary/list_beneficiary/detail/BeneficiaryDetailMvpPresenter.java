package com.compastbc.ui.beneficiary.list_beneficiary.detail;

import android.content.Intent;

import com.compastbc.core.base.MvpPresenter;

/**
 * Created by Hemant Sharma on 10-10-19.
 * Divergent software labs pvt. ltd
 */
public interface BeneficiaryDetailMvpPresenter<V extends BeneficiaryDetailMvpView> extends MvpPresenter<V> {
    void getBnfData(Intent intent);

    void updateBeneficiary(String name, String address, String mob, String gender);
}
