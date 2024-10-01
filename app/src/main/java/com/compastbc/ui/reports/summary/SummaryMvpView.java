package com.compastbc.ui.reports.summary;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SummaryReportBean;
import com.compastbc.nfcprint.print.PrintServices;

public interface SummaryMvpView extends MvpView {

    void showData(SummaryReportBean summaryReportBean);

    void doPrintOperation(PrintServices printUtils);

}
