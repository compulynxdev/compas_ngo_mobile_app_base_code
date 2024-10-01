package com.compastbc.core.data.network.model;

class Beneficiary {
    private Long id;
    private String firstname;
    private String beneficiaryId;
    private String middlename = "";
    private String lastname;
    private String user_image;
    private String signature;
    private String national_id;
    private String front_national_id;
    private String back_national_id;

    private String right_thumb;
    private String right_index;
    private String left_thumb;
    private String left_index;

    private String title;
    private String gender;
    private String date_of_birth;
    private String family_size;
    private String card_number;
    private String card_serial_number;
    private String card_pin;
    private String programmeid;
    //private String beneficiary_group_id;
    private String nationality;

    private String is_uploaded;
    private String member_number;
    private String mobile;
    private String housenumber;
    private String sectionName;

    private String agentId;
    private String deviceId;
    private boolean bio = false;
    private String loggedin;
    private String activation;
    private String bioVerifyStatus = "PENDING";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getUser_image() {
        return user_image;
    }

    public void setUser_image(String user_image) {
        this.user_image = user_image;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getNational_id() {
        return national_id;
    }

    public void setNational_id(String national_id) {
        this.national_id = national_id;
    }

    public String getFront_national_id() {
        return front_national_id;
    }

    public void setFront_national_id(String front_national_id) {
        this.front_national_id = front_national_id;
    }

    public String getBack_national_id() {
        return back_national_id;
    }

    public void setBack_national_id(String back_national_id) {
        this.back_national_id = back_national_id;
    }

    public String getRight_thumb() {
        return right_thumb;
    }

    public void setRight_thumb(String right_thumb) {
        this.right_thumb = right_thumb;
    }

    public String getRight_index() {
        return right_index;
    }

    public void setRight_index(String right_index) {
        this.right_index = right_index;
    }

    public String getLeft_thumb() {
        return left_thumb;
    }

    public void setLeft_thumb(String left_thumb) {
        this.left_thumb = left_thumb;
    }

    public String getLeft_index() {
        return left_index;
    }

    public void setLeft_index(String left_index) {
        this.left_index = left_index;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDate_of_birth() {
        return date_of_birth;
    }

    public void setDate_of_birth(String date_of_birth) {
        this.date_of_birth = date_of_birth;
    }

    public String getFamily_size() {
        return family_size;
    }

    public void setFamily_size(String family_size) {
        this.family_size = family_size;
    }

    public String getCard_number() {
        return card_number;
    }

    public void setCard_number(String card_number) {
        this.card_number = card_number;
    }

    public String getCard_serial_number() {
        return card_serial_number;
    }

    public void setCard_serial_number(String card_serial_number) {
        this.card_serial_number = card_serial_number;
    }

    public String getCard_pin() {
        return card_pin;
    }

    public void setCard_pin(String card_pin) {
        this.card_pin = card_pin;
    }

    public String getProgrammeid() {
        return programmeid;
    }

    public void setProgrammeid(String programmeid) {
        this.programmeid = programmeid;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getIs_uploaded() {
        return is_uploaded;
    }

    public void setIs_uploaded(String is_uploaded) {
        this.is_uploaded = is_uploaded;
    }

    public String getMember_number() {
        return member_number;
    }

    public void setMember_number(String member_number) {
        this.member_number = member_number;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getHousenumber() {
        return housenumber;
    }

    public void setHousenumber(String housenumber) {
        this.housenumber = housenumber;
    }

    public String getSectionName() {
        return sectionName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isBio() {
        return bio;
    }

    public void setBio(boolean bio) {
        this.bio = bio;
    }

    public String getLoggedin() {
        return loggedin;
    }

    public void setLoggedin(String loggedin) {
        this.loggedin = loggedin;
    }

    public String getActivation() {
        return activation;
    }

    public void setActivation(String activation) {
        this.activation = activation;
    }

    public String getBioVerifyStatus() {
        return bioVerifyStatus;
    }

    public void setBioVerifyStatus(String bioVerifyStatus) {
        this.bioVerifyStatus = bioVerifyStatus;
    }
}
