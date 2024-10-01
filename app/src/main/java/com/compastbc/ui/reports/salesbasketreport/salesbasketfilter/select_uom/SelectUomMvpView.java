package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_uom;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.Uom;

import java.util.List;

public interface SelectUomMvpView extends MvpView {
    void setData(List<Uom> data);

    void dismissDialogView();
}
