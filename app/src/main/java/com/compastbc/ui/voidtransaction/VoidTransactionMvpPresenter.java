package com.compastbc.ui.voidtransaction;

import com.compastbc.core.base.MvpPresenter;

public interface VoidTransactionMvpPresenter<V extends VoidTransactionMvpView> extends MvpPresenter<V> {

    // void readCardDetails();

    void getLastTransaction(String cardNo);

    void setLastTransaction(String receiptNo);

    void getTransaction(String trim);
}
