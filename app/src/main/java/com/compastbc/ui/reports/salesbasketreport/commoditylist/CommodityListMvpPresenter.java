package com.compastbc.ui.reports.salesbasketreport.commoditylist;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.SalesCommodityBean;

import java.util.List;

public interface CommodityListMvpPresenter<V extends CommodityListMvpView> extends MvpPresenter<V> {

    void getSaleCommodities(String programId, String productId, String categoryId, int offset);

    SalesCommodityBean getCommodity(List<SalesCommodityBean> beanList, String id);

    String getCurrency(String programmeId);
}
