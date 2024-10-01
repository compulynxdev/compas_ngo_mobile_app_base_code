package com.compastbc.core.data.network.model;

import java.util.List;

public class SubmitTransactionBean {
    private String title;
    private String ttlAmt;
    private String currency = "";
    private List<TransactionHistory> list;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTtlAmt() {
        return ttlAmt;
    }

    public void setTtlAmt(String ttlAmt) {
        this.ttlAmt = ttlAmt;
    }

    public List<TransactionHistory> getList() {
        return list;
    }

    public void setList(List<TransactionHistory> list) {
        this.list = list;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
