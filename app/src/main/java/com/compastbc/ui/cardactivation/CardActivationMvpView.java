package com.compastbc.ui.cardactivation;

import com.compastbc.core.base.MvpView;

import org.json.JSONObject;

public interface CardActivationMvpView extends MvpView {

    void showBeneficiaryDetails(JSONObject object);

    void scanQr();

    void cardActivationSuccess();

}
