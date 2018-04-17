package Modules;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

/**
 * Created by CianFDoherty on 11-Feb-18.
 */
public class Waypoint implements Serializable{
    private Double latitude;
    private Double longitude;

    public Waypoint() {
    }

    public Waypoint(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @PropertyName("latitude")
    public Double getLatitude() {
        return latitude;
    }
    @PropertyName("longitude")
    public Double getLongitude() {
        return longitude;
    }


    //@PropertyName("waypoints")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    //@PropertyName("waypoints")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
}