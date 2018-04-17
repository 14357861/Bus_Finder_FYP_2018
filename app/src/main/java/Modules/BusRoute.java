package Modules;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by CianFDoherty on 11-Feb-18.
 */
public class BusRoute implements Serializable{

    private ArrayList<Stop> stops = new ArrayList<Stop>();
    private ArrayList<Waypoint> locations = new ArrayList<Waypoint>();
    private String name;
    private String from;
    private String to;


    @SuppressWarnings("unused")
    public BusRoute() {

    }

    @PropertyName("name")
    public String getName() {
        return name;
    }
    @PropertyName("name")
    public void setName(String name) {
        this.name = name;
    }
    @PropertyName("from")
    public String getFrom() {
        return from;
    }
    @PropertyName("from")
    public void setFrom(String from) {
        this.from = from;
    }
    @PropertyName("to")
    public String getTo() {
        return to;
    }
    @PropertyName("to")
    public void setTo(String to) {
        this.to = to;
    }

    @PropertyName("waypoints")
    public ArrayList<Waypoint> getLocations() {
        return locations;
    }
    @PropertyName("waypoints")
    public void setLocations(ArrayList<Waypoint> locations) {
        this.locations = locations;
    }

    @PropertyName("stops")
    public ArrayList<Stop> getStops() {
        return stops;
    }
    @PropertyName("stops")
    public void setStops(ArrayList<Stop> stops) {this.stops = stops;}

    public void addLocation(Waypoint point1) {
        this.locations.add(point1);
    }
}