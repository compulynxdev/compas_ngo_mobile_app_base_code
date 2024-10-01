package com.compastbc.ui.transaction.transaction.services.servicedialog;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.db.model.ServicePrices;

import java.util.List;

public interface ServiceDialogMvpPresenter<V extends ServiceDialogMvpView> extends MvpPresenter<V> {

    String getMaxPrice(List<ServicePrices> servicePrices, String uom);
}
