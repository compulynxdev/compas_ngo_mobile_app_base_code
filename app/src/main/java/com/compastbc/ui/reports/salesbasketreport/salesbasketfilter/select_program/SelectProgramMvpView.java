package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_program;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesProgramBean;

import java.util.List;

public interface SelectProgramMvpView extends MvpView {
    void setData(List<SalesProgramBean> data);

    void dismissDialogView();
}
