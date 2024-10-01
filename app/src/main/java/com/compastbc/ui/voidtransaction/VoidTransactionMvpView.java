package com.compastbc.ui.voidtransaction;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.Transactions;

import org.json.JSONObject;

public interface VoidTransactionMvpView extends MvpView {

    void showDetails(JSONObject object);

    void showDetails(Transactions transactionsList);

    void openNextActivity();

    void hideDialog();
}
