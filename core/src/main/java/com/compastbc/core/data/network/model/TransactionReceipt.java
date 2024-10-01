package com.compastbc.core.data.network.model;

import com.compastbc.core.data.db.model.PurchasedProducts;

import java.util.List;

public class TransactionReceipt {


    private String ration;
    private String programCurrency;
    private String receiptNo;
    private String openingBal;
    private String txnValue;
    private String currentBalance;
    private String cardSerialNumber;
    private List<PurchasedProducts> productsList;

    public String getRation() {
        return ration == null ? "" : ration;
    }

    public void setRation(String ration) {
        this.ration = ration;
    }

    public String getProgramCurrency() {
        return programCurrency == null ? "" : programCurrency;
    }

    public String getCardSerialNumber() {
        return cardSerialNumber == null ? "" : cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    public void setProgramCurrency(String programCurrency) {
        this.programCurrency = programCurrency;
    }

    public String getReceiptNo() {
        return receiptNo == null ? "" : receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public String getOpeningBal() {
        return openingBal;
    }

    public void setOpeningBal(String openingBal) {
        this.openingBal = openingBal;
    }

    public String getTxnValue() {
        return txnValue;
    }

    public void setTxnValue(String txnValue) {
        this.txnValue = txnValue;
    }

    public String getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(String currentBalance) {
        this.currentBalance = currentBalance;
    }

    public List<PurchasedProducts> getProductsList() {
        return productsList;
    }

    public void setProductsList(List<PurchasedProducts> productsList) {
        this.productsList = productsList;
    }
}
