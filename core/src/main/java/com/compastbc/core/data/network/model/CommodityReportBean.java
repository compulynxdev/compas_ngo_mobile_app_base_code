package com.compastbc.core.data.network.model;

import java.util.List;

public class CommodityReportBean {
    private String title;

    private String ttlAmt;
    private String currency = "";

    private List<ReportModel> modelList;

    public String getTtlAmt() {
        return ttlAmt;
    }

    public void setTtlAmt(String ttlAmt) {
        this.ttlAmt = ttlAmt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<ReportModel> getModelList() {
        return modelList;
    }

    public void setModelList(List<ReportModel> modelList) {
        this.modelList = modelList;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
