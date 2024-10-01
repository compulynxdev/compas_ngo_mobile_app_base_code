package com.compastbc.ui.reports.sales_transaction_history;

import android.widget.EditText;

import com.compastbc.core.base.MvpPresenter;

public interface SalesTransactionMvpPresenter<V extends SalesTransactionMvpView> extends MvpPresenter<V> {

    void setupSearch(EditText etSearch);

    void getTransactionDetails(String search, String startDate, String endDate, int offset);
}
