package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_commodity;

import com.compastbc.core.base.MvpPresenter;
import com.compastbc.core.data.network.model.SalesCommodityBean;

import java.util.List;

public interface SelectCommodityMvpPresenter<V extends SelectCommodityMvpView> extends MvpPresenter<V> {

    void getSaleCommodities(String programId, String productId, String categoryId, int offset, String startDate, String endDate);

    SalesCommodityBean getCommodity(List<SalesCommodityBean> beanList, String id);

    String getCashCurrency(String programmeId);
}
