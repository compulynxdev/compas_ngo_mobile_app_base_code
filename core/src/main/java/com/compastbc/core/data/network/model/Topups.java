package com.compastbc.core.data.network.model;

import java.util.ArrayList;
import java.util.List;

public class Topups {

    private String cardnumber;
    private String vouchervalue;
    private String programmeid;
    private String vocheridno;
    private String voucherid;
    private String startDate;
    private String endDate;
    private String beneficiaryName;
    private String identificationNumber;
    private int index;
    private String programCurrency;
    private List<Integer> purchasedIds;

    public List<Integer> getPurchasedIds() {
        return purchasedIds == null ? new ArrayList<>() : purchasedIds;
    }

    public void setPurchasedIds(List<Integer> purchasedIds) {
        this.purchasedIds = purchasedIds;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getProgramCurrency() {
        return programCurrency;
    }

    public void setProgramCurrency(String programCurrency) {
        this.programCurrency = programCurrency;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getVouchervalue() {
        return vouchervalue;
    }

    public void setVouchervalue(String vouchervalue) {
        this.vouchervalue = vouchervalue;
    }

    public String getProgrammeid() {
        return programmeid;
    }

    public void setProgrammeid(String programmeid) {
        this.programmeid = programmeid;
    }

    public String getVocheridno() {
        return vocheridno;
    }

    public void setVocheridno(String vocheridno) {
        this.vocheridno = vocheridno;
    }

    public String getVoucherid() {
        return voucherid;
    }

    public void setVoucherid(String voucherid) {
        this.voucherid = voucherid;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getBeneficiaryName() {
        return beneficiaryName == null ? "" : beneficiaryName;
    }

    public void setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
}
