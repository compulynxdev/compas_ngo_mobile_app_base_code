package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_program;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.SalesProgramBean;

import java.util.List;

public interface SelectProgramMvpPresenter<V extends SelectProgramMvpView> extends MvpPresenter<V> {

    void getPrograms(int offset, String startDate, String endDate);

    SalesProgramBean getProgramBean(List<SalesProgramBean> programBeans, String id);
}
