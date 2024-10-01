package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "PurchaseProducts")
public class PurchasedProducts {

    @Id
    private Long id;
    private String serviceId;
    private String quantity;
    private String totalPrice;
    private String programmeId;
    private String quantityDeducted;
    private String uom;
    private String cardNumber;
    private String voucherId;
    private String serviceImage;
    private String serviceName;
    private String maxPrice;

    @Generated(hash = 1613530872)
    public PurchasedProducts(Long id, String serviceId, String quantity,
            String totalPrice, String programmeId, String quantityDeducted,
            String uom, String cardNumber, String voucherId, String serviceImage,
            String serviceName, String maxPrice) {
        this.id = id;
        this.serviceId = serviceId;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.programmeId = programmeId;
        this.quantityDeducted = quantityDeducted;
        this.uom = uom;
        this.cardNumber = cardNumber;
        this.voucherId = voucherId;
        this.serviceImage = serviceImage;
        this.serviceName = serviceName;
        this.maxPrice = maxPrice;
    }

    @Generated(hash = 1330848142)
    public PurchasedProducts() {
    }

    public String getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(String maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(String totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getQuantityDeducted() {
        return quantityDeducted;
    }

    public void setQuantityDeducted(String quantityDeducted) {
        this.quantityDeducted = quantityDeducted;
    }

    public String getUom() {
        return uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }
}
