package com.compastbc.ui.beneficiary.create_beneficiary;

import com.compastbc.core.base.MvpView;

public interface CreateBeneficiaryMvpView extends MvpView {

    void setDate(String date);

    void openNextActivity();

}
