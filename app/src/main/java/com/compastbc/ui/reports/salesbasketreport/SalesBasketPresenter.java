package com.compastbc.ui.reports.salesbasketreport;


import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;


class SalesBasketPresenter<V extends SalesBasketMvpView> extends BasePresenter<V>
        implements SalesBasketMvpPresenter<V> {


    SalesBasketPresenter(DataManager dataManager) {
        super(dataManager);

    }


}
