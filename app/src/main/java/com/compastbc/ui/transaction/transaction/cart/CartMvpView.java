package com.compastbc.ui.transaction.transaction.cart;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.TransactionReceipt;
import com.compastbc.nfcprint.print.PrintServices;


public interface CartMvpView extends MvpView {

    void show(String price, String qty);

    void Update(String id);

    void setData();

    void openNextActivity();

    void openServices();

    void hideDialog();

    void print(TransactionReceipt receipt, boolean vendorReceipt);

    void printReceipt(PrintServices printUtils, TransactionReceipt receipt, boolean vendorReceipt);
}
