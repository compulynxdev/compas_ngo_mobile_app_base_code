package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

@Entity(nameInDb = "Topups")
public class Topups {
    @Id
    private Long id;
    private String beneficiaryId;
    private String cardNumber;
    private String voucherValue;
    private String programmeId;
    @Unique
    private String vocherIdNo;
    private String voucherId;
    private Date startDate;
    private Date endDate;

    @Generated(hash = 2020804906)
    public Topups(Long id, String beneficiaryId, String cardNumber,
                  String voucherValue, String programmeId, String vocherIdNo,
                  String voucherId, Date startDate, Date endDate) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.cardNumber = cardNumber;
        this.voucherValue = voucherValue;
        this.programmeId = programmeId;
        this.vocherIdNo = vocherIdNo;
        this.voucherId = voucherId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Generated(hash = 1973278366)
    public Topups() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getVoucherValue() {
        return voucherValue;
    }

    public void setVoucherValue(String voucherValue) {
        this.voucherValue = voucherValue;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getVocherIdNo() {
        return vocherIdNo;
    }

    public void setVocherIdNo(String vocherIdNo) {
        this.vocherIdNo = vocherIdNo;
    }

    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
