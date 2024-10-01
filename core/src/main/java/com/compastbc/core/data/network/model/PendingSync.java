package com.compastbc.core.data.network.model;

import java.util.Date;

public class PendingSync {
    private Date startDate;
    private Date endDate;
    private String programmeId;
    private String ttxns;
    private String tamt;
    private String programCurrency;

    public String getProgramCurrency() {
        return programCurrency;
    }

    public void setProgramCurrency(String programCurrency) {
        this.programCurrency = programCurrency;
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

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getTtxns() {
        return ttxns;
    }

    public void setTtxns(String ttxns) {
        this.ttxns = ttxns;
    }

    public String getTamt() {
        return tamt;
    }

    public void setTamt(String tamt) {
        this.tamt = tamt;
    }
}
