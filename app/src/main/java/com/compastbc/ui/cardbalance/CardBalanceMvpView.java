package com.compastbc.ui.cardbalance;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.CardBalanceBean;

import java.util.List;

public interface CardBalanceMvpView extends MvpView {

    void showBalance(String identityNo, String name, String cardNo, List<CardBalanceBean> cardBalanceBeans);

}
