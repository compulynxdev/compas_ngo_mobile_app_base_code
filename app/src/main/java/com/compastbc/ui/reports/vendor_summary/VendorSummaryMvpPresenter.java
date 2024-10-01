package com.compastbc.ui.reports.vendor_summary;

import com.compastbc.core.base.MvpPresenter;

public interface VendorSummaryMvpPresenter extends MvpPresenter<VendorSummaryMvpView> {

    void getList();
}
