package com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesBeneficiary;

import java.util.List;

public interface BeneficiaryUomMvpView extends MvpView {

    void setData(List<SalesBeneficiary> data);

}
