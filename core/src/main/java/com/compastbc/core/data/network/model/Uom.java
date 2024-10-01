package com.compastbc.core.data.network.model;

public class Uom {

    private String uom;
    private String maxPrice;
    private String count;
    private String currency;

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getCount() {
        return count == null ? "0" : count;
    }

    public void setCount(String count) {
        this.count = count;
    }
}
