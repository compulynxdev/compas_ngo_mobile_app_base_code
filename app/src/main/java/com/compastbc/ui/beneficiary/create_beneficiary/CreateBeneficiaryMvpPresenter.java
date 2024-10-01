package com.compastbc.ui.beneficiary.create_beneficiary;

import com.compastbc.core.base.MvpPresenter;

public interface CreateBeneficiaryMvpPresenter<V extends CreateBeneficiaryMvpView> extends MvpPresenter<V> {


    void onSelectDate();

    void verifyInputs(String firstName, String lastName, String gender, String address, String dob, String signature, String idno, String mobile);

}
