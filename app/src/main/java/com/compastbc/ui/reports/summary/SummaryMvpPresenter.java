package com.compastbc.ui.reports.summary;

import com.compastbc.core.base.MvpPresenter;

public interface SummaryMvpPresenter<V extends SummaryMvpView> extends MvpPresenter<V> {

    void getAllDetails();

}
