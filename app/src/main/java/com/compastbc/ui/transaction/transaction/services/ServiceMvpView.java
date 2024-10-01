package com.compastbc.ui.transaction.transaction.services;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.ServicePrices;
import com.compastbc.core.data.db.model.Services;

import java.util.List;

public interface ServiceMvpView extends MvpView {

    void showServices(List<Services> servicesList);

    void showDialog(List<ServicePrices> servicePrices);
}
