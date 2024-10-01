package com.compastbc.ui.reports.sales_transaction_history;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.TransactionHistory;
import com.compastbc.nfcprint.print.PrintServices;

import java.util.List;

public interface SalesTransactionMvpView extends MvpView {

    void setData(List<TransactionHistory> transactionHistories);

    void doPrintOperation(PrintServices printUtils);

    void createPdf();

    void doSearch(int page, String search);
}
