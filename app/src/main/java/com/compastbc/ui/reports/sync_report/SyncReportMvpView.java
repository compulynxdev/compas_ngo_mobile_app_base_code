package com.compastbc.ui.reports.sync_report;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SyncReportModel;

import java.util.List;

public interface SyncReportMvpView extends MvpView {

    void setData(List<SyncReportModel> models, String count, String totalTxn, String totalAmount);

}
