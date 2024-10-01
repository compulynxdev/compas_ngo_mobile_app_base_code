package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "ServicePrices")
public class ServicePrices {

    private String serviceId;
    private String uom;
    private Double maxPrice;
    private String currency = "";

    @Generated(hash = 1477973094)
    public ServicePrices(String serviceId, String uom, Double maxPrice,
                         String currency) {
        this.serviceId = serviceId;
        this.uom = uom;
        this.maxPrice = maxPrice;
        this.currency = currency;
    }

    @Generated(hash = 105923477)
    public ServicePrices() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public Double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }
}
