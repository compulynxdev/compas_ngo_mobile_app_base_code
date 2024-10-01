package com.compastbc.ui.transaction.transaction.vouchers;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.db.model.Vouchers;

import java.util.List;

public interface VouchersMvpView extends MvpView {

    void showVouchers(List<Vouchers> list);
}
