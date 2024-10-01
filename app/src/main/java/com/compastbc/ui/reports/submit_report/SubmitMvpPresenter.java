package com.compastbc.ui.reports.submit_report;

import com.compastbc.core.base.MvpPresenter;

public interface SubmitMvpPresenter<V extends SubmitMvpView> extends MvpPresenter<V> {

    void getData();

    void updateTransactions();

}
