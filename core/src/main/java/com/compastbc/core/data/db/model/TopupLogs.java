package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "TopupsLogs")
public class TopupLogs {
    private Long id;
    private String ntopupValue;
    @Unique
    private String nvoucherIdNo;
    private String oCardBal;
    private String nCardBal;
    private String ovoucherIdNo;
    private String topupTime;
    private String cardNo;
    private String isUploaded;
    private String deviceIdNo;
    private String programmeId;
    private String userName;
    private String refNo;

    @Generated(hash = 1863681825)
    public TopupLogs(Long id, String ntopupValue, String nvoucherIdNo,
                     String oCardBal, String nCardBal, String ovoucherIdNo, String topupTime,
                     String cardNo, String isUploaded, String deviceIdNo, String programmeId,
                     String userName, String refNo) {
        this.id = id;
        this.ntopupValue = ntopupValue;
        this.nvoucherIdNo = nvoucherIdNo;
        this.oCardBal = oCardBal;
        this.nCardBal = nCardBal;
        this.ovoucherIdNo = ovoucherIdNo;
        this.topupTime = topupTime;
        this.cardNo = cardNo;
        this.isUploaded = isUploaded;
        this.deviceIdNo = deviceIdNo;
        this.programmeId = programmeId;
        this.userName = userName;
        this.refNo = refNo;
    }

    @Generated(hash = 2129352202)
    public TopupLogs() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNtopupValue() {
        return ntopupValue;
    }

    public void setNtopupValue(String ntopupValue) {
        this.ntopupValue = ntopupValue;
    }

    public String getNvoucherIdNo() {
        return nvoucherIdNo;
    }

    public void setNvoucherIdNo(String nvoucherIdNo) {
        this.nvoucherIdNo = nvoucherIdNo;
    }

    public String getOvoucherIdNo() {
        return ovoucherIdNo;
    }

    public void setOvoucherIdNo(String ovoucherIdNo) {
        this.ovoucherIdNo = ovoucherIdNo;
    }

    public String getTopupTime() {
        return topupTime;
    }

    public void setTopupTime(String topupTime) {
        this.topupTime = topupTime;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getDeviceIdNo() {
        return deviceIdNo;
    }

    public void setDeviceIdNo(String deviceIdNo) {
        this.deviceIdNo = deviceIdNo;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getOCardBal() {
        return this.oCardBal;
    }

    public void setOCardBal(String oCardBal) {
        this.oCardBal = oCardBal;
    }

    public String getNCardBal() {
        return this.nCardBal;
    }

    public void setNCardBal(String nCardBal) {
        this.nCardBal = nCardBal;
    }
}
