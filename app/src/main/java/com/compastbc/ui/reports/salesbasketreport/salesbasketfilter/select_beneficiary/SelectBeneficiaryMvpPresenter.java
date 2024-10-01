package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_beneficiary;

import com.compastbc.core.base.MvpPresenter;

public interface SelectBeneficiaryMvpPresenter<V extends SelectBeneficiaryMvpView> extends MvpPresenter<V> {
    void getBeneficiaryDetails(String programId, String commodityId, String uom, int offset, String startDate, String endDate);
}
