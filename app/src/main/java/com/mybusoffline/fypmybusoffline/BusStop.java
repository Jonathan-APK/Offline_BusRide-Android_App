package com.mybusoffline.fypmybusoffline;

import java.io.Serializable;

/**
 * Created by darks on 20-May-18.
 */

public class BusStop implements Serializable {

    private String description;
    private String distance;
    private String roadName;
    private String busStopCode;
    private String latitude;
    private String longitude ;

    public BusStop(String distance, String busStopCode, String description, String roadName, String latitude, String longitude ){
        this.distance = distance;
        this.busStopCode = busStopCode;
        this.description = description;
        this.roadName = roadName;
        this.longitude  = longitude ;
        this.latitude = latitude;
    }

    public String getDistance() {
        return distance;
    }

    public String getBusStopCode() {
        return busStopCode;
    }

    public String getDescription() {
        return description;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

}
