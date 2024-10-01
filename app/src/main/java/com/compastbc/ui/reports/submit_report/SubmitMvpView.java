package com.compastbc.ui.reports.submit_report;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SubmitTransactionBean;
import com.compastbc.nfcprint.print.PrintServices;

import java.util.List;

public interface SubmitMvpView extends MvpView {

    void doPrintOperation(PrintServices printUtils);

    void setData(List<SubmitTransactionBean> transactionHistories, String amount);

    void openNextActivity();
}
