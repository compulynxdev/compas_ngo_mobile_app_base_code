package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

@Entity(nameInDb = "Transactions")
public class Transactions {

    @Id
    private Long id;


    private String voucherId;
    private String transactionType;
    private String date;
    private String uom;
    private String voucherIdNo;
    private String cardNo;
    @Unique
    private Long receiptNo;
    private String isUploaded;
    private String locationId;
    private String identityNo;
    private String deviceId;
    private String user;
    private String programId;
    private String programName;
    private String programCurrency;
    private String beneficiaryName;
    private Date topupStartDate;
    private Date topupEndDate;
    private String agentId;
    private String submit;
    private double longitude;
    private double latitude;
    private String timeStamp;
    private String totalAmountChargedByRetail;
    private String totalValueRemaining;
    private String cardSerialNumber;

    @Generated(hash = 338950345)
    public Transactions() {
    }

    @Generated(hash = 1007696001)
    public Transactions(Long id, String voucherId, String transactionType, String date, String uom,
            String voucherIdNo, String cardNo, Long receiptNo, String isUploaded, String locationId,
            String identityNo, String deviceId, String user, String programId, String programName,
            String programCurrency, String beneficiaryName, Date topupStartDate, Date topupEndDate,
            String agentId, String submit, double longitude, double latitude, String timeStamp,
            String totalAmountChargedByRetail, String totalValueRemaining, String cardSerialNumber) {
        this.id = id;
        this.voucherId = voucherId;
        this.transactionType = transactionType;
        this.date = date;
        this.uom = uom;
        this.voucherIdNo = voucherIdNo;
        this.cardNo = cardNo;
        this.receiptNo = receiptNo;
        this.isUploaded = isUploaded;
        this.locationId = locationId;
        this.identityNo = identityNo;
        this.deviceId = deviceId;
        this.user = user;
        this.programId = programId;
        this.programName = programName;
        this.programCurrency = programCurrency;
        this.beneficiaryName = beneficiaryName;
        this.topupStartDate = topupStartDate;
        this.topupEndDate = topupEndDate;
        this.agentId = agentId;
        this.submit = submit;
        this.longitude = longitude;
        this.latitude = latitude;
        this.timeStamp = timeStamp;
        this.totalAmountChargedByRetail = totalAmountChargedByRetail;
        this.totalValueRemaining = totalValueRemaining;
        this.cardSerialNumber = cardSerialNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVoucherId() {
        return this.voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getTransactionType() {
        return this.transactionType;
    }

    public String getCardSerialNumber() {
        return cardSerialNumber == null ? "" : cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDate() {
        return this.date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getUom() {
        return this.uom;
    }

    public void setUom(String uom) {
        this.uom = uom;
    }

    public String getVoucherIdNo() {
        return this.voucherIdNo;
    }

    public void setVoucherIdNo(String voucherIdNo) {
        this.voucherIdNo = voucherIdNo;
    }

    public String getCardNo() {
        return this.cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public Long getReceiptNo() {
        return this.receiptNo;
    }

    public void setReceiptNo(Long receiptNo) {
        this.receiptNo = receiptNo;
    }

    public String getIsUploaded() {
        return this.isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getLocationId() {
        return this.locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getIdentityNo() {
        return this.identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getDeviceId() {
        return this.deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUser() {
        return this.user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getProgramId() {
        return this.programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return this.programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getProgramCurrency() {
        return programCurrency;
    }

    public void setProgramCurrency(String programCurrency) {
        this.programCurrency = programCurrency;
    }

    public String getBeneficiaryName() {
        return this.beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public Date getTopupStartDate() {
        return this.topupStartDate;
    }

    public void setTopupStartDate(Date topupStartDate) {
        this.topupStartDate = topupStartDate;
    }

    public Date getTopupEndDate() {
        return this.topupEndDate;
    }

    public void setTopupEndDate(Date topupEndDate) {
        this.topupEndDate = topupEndDate;
    }

    public String getAgentId() {
        return this.agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getSubmit() {
        return this.submit;
    }

    public void setSubmit(String submit) {
        this.submit = submit;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTimeStamp() {
        return this.timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTotalAmountChargedByRetail() {
        return this.totalAmountChargedByRetail;
    }

    public void setTotalAmountChargedByRetail(String totalAmountChargedByRetail) {
        this.totalAmountChargedByRetail = totalAmountChargedByRetail;
    }

    public String getTotalValueRemaining() {
        return this.totalValueRemaining;
    }

    public void setTotalValueRemaining(String totalValueRemaining) {
        this.totalValueRemaining = totalValueRemaining;
    }
}
