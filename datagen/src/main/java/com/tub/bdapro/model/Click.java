package com.tub.bdapro.model;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.tub.bdapro.jackson.ZonedDateTimeSerializer;
import com.tub.bdapro.util.Json;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class Click {

    private  String clickId;
    @JsonSerialize(using = ZonedDateTimeSerializer.class)
    private ZonedDateTime timestamp;
    private  String siteId;
    private  String ip;
    private  String device;
    private  String country;

    public Click(){}

    public Click(
            final String clickId,
            final ZonedDateTime timestamp,
            final String siteId,
            final String ip,
            final String device,
            final String country
    )
    {
        this.clickId = clickId;
        this.timestamp = timestamp;
        this.siteId = siteId;
        this.ip = ip;
        this.device = device;
        this.country = country;

    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getClickId() {
        return clickId;
    }

    public void setClickId(String clickId) {
        this.clickId = clickId;
    }


    public String getSiteId() {
        return siteId;
    }

    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "Click{" +
                "clickId='" + clickId + '\'' +
                ", timestamp=" + timestamp +
                ", siteId='" + siteId + '\'' +
                ", ip='" + ip + '\'' +
                ", device='" + device + '\'' +
                '}';
    }

}
