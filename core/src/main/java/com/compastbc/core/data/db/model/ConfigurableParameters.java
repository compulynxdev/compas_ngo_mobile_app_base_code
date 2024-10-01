package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "ConfigurableParameters")
public class ConfigurableParameters {
    private boolean voidTransaction;
    private boolean salesReport;
    private boolean activityLog;
    private boolean attendanceLog;
    private boolean carryForward;
    private boolean biometric;
    private boolean online;
    private boolean automated;
    private int minimumFinger;
    private int matchingPercentage;
    private boolean fingerPrintActive;
    private boolean irisActive;
    private boolean faceActive;
    private String idType;
    private int idLength;

    @Generated(hash = 1087483806)
    public ConfigurableParameters() {
    }


    @Generated(hash = 980759433)
    public ConfigurableParameters(boolean voidTransaction, boolean salesReport,
            boolean activityLog, boolean attendanceLog, boolean carryForward,
            boolean biometric, boolean online, boolean automated, int minimumFinger,
            int matchingPercentage, boolean fingerPrintActive, boolean irisActive,
            boolean faceActive, String idType, int idLength) {
        this.voidTransaction = voidTransaction;
        this.salesReport = salesReport;
        this.activityLog = activityLog;
        this.attendanceLog = attendanceLog;
        this.carryForward = carryForward;
        this.biometric = biometric;
        this.online = online;
        this.automated = automated;
        this.minimumFinger = minimumFinger;
        this.matchingPercentage = matchingPercentage;
        this.fingerPrintActive = fingerPrintActive;
        this.irisActive = irisActive;
        this.faceActive = faceActive;
        this.idType = idType;
        this.idLength = idLength;
    }


    public int getIdLength() {
        return idLength;
    }

    public void setIdLength(int idLength) {
        this.idLength = idLength;
    }

    public boolean isVoidTransaction() {
        return voidTransaction;
    }

    public boolean isSalesReport() {
        return salesReport;
    }

    public boolean isActivityLog() {
        return activityLog;
    }

    public boolean isAttendanceLog() {
        return attendanceLog;
    }

    public boolean isCarryForward() {
        return carryForward;
    }

    public boolean isBiometric() {
        return biometric;
    }

    public boolean isOnline() {
        return online;
    }

    public boolean isAutomated() {
        return automated;
    }

    public int getMinimumFinger() {
        return minimumFinger;
    }

    public void setMinimumFinger(int minimumFinger) {
        this.minimumFinger = minimumFinger;
    }

    public int getMatchingPercentage() {
        return matchingPercentage;
    }

    public void setMatchingPercentage(int matchingPercentage) {
        this.matchingPercentage = matchingPercentage;
    }

    public boolean isFingerPrintActive() {
        return fingerPrintActive;
    }

    public boolean isIrisActive() {
        return irisActive;
    }

    public boolean isFaceActive() {
        return faceActive;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public boolean getVoidTransaction() {
        return this.voidTransaction;
    }

    public void setVoidTransaction(boolean voidTransaction) {
        this.voidTransaction = voidTransaction;
    }

    public boolean getSalesReport() {
        return this.salesReport;
    }

    public void setSalesReport(boolean salesReport) {
        this.salesReport = salesReport;
    }

    public boolean getActivityLog() {
        return this.activityLog;
    }

    public void setActivityLog(boolean activityLog) {
        this.activityLog = activityLog;
    }

    public boolean getAttendanceLog() {
        return this.attendanceLog;
    }

    public void setAttendanceLog(boolean attendanceLog) {
        this.attendanceLog = attendanceLog;
    }

    public boolean getCarryForward() {
        return this.carryForward;
    }

    public void setCarryForward(boolean carryForward) {
        this.carryForward = carryForward;
    }

    public boolean getBiometric() {
        return this.biometric;
    }

    public void setBiometric(boolean biometric) {
        this.biometric = biometric;
    }

    public boolean getOnline() {
        return this.online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean getAutomated() {
        return this.automated;
    }

    public void setAutomated(boolean automated) {
        this.automated = automated;
    }

    public boolean getFingerPrintActive() {
        return this.fingerPrintActive;
    }

    public void setFingerPrintActive(boolean fingerPrintActive) {
        this.fingerPrintActive = fingerPrintActive;
    }

    public boolean getIrisActive() {
        return this.irisActive;
    }

    public void setIrisActive(boolean irisActive) {
        this.irisActive = irisActive;
    }

    public boolean getFaceActive() {
        return this.faceActive;
    }

    public void setFaceActive(boolean faceActive) {
        this.faceActive = faceActive;
    }

}
