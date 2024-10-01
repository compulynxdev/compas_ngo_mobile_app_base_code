package com.compastbc.ui.reports.salesbasketreport.salesbasketfilter.select_commodity;

import com.compastbc.core.base.MvpView;
import com.compastbc.core.data.network.model.SalesCommodityBean;

import java.util.List;

public interface SelectCommodityMvpView extends MvpView {
    void setData(List<SalesCommodityBean> data);

    void dismissDialogView();
}
