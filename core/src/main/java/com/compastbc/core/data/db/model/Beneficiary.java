package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

@Entity(nameInDb = "Beneficiary")
public class Beneficiary {

    @Id
    private Long id;
    private String firstName;
    private String beneficiaryId;
    private String lastName;
    private String signature;
    private String image;
    @Unique
    private String identityNo;
    private String gender;
    private String dateOfBirth;
    private String cardNumber;
    private String cardPin;
    private String programmeId;
    private String isUploaded;
    private String memberNumber;
    private String mobile;
    private String address;
    private String sectionName;
    private String agentId;
    private String deviceId;
    private boolean bio = false;
    private String activation;
    private String bioVerifyStatus = "PENDING";
    private String cardSerialNumber;

    public String getCardSerialNumber() {
        return cardSerialNumber;
    }

    public void setCardSerialNumber(String cardSerialNumber) {
        this.cardSerialNumber = cardSerialNumber;
    }

    @Generated(hash = 237875129)
    public Beneficiary() {
    }

    @Generated(hash = 656713811)
    public Beneficiary(Long id, String firstName, String beneficiaryId,
            String lastName, String signature, String image, String identityNo,
            String gender, String dateOfBirth, String cardNumber, String cardPin,
            String programmeId, String isUploaded, String memberNumber,
            String mobile, String address, String sectionName, String agentId,
            String deviceId, boolean bio, String activation, String bioVerifyStatus,
            String cardSerialNumber) {
        this.id = id;
        this.firstName = firstName;
        this.beneficiaryId = beneficiaryId;
        this.lastName = lastName;
        this.signature = signature;
        this.image = image;
        this.identityNo = identityNo;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.cardNumber = cardNumber;
        this.cardPin = cardPin;
        this.programmeId = programmeId;
        this.isUploaded = isUploaded;
        this.memberNumber = memberNumber;
        this.mobile = mobile;
        this.address = address;
        this.sectionName = sectionName;
        this.agentId = agentId;
        this.deviceId = deviceId;
        this.bio = bio;
        this.activation = activation;
        this.bioVerifyStatus = bioVerifyStatus;
        this.cardSerialNumber = cardSerialNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getBeneficiaryId() {
        return beneficiaryId;
    }

    public void setBeneficiaryId(String beneficiaryId) {
        this.beneficiaryId = beneficiaryId;
    }

    public String getLastName() {
        return lastName == null ? "" : lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getIdentityNo() {
        return identityNo;
    }

    public void setIdentityNo(String identityNo) {
        this.identityNo = identityNo;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardPin() {
        return cardPin;
    }

    public void setCardPin(String cardPin) {
        this.cardPin = cardPin;
    }

    public String getProgrammeId() {
        return programmeId;
    }

    public void setProgrammeId(String programmeId) {
        this.programmeId = programmeId;
    }

    public String getIsUploaded() {
        return isUploaded;
    }

    public void setIsUploaded(String isUploaded) {
        this.isUploaded = isUploaded;
    }

    public String getMemberNumber() {
        return memberNumber;
    }

    public void setMemberNumber(String memberNumber) {
        this.memberNumber = memberNumber;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAddress() {
        return address == null || address.equals("null") ? "" : address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public boolean getBio() {
        return this.bio;
    }

    public void setBio(boolean bio) {
        this.bio = bio;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
