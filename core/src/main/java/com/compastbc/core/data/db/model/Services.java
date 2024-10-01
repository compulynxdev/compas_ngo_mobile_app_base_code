package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "Services")
public class Services {

    @Id
    private Long id;
    private String serviceId;
    private String serviceCode;
    private String serviceName;
    private String serviceImage;
    private String categoryId;
    private String serviceType;
    private String locationId;
    private double maxQuantity = 0.0;
    private double maxQuantityBenf = 0.0;


    @Generated(hash = 756850816)
    public Services(Long id, String serviceId, String serviceCode,
            String serviceName, String serviceImage, String categoryId,
            String serviceType, String locationId, double maxQuantity,
            double maxQuantityBenf) {
        this.id = id;
        this.serviceId = serviceId;
        this.serviceCode = serviceCode;
        this.serviceName = serviceName;
        this.serviceImage = serviceImage;
        this.categoryId = categoryId;
        this.serviceType = serviceType;
        this.locationId = locationId;
        this.maxQuantity = maxQuantity;
        this.maxQuantityBenf = maxQuantityBenf;
    }

    @Generated(hash = 2131255380)
    public Services() {
    }

    
    public double getMaxQuantityBenf() {
        return maxQuantityBenf;
    }

    public void setMaxQuantityBenf(double maxQuantityBenf) {
        this.maxQuantityBenf = maxQuantityBenf;
    }

    


    public double getMaxQuantity() {
        return maxQuantity;
    }

    public void setMaxQuantity(double maxQuantity) {
        this.maxQuantity = maxQuantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getServiceId() {
        return serviceId == null ? "" : serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceImage() {
        return serviceImage;
    }

    public void setServiceImage(String serviceImage) {
        this.serviceImage = serviceImage;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }
}
