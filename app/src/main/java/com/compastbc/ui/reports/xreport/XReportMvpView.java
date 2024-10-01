package com.compastbc.ui.reports.xreport;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.XReportBean;
import com.compastbc.nfcprint.print.PrintServices;

import java.util.List;

public interface XReportMvpView extends MvpView {

    void showData(List<XReportBean> xReportBean);

    void doPrintOperation(PrintServices printUtils);
}
