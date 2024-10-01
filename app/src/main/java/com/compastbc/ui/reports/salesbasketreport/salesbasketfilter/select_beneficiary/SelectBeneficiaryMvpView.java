package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_beneficiary;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesBeneficiary;

import java.util.List;

public interface SelectBeneficiaryMvpView extends MvpView {
    void setData(List<SalesBeneficiary> data);

    void dismissDialogView();
}
