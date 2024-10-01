package com.compastbc.ui.reports.salesbasketreport;

import com.compastbc.core.base.MvpView;
import com.compastbc.nfcprint.print.PrintServices;

public interface SalesBasketMvpView extends MvpView {

    void doPrintOperation(PrintServices printUtils);

}
