package com.compastbc.core.data.network.model;

/**
 * Created by Hemant Sharma on 14-10-19.
 * Divergent software labs pvt. ltd
 */
public class BeneficiaryFilterBean {
    private String tmpId = "", tmpName = "", tmpDob = "", tmpGender = "", tmpBioStatus = "";

    public String getTmpId() {
        return tmpId;
    }

    public void setTmpId(String tmpId) {
        this.tmpId = tmpId;
    }

    public String getTmpName() {
        return tmpName;
    }

    public void setTmpName(String tmpName) {
        this.tmpName = tmpName;
    }

    public String getTmpDob() {
        return tmpDob;
    }

    public void setTmpDob(String tmpDob) {
        this.tmpDob = tmpDob;
    }

    public String getTmpGender() {
        return tmpGender;
    }

    public void setTmpGender(String tmpGender) {
        this.tmpGender = tmpGender;
    }

    public String getTmpBioStatus() {
        return tmpBioStatus;
    }

    public void setTmpBioStatus(String tmpBioStatus) {
        this.tmpBioStatus = tmpBioStatus;
    }
}
