package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

/**
 * Created by Hemant Sharma on 25-01-21.
 * Divergent software labs pvt. ltd
 */
@Entity(nameInDb = "NFCCardData")
public class NFCCardData {
    @Id
    private Long id;

    @Unique
    private String cardID;
    private String cardNumber;
    private String beneficiaryName;
    private String cardJsonObjData;
    private Date createdDate;


    @Generated(hash = 1872662261)
    public NFCCardData(Long id, String cardID, String cardNumber,
            String beneficiaryName, String cardJsonObjData, Date createdDate) {
        this.id = id;
        this.cardID = cardID;
        this.cardNumber = cardNumber;
        this.beneficiaryName = beneficiaryName;
        this.cardJsonObjData = cardJsonObjData;
        this.createdDate = createdDate;
    }

    @Generated(hash = 1154693055)
    public NFCCardData() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCardID() {
        return cardID;
    }

    public void setCardID(String cardID) {
        this.cardID = cardID;
    }

    public String getBeneficiaryName() {
        return beneficiaryName == null ? "" : beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getCardJsonObjData() {
        return cardJsonObjData;
    }

    public void setCardJsonObjData(String cardJsonObjData) {
        this.cardJsonObjData = cardJsonObjData;
    }

    public Date getCreatedDate() {
        return createdDate == null ? new Date() : createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }
}
