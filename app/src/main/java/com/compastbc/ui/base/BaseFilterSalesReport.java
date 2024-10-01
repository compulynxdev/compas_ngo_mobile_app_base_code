package com.compastbc.ui.base;

public abstract class BaseFilterSalesReport extends BaseFragment {
    private static String programmeId;
    private static String productId;
    private static String categoryId;
    private static String commodityId;
    private static String uom;
    private static String currency;
    private static String commodityName;
    private static String benfName;

    public static String getCurrency() {
        return currency;
    }

    public static void setCurrency(String currency) {
        BaseFilterSalesReport.currency = currency;
    }

    public static String getBenfName() {
        return benfName;
    }

    public static void setBenfName(String benfName) {
        BaseFilterSalesReport.benfName = benfName;
    }

    public static String getProgrammeId() {
        return programmeId;
    }

    public static void setProgrammeId(String programmeId) {
        BaseFilterSalesReport.programmeId = programmeId;
    }

    public static String getProductId() {
        return productId;
    }

    public static void setProductId(String productId) {
        BaseFilterSalesReport.productId = productId;
    }

    public static String getCategoryId() {
        return categoryId;
    }

    public static void setCategoryId(String categoryId) {
        BaseFilterSalesReport.categoryId = categoryId;
    }

    public static String getCommodityId() {
        return commodityId;
    }

    public static void setCommodityId(String commodityId) {
        BaseFilterSalesReport.commodityId = commodityId;
    }

    public static String getUom() {
        return uom;
    }

    public static void setUom(String uom) {
        BaseFilterSalesReport.uom = uom;
    }

    public static String getCommodityName() {
        return commodityName;
    }

    public static void setCommodityName(String commodityName) {
        BaseFilterSalesReport.commodityName = commodityName;
    }
}
