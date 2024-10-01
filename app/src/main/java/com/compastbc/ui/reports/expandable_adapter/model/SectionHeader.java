package com.compastbc.ui.reports.expandable_adapter.model;

/**
 * Created by chris on 2017-06-13.
 */

public class SectionHeader {

    private String title;
    private String amount;
    private String currency = "";
    private String date;

    public SectionHeader() {
    }

    public SectionHeader(String title) {
        this.title = title;
    }

    public SectionHeader(String title, String amount, String currency) {
        this.title = title;
        this.amount = amount;
        this.currency = currency;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
