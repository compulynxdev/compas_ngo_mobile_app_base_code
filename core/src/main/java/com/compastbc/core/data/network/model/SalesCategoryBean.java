package com.compastbc.core.data.network.model;

public class SalesCategoryBean {

    private String categoryId;
    private String categoryName;
    private String totalAmount;
    private String beneficiaryCount;
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
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
}
