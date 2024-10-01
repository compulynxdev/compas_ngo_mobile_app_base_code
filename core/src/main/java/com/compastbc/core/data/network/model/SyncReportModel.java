package com.compastbc.core.data.network.model;

import java.util.List;

public class SyncReportModel {
    private String deviceId;
    private String syncDate;
    private String totalTxns;
    private String totalAmount;
    private List<String> currencyAmounts;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public List<String> getCurrencyAmounts() {
        return currencyAmounts;
    }

    public void setCurrencyAmounts(List<String> currencyAmounts) {
        this.currencyAmounts = currencyAmounts;
    }

    public String getSyncDate() {
        return syncDate;
    }

    public void setSyncDate(String syncDate) {
        this.syncDate = syncDate;
    }

    public String getTotalTxns() {
        return totalTxns;
    }

    public void setTotalTxns(String totalTxns) {
        this.totalTxns = totalTxns;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }
}
