package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "Users")
public class Users {
    @Id
    private Long id;
    private String username;
    private String password;
    @Unique
    private String usersId;
    private String level;
    private String locationid;
    private String isloggedin;
    private String isuploaded = "1";
    //private String merchantMaster;
    private Boolean bio = false;
    private String fpli;
    private String fplt;
    private String fplf;
    private String f1;
    private String f2;
    private String f3;
    private String datetime;
    private String image;
    private String f4;
    private String fpri;
    private String fprt;
    private String fprf;
    private String locationName;
    private String agentId;

    @Generated(hash = 1042591108)
    public Users(Long id, String username, String password, String usersId, String level,
                 String locationid, String isloggedin, String isuploaded, Boolean bio, String fpli,
                 String fplt, String fplf, String f1, String f2, String f3, String datetime,
                 String image, String f4, String fpri, String fprt, String fprf,
                 String locationName, String agentId) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.usersId = usersId;
        this.level = level;
        this.locationid = locationid;
        this.isloggedin = isloggedin;
        this.isuploaded = isuploaded;
        this.bio = bio;
        this.fpli = fpli;
        this.fplt = fplt;
        this.fplf = fplf;
        this.f1 = f1;
        this.f2 = f2;
        this.f3 = f3;
        this.datetime = datetime;
        this.image = image;
        this.f4 = f4;
        this.fpri = fpri;
        this.fprt = fprt;
        this.fprf = fprf;
        this.locationName = locationName;
        this.agentId = agentId;
    }

    @Generated(hash = 2146996206)
    public Users() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsersId() {
        return usersId;
    }

    public void setUsersId(String usersId) {
        this.usersId = usersId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLocationid() {
        return locationid;
    }

    public void setLocationid(String locationid) {
        this.locationid = locationid;
    }

    public String getIsloggedin() {
        return isloggedin;
    }

    public void setIsloggedin(String isloggedin) {
        this.isloggedin = isloggedin;
    }

    public String getIsuploaded() {
        return isuploaded;
    }

    public void setIsuploaded(String isuploaded) {
        this.isuploaded = isuploaded;
    }

    public Boolean getBio() {
        return bio;
    }

    public void setBio(Boolean bio) {
        this.bio = bio;
    }

    public String getFpli() {
        return fpli;
    }

    public void setFpli(String fpli) {
        this.fpli = fpli;
    }

    public String getFplt() {
        return fplt;
    }

    public void setFplt(String fplt) {
        this.fplt = fplt;
    }

    public String getFplf() {
        return fplf;
    }

    public void setFplf(String fplf) {
        this.fplf = fplf;
    }

    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }

    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }

    public String getF3() {
        return f3;
    }

    public void setF3(String f3) {
        this.f3 = f3;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getF4() {
        return f4;
    }

    public void setF4(String f4) {
        this.f4 = f4;
    }

    public String getFpri() {
        return fpri;
    }

    public void setFpri(String fpri) {
        this.fpri = fpri;
    }

    public String getFprt() {
        return fprt;
    }

    public void setFprt(String fprt) {
        this.fprt = fprt;
    }

    public String getFprf() {
        return fprf;
    }

    public void setFprf(String fprf) {
        this.fprf = fprf;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }
}
