package com.compastbc.ui.reports.salesbasketreport.programlist;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.SalesProgramBean;

import java.util.List;

public interface ProgramFragmentMvpPresenter<V extends ProgramFragmentMvpView> extends MvpPresenter<V> {

    SalesProgramBean getProgramBean(List<SalesProgramBean> programBeans, String id);

    void getProgrammesData(int offset);
}
