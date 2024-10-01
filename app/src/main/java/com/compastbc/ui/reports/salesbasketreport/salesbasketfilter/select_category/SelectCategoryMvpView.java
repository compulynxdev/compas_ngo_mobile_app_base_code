package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_category;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesCategoryBean;

import java.util.List;

public interface SelectCategoryMvpView extends MvpView {
    void setData(List<SalesCategoryBean> data);

    void dismissDialogView();
}
