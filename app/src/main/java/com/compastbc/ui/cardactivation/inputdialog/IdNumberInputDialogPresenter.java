package com.compastbc.ui.cardactivation.inputdialog;

import com.compastbc.core.base.BasePresenter;
import com.compastbc.core.data.DataManager;

class IdNumberInputDialogPresenter<V extends IdNumberInputDialogMvpView> extends BasePresenter<V>
        implements IdNumberInputDialogMvpPresenter<V> {

    IdNumberInputDialogPresenter(DataManager dataManager) {
        super(dataManager);
    }

}
