package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "BeneficiaryBio")
public class BeneficiaryBio {

    @Id
    private Long id;
    @Unique
    private String beneficiaryId;
    private String fpli;
    private String fplt;
    private String fplf;
    private String f1;
    private String f2;
    private String f3;
    private String f4;
    private String fpri;
    private String fprt;
    private String fprf;
    private String datetime;

    @Generated(hash = 1471564197)
    public BeneficiaryBio(Long id, String beneficiaryId, String fpli, String fplt,
                          String fplf, String f1, String f2, String f3, String f4, String fpri,
                          String fprt, String fprf, String datetime) {
        this.id = id;
        this.beneficiaryId = beneficiaryId;
        this.fpli = fpli;
        this.fplt = fplt;
        this.fplf = fplf;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.f4 = f4;
        this.fpri = fpri;
        this.fprt = fprt;
        this.fprf = fprf;
        this.datetime = datetime;
    }

    @Generated(hash = 1906802062)
    public BeneficiaryBio() {
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

    public String getFpli() {
        return (fpli == null || fpli.equals("null")) ? null : fpli;
    }

    public void setFpli(String fpli) {
        this.fpli = fpli;
    }

    public String getFplt() {
        return (fplt == null || fplt.equals("null")) ? null : fplt;
    }

    public void setFplt(String fplt) {
        this.fplt = fplt;
    }

    public String getFplf() {
        return (fplf == null || fplf.equals("null")) ? null : fplf;
    }

    public void setFplf(String fplf) {
        this.fplf = fplf;
    }

    public String getF1() {
        return (f1 == null || f1.equals("null")) ? null : f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return (f2 == null || f2.equals("null")) ? null : f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public String getF3() {
        return (f3 == null || f3.equals("null")) ? null : f3;
    }

    public void setF3(String f3) {
        this.f3 = f3;
    }

    public String getF4() {
        return (f4 == null || f4.equals("null")) ? null : f4;
    }

    public void setF4(String f4) {
        this.f4 = f4;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getFpri() {
        return (fpri == null || fpri.equals("null")) ? null : fpri;
    }

    public void setFpri(String fpri) {
        this.fpri = fpri;
    }

    public String getFprt() {
        return (fprt == null || fprt.equals("null")) ? null : fprt;
    }

    public void setFprt(String fprt) {
        this.fprt = fprt;
    }

    public String getFprf() {
        return (fprf == null || fprf.equals("null")) ? null : fprf;
    }

    public void setFprf(String fprf) {
        this.fprf = fprf;
    }
}
