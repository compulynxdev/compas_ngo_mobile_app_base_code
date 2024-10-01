package com.compastbc.ui.transaction.transaction.services;

import com.compastbc.core.base.MvpPresenter;

public interface ServiceMvpPresenter<V extends ServiceMvpView> extends MvpPresenter<V> {

    void getCommodities();

    void getUoms(String Id);
}
