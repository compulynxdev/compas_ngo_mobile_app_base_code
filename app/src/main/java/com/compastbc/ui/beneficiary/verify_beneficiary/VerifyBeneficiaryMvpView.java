package com.compastbc.ui.beneficiary.verify_beneficiary;

import com.compastbc.core.base.MvpView;

import org.json.JSONObject;

public interface VerifyBeneficiaryMvpView extends MvpView {
    void reEnrollFingerPrint(JSONObject object);

    void openNextActivity();

    void openPreviousActivity();

}
