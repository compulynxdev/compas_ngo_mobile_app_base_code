package com.compastbc.ui.transaction.transaction;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.Programs;

import org.json.JSONObject;

import java.util.List;

public interface TransactionMvpView extends MvpView {

    void showPinView(String cardPin, List<Programs> programmesList, JSONObject programData);

    void showBiometricView(List<Programs> programmesList, List<String> fps, JSONObject programData);

    void showProgramList(List<Programs> programmesList);

    void openBeneficiaryActivity();

    void openCartActivity();
}
