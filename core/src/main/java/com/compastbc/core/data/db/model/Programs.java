package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Transient;
import org.greenrobot.greendao.annotation.Unique;

import java.util.ArrayList;
import java.util.List;

@Entity(nameInDb = "Programs")
public class Programs {

    @Unique
    private String programId;
    private String programName;
    private String voucherId;
    private String productId;
    private String programCurrency;
    @Transient
    private List<Integer> puchasedItemIds = new ArrayList<>() ;


    @Generated(hash = 1268597385)
    public Programs(String programId, String programName, String voucherId,
            String productId, String programCurrency) {
        this.programId = programId;
        this.programName = programName;
        this.voucherId = voucherId;
        this.productId = productId;
        this.programCurrency = programCurrency;
    }

    @Generated(hash = 9361640)
    public Programs() {
    }


    public List<Integer> getPuchasedItemIds() {
        return puchasedItemIds;
    }

    public void setPuchasedItemIds(List<Integer> puchasedItemIds) {
        this.puchasedItemIds = puchasedItemIds;
    }

    public String getProgramCurrency() {
        return programCurrency == null ? "$" : programCurrency;
    }

    public void setProgramCurrency(String programCurrency) {
        this.programCurrency = programCurrency;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

}
