package com.compastbc.ui.reports.salesbasketreport.commoditylist;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesCommodityBean;

import java.util.List;

public interface CommodityListMvpView extends MvpView {

    void setData(List<SalesCommodityBean> data);
}
