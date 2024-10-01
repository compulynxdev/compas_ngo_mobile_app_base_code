package com.compastbc.ui.reports.salesbasketreport.programlist;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesProgramBean;

import java.util.List;

public interface ProgramFragmentMvpView extends MvpView {
    void setData(List<SalesProgramBean> data);

}
