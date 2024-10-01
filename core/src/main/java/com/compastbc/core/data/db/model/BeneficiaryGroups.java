package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;

@Entity(nameInDb = "BeneficiaryGroups")
public class BeneficiaryGroups {

    @Id
    private Long id;
    private String bnfGrpName;
    private String bnfGrpId;

    @Generated(hash = 547166476)
    public BeneficiaryGroups(Long id, String bnfGrpName, String bnfGrpId) {
        this.id = id;
        this.bnfGrpName = bnfGrpName;
        this.bnfGrpId = bnfGrpId;
    }

    @Generated(hash = 417128105)
    public BeneficiaryGroups() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBnfGrpName() {
        return bnfGrpName;
    }

    public void setBnfGrpName(String bnfGrpName) {
        this.bnfGrpName = bnfGrpName;
    }

    public String getBnfGrpId() {
        return bnfGrpId;
    }

    public void setBnfGrpId(String bnfGrpId) {
        this.bnfGrpId = bnfGrpId;
    }
}
