package com.compastbc.ui.transaction.transaction.services.servicedialog;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.ServicePrices;

import java.util.List;

public class ServiceDialogPresenter<V extends ServiceDialogMvpView> extends BasePresenter<V>
        implements ServiceDialogMvpPresenter<V> {

    ServiceDialogPresenter(DataManager dataManager) {
        super(dataManager);
    }


    @Override
    public String getMaxPrice(List<ServicePrices> servicePrices, String uom) {

        for (int i = 0; i < servicePrices.size(); i++) {
            if (servicePrices.get(i).getUom().equalsIgnoreCase(uom))
                return String.valueOf(servicePrices.get(i).getMaxPrice());
        }
        return null;
    }
}
