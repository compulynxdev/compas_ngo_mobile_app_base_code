package com.compastbc.core.data.network.model;

public class SalesProgramBean {

    private String programId;
    private String programName;
    private String totalAmount;
    private String beneficiaryCount;
    private String productId;
    private String currency;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
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
