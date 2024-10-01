package com.compastbc.ui.transaction.transaction.vouchers;

import com.compastbc.core.base.MvpPresenter;

public interface VouchersMvpPresenter<V extends VouchersMvpView> extends MvpPresenter<V> {

    void getVouchersByProgramId(int programId);

    void getTopups();
}
