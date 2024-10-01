package com.compastbc.ui.reports.salesbasketreport.categorylist;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesCategoryBean;

import java.util.List;

public interface CategoryMvpView extends MvpView {


    void setData(List<SalesCategoryBean> data);

}
