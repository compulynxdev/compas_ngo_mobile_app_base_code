package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_category;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.SalesCategoryBean;

import java.util.List;

public interface SelectCategoryMvpPresenter<V extends SelectCategoryMvpView> extends MvpPresenter<V> {

    void getSaleCatrgories(String programId, String productId, int offset, String startDate, String endDate);

    SalesCategoryBean getCategory(List<SalesCategoryBean> salesCategoryBeans, String id);
}
