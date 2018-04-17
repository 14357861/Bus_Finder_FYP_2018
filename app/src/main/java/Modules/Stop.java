package Modules;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

/**
 * Created by CianFDoherty on 03-Mar-18.
 */

public class Stop implements Serializable {
    private Double latitude;
    private Double longitude;
    private String stop;

    public Stop() {
    }

    public Stop(Double latitude, Double longitude, String stop) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.stop= stop;
    }

    @PropertyName("latitude")
    public Double getLatitude() {
        return latitude;
    }
    @PropertyName("longitude")
    public Double getLongitude() {
        return longitude;
    }
    @PropertyName("stop")
    public String getStop() { return stop; }


    //@PropertyName("longitude")
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    //@PropertyName("latitude")
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    //@PropertyName("name")
    public void setName(String stop) {
        this.stop= stop;
    }
}