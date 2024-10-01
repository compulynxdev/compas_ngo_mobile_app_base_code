package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_uom;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.Uom;

import java.util.List;

public interface SelectUomMvpPresenter<V extends SelectUomMvpView> extends MvpPresenter<V> {
    void getSaleUom(String programId, String commodityId, int offset, String startDate, String endDate);

    Uom getUom(List<Uom> uoms, String uom);
}
