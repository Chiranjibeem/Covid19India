package com.covid19.india.Covid19India.model;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity(name="ERROR")
@EntityListeners(AuditingEntityListener.class)
public class ErrorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(name="USER_HOST")
    private String userHost;

    @Column(name="IP_ADDRESS")
    private String ipAddress;

    @Column(name="ACCESS_URL")
    private String accessURL;

    @Column(name="ERROR_MSG")
    private String errorMessgae;

    @CreatedDate
    @Column(name="CREATED_DATE")
    private Timestamp createdDate;

    @LastModifiedDate
    @Column(name="LAST_MOD_DATE")
    private Timestamp lastModifyTimestamp;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserHost() {
        return userHost;
    }

    public void setUserHost(String userHost) {
        this.userHost = userHost;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getAccessURL() {
        return accessURL;
    }

    public void setAccessURL(String accessURL) {
        this.accessURL = accessURL;
    }

    public String getErrorMessgae() {
        return errorMessgae;
    }

    public void setErrorMessgae(String errorMessgae) {
        this.errorMessgae = errorMessgae;
    }

    public Timestamp getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Timestamp createdDate) {
        this.createdDate = createdDate;
    }

    public Timestamp getLastModifyTimestamp() {
        return lastModifyTimestamp;
    }

    public void setLastModifyTimestamp(Timestamp lastModifyTimestamp) {
        this.lastModifyTimestamp = lastModifyTimestamp;
    }
}
