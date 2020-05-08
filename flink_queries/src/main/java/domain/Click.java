package domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jackson.JacksonZonedDateTimeDeserializer;
import jackson.JacksonZonedDateTimeJsonSerializer;

import java.time.ZonedDateTime;

public class Click {

    private  String clickId;
    private  String siteId;
    private  String ip;
    private  String device;
    private String country;

    @JsonDeserialize(using = JacksonZonedDateTimeDeserializer.class)
    @JsonSerialize(using = JacksonZonedDateTimeJsonSerializer.class)
    private ZonedDateTime timestamp;

    public Click() {}

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

    public String getClickId() { return clickId; }

    public String getSiteId() { return siteId; }

    public String getIp() { return ip; }

    public String getDevice() { return device; }

    public String getCountry() { return country; }

    public ZonedDateTime getTimestamp() { return timestamp; }

    public void setClickId(String clickId) { this.clickId = clickId; }

    public void setSiteId(String siteId) { this.siteId = siteId; }

    public void setIp(String ip) { this.ip = ip; }

    public void setDevice(String device) { this.device = device; }

    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }

    public void setCountry(String country) {
        this.country = country;
    }


    @Override
    public String toString() {
        return "Click{" +
                "clickId='" + clickId + '\'' +
                ", timestamp=" + timestamp +
                ", siteId='" + siteId + '\'' +
                ", ip='" + ip + '\'' +
                ", device='" + device + '\'' +
                ", country='" + country + '\'' +
                '}';
    }


}
