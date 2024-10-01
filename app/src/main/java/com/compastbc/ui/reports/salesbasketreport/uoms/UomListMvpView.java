package com.compastbc.ui.reports.salesbasketreport.uoms;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.Uom;

import java.util.List;

public interface UomListMvpView extends MvpView {
    void setData(List<Uom> data);
}
