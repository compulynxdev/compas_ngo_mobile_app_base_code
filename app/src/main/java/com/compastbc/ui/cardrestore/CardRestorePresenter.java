package com.compastbc.ui.cardrestore;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;
import com.compastbc.core.data.db.model.NFCCardData;

import java.util.List;

class CardRestorePresenter<V extends CardRestoreMvpView> extends BasePresenter<V>
        implements CardRestoreMvpPresenter<V> {

    CardRestorePresenter(DataManager dataManager) {
        super(dataManager);
    }

    public List<NFCCardData> getNFCCardList() {
        return getDataManager().getDaoSession().getNFCCardDataDao().loadAll();
    }
}
