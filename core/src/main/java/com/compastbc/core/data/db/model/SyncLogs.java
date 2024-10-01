package com.compastbc.core.data.db.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;

/**
 * Created by Hemant Sharma on 13-02-20.
 * Divergent software labs pvt. ltd
 */
@Entity(nameInDb = "SyncLogs")
public class SyncLogs {

    @Id
    private Long id;
    @Unique
    private String uniqueId;
    private String reason;
    private String status;
    private String send_by;
    private String send_by_deviceId;
    private Date send_date;
    private Date received_date;
    private String received_by;
    private String received_deviceId;
    private String upload_by;
    private Date upload_date;
    private String upload_deviceId;
    private String total_transaction = "0";
    private String total_amount = "0.0";
    private String programId;
    private Date startDate;
    private Date endDate;
    private String programCurrency;

    @Generated(hash = 407364333)
    public SyncLogs(Long id, String uniqueId, String reason, String status,
                    String send_by, String send_by_deviceId, Date send_date,
                    Date received_date, String received_by, String received_deviceId,
                    String upload_by, Date upload_date, String upload_deviceId,
                    String total_transaction, String total_amount, String programId,
                    Date startDate, Date endDate, String programCurrency) {
        this.id = id;
        this.uniqueId = uniqueId;
        this.reason = reason;
        this.status = status;
        this.send_by = send_by;
        this.send_by_deviceId = send_by_deviceId;
        this.send_date = send_date;
        this.received_date = received_date;
        this.received_by = received_by;
        this.received_deviceId = received_deviceId;
        this.upload_by = upload_by;
        this.upload_date = upload_date;
        this.upload_deviceId = upload_deviceId;
        this.total_transaction = total_transaction;
        this.total_amount = total_amount;
        this.programId = programId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.programCurrency = programCurrency;
    }

    @Generated(hash = 354745934)
    public SyncLogs() {
    }

    public String getProgramCurrency() {
        return programCurrency;
    }

    public void setProgramCurrency(String programCurrency) {
        this.programCurrency = programCurrency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSend_by() {
        return send_by;
    }

    public void setSend_by(String send_by) {
        this.send_by = send_by;
    }

    public String getSend_by_deviceId() {
        return send_by_deviceId;
    }

    public void setSend_by_deviceId(String send_by_deviceId) {
        this.send_by_deviceId = send_by_deviceId;
    }

    public Date getSend_date() {
        return send_date;
    }

    public void setSend_date(Date send_date) {
        this.send_date = send_date;
    }

    public Date getReceived_date() {
        return received_date;
    }

    public void setReceived_date(Date received_date) {
        this.received_date = received_date;
    }

    public String getReceived_by() {
        return received_by;
    }

    public void setReceived_by(String received_by) {
        this.received_by = received_by;
    }

    public String getReceived_deviceId() {
        return received_deviceId;
    }

    public void setReceived_deviceId(String received_deviceId) {
        this.received_deviceId = received_deviceId;
    }

    public String getUpload_by() {
        return upload_by;
    }

    public void setUpload_by(String upload_by) {
        this.upload_by = upload_by;
    }

    public Date getUpload_date() {
        return upload_date;
    }

    public void setUpload_date(Date upload_date) {
        this.upload_date = upload_date;
    }

    public String getUpload_deviceId() {
        return upload_deviceId;
    }

    public void setUpload_deviceId(String upload_deviceId) {
        this.upload_deviceId = upload_deviceId;
    }

    public String getTotal_transaction() {
        return total_transaction == null || total_transaction.isEmpty() ? "0" : total_transaction;
    }

    public void setTotal_transaction(String total_transaction) {
        this.total_transaction = total_transaction;
    }

    public String getTotal_amount() {
        return total_amount == null || total_amount.isEmpty() ? "0" : total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
