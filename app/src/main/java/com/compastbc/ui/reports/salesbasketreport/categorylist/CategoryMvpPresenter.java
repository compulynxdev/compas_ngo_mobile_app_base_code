package com.compastbc.ui.reports.salesbasketreport.categorylist;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.SalesCategoryBean;

import java.util.List;

public interface CategoryMvpPresenter<V extends CategoryMvpView> extends MvpPresenter<V> {

    void getSaleCatrgories(String programId, String productId, int offset);

    SalesCategoryBean getCategory(List<SalesCategoryBean> salesCategoryBeans, String id);

}
