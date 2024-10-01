package com.compastbc.ui.reports.xreport;

import com.compastbc.core.base.MvpPresenter;

public interface XReportMvpPresenter<V extends XReportMvpView> extends MvpPresenter<V> {

    void getXreportData();
}
