package com.compastbc.ui.reports.salesbasketreport.uoms;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.Uom;

import java.util.List;

public interface UomListMvpPresenter<V extends UomListMvpView> extends MvpPresenter<V> {

    void getSaleUom(String programId, String commodityId, int offset);

    Uom getUom(List<Uom> uoms, String uom);


}
