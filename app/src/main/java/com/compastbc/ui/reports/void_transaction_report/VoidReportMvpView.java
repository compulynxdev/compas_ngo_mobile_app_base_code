package com.compastbc.ui.reports.void_transaction_report;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.nfcprint.print.PrintServices;

import java.util.List;

public interface VoidReportMvpView extends MvpView {
    void setData(List<TransactionHistory> transactionHistories);

    void doPrintOperation(PrintServices printUtils);
}
