package com.compastbc.core.data.network.model;

import java.util.List;

public class SalesBasketReportModel {

    public List<SalesProgramBean> salesProgramBeans;
    public SalesProgramBean programBean;

    public List<SalesCategoryBean> salesCategoryBeans;
    public SalesCategoryBean categoryBean;

    public List<SalesCommodityBean> salesCommodityBeans;
    public SalesCommodityBean commodityBean;

    public List<Uom> uomList;
    public Uom uom;

    public List<SalesBeneficiary> salesBeneficiaries;
    public SalesBeneficiary salesBeneficiary;
}
