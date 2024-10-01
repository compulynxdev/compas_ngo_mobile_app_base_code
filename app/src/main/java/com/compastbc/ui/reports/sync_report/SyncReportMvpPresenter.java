package com.compastbc.ui.reports.sync_report;

import com.compastbc.core.base.MvpPresenter;

public interface SyncReportMvpPresenter<V extends SyncReportMvpView> extends MvpPresenter<V> {

    void getData();

}
