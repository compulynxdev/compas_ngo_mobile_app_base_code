package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "TransactionListProducts")
public class TransactionListProducts {
    public String productId;
    public String productName;
    public String quantity;
    public String val;
    public String programId;
    public String unitOfMeasure;
    public String transactionDate;
    public String deviceId;
    public String transactionNo;
    public String beneficiaryName;
    public String voidTransaction;
    @Id
    private Long id;
    @Unique
    private String uniqueid;
    @Transient
    private String identificationNum;

    @Generated(hash = 1020138440)
    public TransactionListProducts(String productId, String productName, String quantity, String val, String programId,
                                   String unitOfMeasure, String transactionDate, String deviceId, String transactionNo, String beneficiaryName,
                                   String voidTransaction, Long id, String uniqueid) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.val = val;
        this.programId = programId;
        this.unitOfMeasure = unitOfMeasure;
        this.transactionDate = transactionDate;
        this.deviceId = deviceId;
        this.transactionNo = transactionNo;
        this.beneficiaryName = beneficiaryName;
        this.voidTransaction = voidTransaction;
        this.id = id;
        this.uniqueid = uniqueid;
    }

    @Generated(hash = 162858476)
    public TransactionListProducts() {
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTransactionNo() {
        return transactionNo;
    }

    public void setTransactionNo(String transactionNo) {
        this.transactionNo = transactionNo;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getVoidTransaction() {
        return voidTransaction;
    }

    public void setVoidTransaction(String voidTransaction) {
        this.voidTransaction = voidTransaction;
    }

    public String getUniqueid() {
        return uniqueid;
    }

    public void setUniqueid(String uniqueid) {
        this.uniqueid = uniqueid;
    }

    public String getIdentificationNum() {
        return identificationNum;
    }

    public void setIdentificationNum(String identificationNum) {
        this.identificationNum = identificationNum;
    }
}
