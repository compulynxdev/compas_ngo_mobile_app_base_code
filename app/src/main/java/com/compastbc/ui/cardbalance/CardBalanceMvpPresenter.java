package com.compastbc.ui.cardbalance;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.db.model.Programs;
import com.compastbc.core.data.network.model.Topups;

import java.util.List;

public interface CardBalanceMvpPresenter<V extends CardBalanceMvpView> extends MvpPresenter<V> {

    void readCardBalance();

    void findBlockCard(String cardNo);

    void findTopups(String cardNo);

    void findPrograms(List<Integer> programId);

    void createBean(List<Topups> topups, List<Programs> programmesList);
}
