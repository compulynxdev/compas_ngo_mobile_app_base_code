package com.compastbc.ui.reports;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.HomeBean;

import java.util.List;

interface ReportsMvpPresenter<V extends ReportsMvpView> extends MvpPresenter<V> {
    List<HomeBean> getHomeOptions();
}
