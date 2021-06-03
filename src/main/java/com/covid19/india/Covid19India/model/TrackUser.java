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

@Entity(name = "TRACK_USER")
@EntityListeners(AuditingEntityListener.class)
public class TrackUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="TRACK_ID")
    private int trackId;

    @Column(name="USER_HOST")
    private String userHost;

    @Column(name="IP_ADDRESS")
    private String ipAddress;

    @Column(name="ACCESS_URL")
    private String accessURL;

    @CreatedDate
    @Column(name="CREATED_DATE")
    private Timestamp createdDate;

    @LastModifiedDate
    @Column(name="LAST_MOD_DATE")
    private Timestamp lastModifyTimestamp;

    public int getTrackId() {
        return trackId;
    }

    public void setTrackId(int trackId) {
        this.trackId = trackId;
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
