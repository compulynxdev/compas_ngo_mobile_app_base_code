package com.compastbc.ui.reports.void_transaction_report;

import com.compastbc.core.base.MvpPresenter;

public interface VoidReportMvpPresenter<V extends VoidReportMvpView> extends MvpPresenter<V> {


    void getTransactionDetails(String startDate, String endDate, int offset);
}
