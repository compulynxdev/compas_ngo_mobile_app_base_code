package com.compastbc.ui.transaction.transaction.cart;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.db.model.PurchasedProducts;

import java.util.List;

public interface CartMvpPresenter<V extends CartMvpView> extends MvpPresenter<V> {

    void getData(List<PurchasedProducts> purchasedProducts);

    void Update(Long id);

    void readCardDetails();

    void saveData();

}
