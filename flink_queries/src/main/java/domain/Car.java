package domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jackson.JacksonZonedDateTimeDeserializer;
import jackson.JacksonZonedDateTimeJsonSerializer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class Car { //Car stream (carId, carType, roadType, streetId, timestamp)

    private String carId;
    private String carType;
    private String roadType;
    private Integer streetId;

    @JsonDeserialize(using = JacksonZonedDateTimeDeserializer.class)
    @JsonSerialize(using = JacksonZonedDateTimeJsonSerializer.class)
    private ZonedDateTime timestamp;


    public Car(){}

    /**
     * Create a car data record with given carType, roadType and streetId.
     *
     * @param carType
     * @param streetId
     */
    public Car(String carType, String roadType, Integer streetId) {
        this.carId = UUID.randomUUID().toString();
        this.streetId = streetId;
        this.carType = carType;
        this.roadType = roadType;
        this.timestamp = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    public String getCarId() {
        return carId;
    }

    public Integer getStreetId() {
        return streetId;
    }

    public String getCarType() {
        return carType;
    }

    public String getRoadType() { return roadType; }

    public ZonedDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setCarId(String carId) { this.carId = carId; }

    public void setCarType(String carType) { this.carType = carType; }

    public void setRoadType(String roadType) { this.roadType = roadType; }

    public void setStreetId(Integer streetId) { this.streetId = streetId; }


}
