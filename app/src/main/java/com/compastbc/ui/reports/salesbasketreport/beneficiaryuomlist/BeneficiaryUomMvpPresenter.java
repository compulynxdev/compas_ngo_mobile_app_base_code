package com.compastbc.ui.reports.salesbasketreport.beneficiaryuomlist;

import com.compastbc.core.base.MvpPresenter;

public interface BeneficiaryUomMvpPresenter<V extends BeneficiaryUomMvpView> extends MvpPresenter<V> {

    void getBeneficiaryDetails(String programId, String commodityId, String uom, int offset);
}
