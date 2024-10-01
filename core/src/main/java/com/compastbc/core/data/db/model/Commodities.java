package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "Commodities")
public class Commodities {

    public String productName;
    public String programId;
    public String beneficiaryName;
    @Id
    private Long id;
    private String productId;
    private String quantityDeducted;
    private String transactionNo;
    private String uom;
    private String categoryId;
    @Unique
    private Long uniqueId;
    private String date;
    private String voidTransaction;
    private double totalAmountChargedByRetailer;
    private String identificationNum;
    private String maxPrice;

    @Generated(hash = 1194255643)
    public Commodities(String productName, String programId, String beneficiaryName, Long id,
            String productId, String quantityDeducted, String transactionNo, String uom,
            String categoryId, Long uniqueId, String date, String voidTransaction,
            double totalAmountChargedByRetailer, String identificationNum, String maxPrice) {
        this.productName = productName;
        this.programId = programId;
        this.beneficiaryName = beneficiaryName;
        this.id = id;
        this.productId = productId;
        this.quantityDeducted = quantityDeducted;
        this.transactionNo = transactionNo;
        this.uom = uom;
        this.categoryId = categoryId;
        this.uniqueId = uniqueId;
        this.date = date;
        this.voidTransaction = voidTransaction;
        this.totalAmountChargedByRetailer = totalAmountChargedByRetailer;
        this.identificationNum = identificationNum;
        this.maxPrice = maxPrice;
    }

    @Generated(hash = 1438506391)
    public Commodities() {
    }

    public String getIdentificationNum() {
        return identificationNum;
    }

    public void setIdentificationNum(String identificationNum) {
        this.identificationNum = identificationNum;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getQuantityDeducted() {
        return quantityDeducted;
    }

    public void setQuantityDeducted(String quantityDeducted) {
        this.quantityDeducted = quantityDeducted;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVoidTransaction() {
        return voidTransaction;
    }

    public void setVoidTransaction(String voidTransaction) {
        this.voidTransaction = voidTransaction;
    }

    public double getTotalAmountChargedByRetailer() {
        return totalAmountChargedByRetailer;
    }

    public void setTotalAmountChargedByRetailer(double totalAmountChargedByRetailer) {
        this.totalAmountChargedByRetailer = totalAmountChargedByRetailer;
    }
}
