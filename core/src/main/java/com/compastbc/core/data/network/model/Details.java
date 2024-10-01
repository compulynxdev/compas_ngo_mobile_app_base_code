package com.compastbc.core.data.network.model;

import com.compastbc.core.data.db.model.Programs;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Details {
    private String voucherId = "";
    private String programmeId = "";
    private String cardNumber = "";
    private String status = "";
    private double longitude;
    private double latitude;
    private String error = "";
    private String level = "";
    private Beneficiary beneficiary = null;
    private String voucherValue = "";
    private JSONObject topups = null;
    private String uid = "";
    private String downloadStatus = "";
    private String campId = "";
    private String merchantMasterId = "";
    private String cycle = "";
    private String voucherNo = "";
    // private    String benGroup="";
    private String rationNo = "";
    private String tcount = "";
    private String netSales = "";
    private String date = "";
    private JSONArray programs;
    private List<Programs> programsList = new ArrayList<>();
    private String vcount = "";
    private String vAmount = "";
    private String sales = "";
    private String index = "";
    private String locationId = "";
    private boolean salesReport = false;
    private boolean voidTransaction = false;
    private boolean activityLog = false;
    private boolean attendanceLog = false;
    private boolean mode = false;
    private boolean automated = false;
    private boolean uploadLog = false;
    private boolean carryForward = false;
    private boolean bioStatus = false;

    private String user = "";
    private String password = "";
    private String time = "";
    private String agentId = "";
    private String locationName = "";
    private String benIdLevel = "";
    private String vendorIdLevel = "";


    public String getVoucherId() {
        return voucherId;
    }

    public void setVoucherId(String voucherId) {
        this.voucherId = voucherId;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getBenIdLevel() {
        return benIdLevel;
    }

    public void setBenIdLevel(String benIdLevel) {
        this.benIdLevel = benIdLevel;
    }

    public String getVendorIdLevel() {
        return vendorIdLevel;
    }

    public void setVendorIdLevel(String vendorIdLevel) {
        this.vendorIdLevel = vendorIdLevel;
    }

    public String getStatus() {
        return status;
    }

    public boolean isBioStatus() {
        return bioStatus;
    }

    public void setBioStatus(boolean bioStatus) {
        this.bioStatus = bioStatus;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getVoucherValue() {
        return voucherValue;
    }

    public void setVoucherValue(String voucherValue) {
        this.voucherValue = voucherValue;
    }

    public JSONObject getTopups() {
        return topups;
    }

    public void setTopups(JSONObject topups) {
        this.topups = topups;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(String downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public String getCampId() {
        return campId;
    }

    public void setCampId(String campId) {
        this.campId = campId;
    }

    public String getMerchantMasterId() {
        return merchantMasterId;
    }

    public void setMerchantMasterId(String merchantMasterId) {
        this.merchantMasterId = merchantMasterId;
    }

    public String getCycle() {
        return cycle;
    }

    public void setCycle(String cycle) {
        this.cycle = cycle;
    }

    public String getVoucherNo() {
        return voucherNo;
    }

    public void setVoucherNo(String voucherNo) {
        this.voucherNo = voucherNo;
    }

    public String getRationNo() {
        return rationNo;
    }

    public void setRationNo(String rationNo) {
        this.rationNo = rationNo;
    }

    public String getTcount() {
        return tcount;
    }

    public void setTcount(String tcount) {
        this.tcount = tcount;
    }

    public String getNetSales() {
        return netSales;
    }

    public void setNetSales(String netSales) {
        this.netSales = netSales;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public JSONArray getPrograms() {
        return programs;
    }

    public void setPrograms(JSONArray programs) {
        this.programs = programs;
    }

    public List<Programs> getProgramsList() {
        return programsList;
    }

    public void setProgramsList(List<Programs> programsList) {
        this.programsList = programsList;
    }

    public String getVcount() {
        return vcount;
    }

    public void setVcount(String vcount) {
        this.vcount = vcount;
    }

    public String getvAmount() {
        return vAmount;
    }

    public void setvAmount(String vAmount) {
        this.vAmount = vAmount;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getIndex() {
        return index;
    }


    public void setIndex(String index) {
        this.index = index;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public boolean isSalesReport() {
        return salesReport;
    }

    public void setSalesReport(boolean salesReport) {
        this.salesReport = salesReport;
    }

    public boolean isVoidTransaction() {
        return voidTransaction;
    }

    public void setVoidTransaction(boolean voidTransaction) {
        this.voidTransaction = voidTransaction;
    }

    public boolean isActivityLog() {
        return activityLog;
    }

    public void setActivityLog(boolean activityLog) {
        this.activityLog = activityLog;
    }

    public boolean isAttendanceLog() {
        return attendanceLog;
    }

    public void setAttendanceLog(boolean attendanceLog) {
        this.attendanceLog = attendanceLog;
    }

    public boolean isMode() {
        return mode;
    }

    public void setMode(boolean mode) {
        this.mode = mode;
    }

    public boolean isAutomated() {
        return automated;
    }

    public void setAutomated(boolean automated) {
        this.automated = automated;
    }

    public boolean isUploadLog() {
        return uploadLog;
    }

    public void setUploadLog(boolean uploadLog) {
        this.uploadLog = uploadLog;
    }

    public boolean isCarryForward() {
        return carryForward;
    }

    public void setCarryForward(boolean carryForward) {
        this.carryForward = carryForward;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
