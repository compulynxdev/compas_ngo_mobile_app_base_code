package com.compastbc.core.data.network.model;

public class SalesCommodityBean {

    private String commodityId;
    private String commodityName;
    private String totalAmount;
    private String beneficiaryCount;
    private String commodityType;
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCommodityId() {
        return commodityId;
    }

    public void setCommodityId(String commodityId) {
        this.commodityId = commodityId;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getBeneficiaryCount() {
        return beneficiaryCount == null ? "0" : beneficiaryCount;
    }

    public void setBeneficiaryCount(String beneficiaryCount) {
        this.beneficiaryCount = beneficiaryCount;
    }

    public String getCommodityType() {
        return commodityType;
    }

    public void setCommodityType(String commodityType) {
        this.commodityType = commodityType;
    }
}
