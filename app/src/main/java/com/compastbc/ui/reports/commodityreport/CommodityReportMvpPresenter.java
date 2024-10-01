package com.compastbc.ui.reports.commodityreport;

import com.compastbc.core.base.MvpPresenter;

public interface CommodityReportMvpPresenter<V extends CommodityReportMvpView> extends MvpPresenter<V> {

    void onSelectDate();

    void getData(String selectedDate);
}
