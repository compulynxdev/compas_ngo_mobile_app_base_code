package com.compastbc.ui.reports.vendor_summary;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.TxnCount;

import java.util.List;

public interface VendorSummaryMvpView extends MvpView {

    void showData(List<TxnCount> list, long ttlCount);
}
