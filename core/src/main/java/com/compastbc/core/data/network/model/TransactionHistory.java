package com.compastbc.core.data.network.model;

public class TransactionHistory {

    private String receiptNo;
    private String amount;
    private String uom;
    private String benfName;
    private String commodityName;
    private String quantity;
    private String identityNo;
    private String transactionType;
    private String date;
    private String currency = "";
    private String cardSerialNumber;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReceiptNo() {
        return receiptNo;
    }

    public void setReceiptNo(String receiptNo) {
        this.receiptNo = receiptNo;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getBenfName() {
        return benfName;
    }

    public void setBenfName(String benfName) {
        this.benfName = benfName;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public String getQuantity() {
        return quantity;
    }

    public String getCardSerialNumber() {
        return cardSerialNumber == null ? "" : cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
