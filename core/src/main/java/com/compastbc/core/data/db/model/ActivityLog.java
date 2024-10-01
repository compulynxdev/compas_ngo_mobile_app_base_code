package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

/**
 * Created by Hemant Sharma on 10-02-20.
 * Divergent software labs pvt. ltd
 */
@Entity(nameInDb = "ActivityLog")
public class ActivityLog {
    @Id
    private Long id;
    private String deviceId;
    private String userName;
    private String locationId;
    private String activity;
    private String action;
    private String date;
    private double latitude;
    private double longitude;
    @Unique
    private String uniqueId;

    @Generated(hash = 2114612826)
    public ActivityLog(Long id, String deviceId, String userName, String locationId,
                       String activity, String action, String date, double latitude,
                       double longitude, String uniqueId) {
        this.id = id;
        this.deviceId = deviceId;
        this.userName = userName;
        this.locationId = locationId;
        this.activity = activity;
        this.action = action;
        this.date = date;
        this.latitude = latitude;
        this.longitude = longitude;
        this.uniqueId = uniqueId;
    }

    @Generated(hash = 1382907823)
    public ActivityLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }
}
