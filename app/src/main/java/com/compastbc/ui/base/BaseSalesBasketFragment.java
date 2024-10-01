package com.compastbc.ui.base;

public abstract class BaseSalesBasketFragment extends BaseFragment {

    private static String programmeId;
    private static String productId;
    private static String categoryId;
    private static String commodityId;
    private static String uom;
    private static String commodityName;
    private static String currency;

    public static String getProgrammeId() {
        return programmeId;
    }

    public static void setProgrammeId(String programmeId) {
        BaseSalesBasketFragment.programmeId = programmeId;
    }

    public static String getProductId() {
        return productId;
    }

    public static void setProductId(String productId) {
        BaseSalesBasketFragment.productId = productId;
    }

    public static String getCurrency() {
        return currency;
    }

    public static void setCurrency(String currency) {
        BaseSalesBasketFragment.currency = currency;
    }

    public static String getCategoryId() {
        return categoryId;
    }

    public static void setCategoryId(String categoryId) {
        BaseSalesBasketFragment.categoryId = categoryId;
    }

    public static String getCommodityId() {
        return commodityId;
    }

    public static void setCommodityId(String commodityId) {
        BaseSalesBasketFragment.commodityId = commodityId;
    }

    public static String getCommodityName() {
        return commodityName;
    }

    public static void setCommodityName(String commodityName) {
        BaseSalesBasketFragment.commodityName = commodityName;
    }

    public static String getUom() {
        return uom;
    }

    public static void setUom(String uom) {
        BaseSalesBasketFragment.uom = uom;
    }
}
