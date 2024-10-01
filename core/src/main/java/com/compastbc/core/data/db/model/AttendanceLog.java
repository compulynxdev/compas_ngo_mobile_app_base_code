package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Hemant Sharma on 12-02-20.
 * Divergent software labs pvt. ltd
 */
@Entity(nameInDb = "AttendanceLog")
public class AttendanceLog {

    @Id
    private Long id;

    private String username;
    private String deviceId;
    private double longitude;
    private double latitude;
    private String loginDate;
    private boolean loginSuccess;
    private String locationId;
    @Unique
    private String uniqueId;

    public AttendanceLog() {
    }

    @Generated(hash = 1570092457)
    public AttendanceLog(Long id, String username, String deviceId,
                         double longitude, double latitude, String loginDate,
                         boolean loginSuccess, String locationId, String uniqueId) {
        this.id = id;
        this.username = username;
        this.deviceId = deviceId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.loginDate = loginDate;
        this.loginSuccess = loginSuccess;
        this.locationId = locationId;
        this.uniqueId = uniqueId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public String getLoginDate() {
        return loginDate;
    }

    public void setLoginDate(String loginDate) {
        this.loginDate = loginDate;
    }

    public boolean isLoginSuccess() {
        return loginSuccess;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public boolean getLoginSuccess() {
        return this.loginSuccess;
    }

    public void setLoginSuccess(boolean loginSuccess) {
        this.loginSuccess = loginSuccess;
    }
}
