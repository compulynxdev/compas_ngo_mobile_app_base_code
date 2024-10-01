package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "Vouchers")
public class Vouchers {

    private String voucherId;
    private String voucherName;
    private String programId;

    @Generated(hash = 146113487)
    public Vouchers(String voucherId, String voucherName, String programId) {
        this.voucherId = voucherId;
        this.voucherName = voucherName;
        this.programId = programId;
    }

    @Generated(hash = 1353651429)
    public Vouchers() {
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

    public String getVoucherName() {
        return voucherName;
    }

    public void setVoucherName(String voucherName) {
        this.voucherName = voucherName;
    }
}
