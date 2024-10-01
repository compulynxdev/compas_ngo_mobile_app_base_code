package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;

@Entity(nameInDb = "ExceptionLog")
public class ExceptionLog {

    private String deviceId;
    private String agentId;
    private String deviceName;
    private String dataObject;
    private String createdDate;
    private String screenName;


    @Generated(hash = 86322708)
    public ExceptionLog(String deviceId, String agentId, String deviceName,
            String dataObject, String createdDate, String screenName) {
        this.deviceId = deviceId;
        this.agentId = agentId;
        this.deviceName = deviceName;
        this.dataObject = dataObject;
        this.createdDate = createdDate;
        this.screenName = screenName;
    }

    @Generated(hash = 1357110165)
    public ExceptionLog() {
    }


    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDataObject() {
        return dataObject;
    }

    public void setDataObject(String dataObject) {
        this.dataObject = dataObject;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
