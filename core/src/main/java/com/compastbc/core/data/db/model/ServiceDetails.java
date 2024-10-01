package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "ServiceDetails")
public class ServiceDetails {

    private String serviceId;
    private String programId;
    private String voucherId;

    @Generated(hash = 2041866974)
    public ServiceDetails(String serviceId, String programId, String voucherId) {
        this.serviceId = serviceId;
        this.programId = programId;
        this.voucherId = voucherId;
    }

    @Generated(hash = 571598869)
    public ServiceDetails() {
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }
}
