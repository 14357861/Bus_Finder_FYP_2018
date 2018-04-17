package Modules;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.Customer.MapsActivity;
import com.example.cianfdoherty.googlemapsapidemo.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by CianFDoherty on 04-Mar-18.
 */

public class DirectionFinder extends ListActivity {
    private LocationManager mLocationManager;
    private DatabaseReference firebaseDatabase;
    private List<BusRoute> routes = new ArrayList<BusRoute>();
    private String destination;
    private Context mContext;
    private Address destinationLocation;

    private ArrayList<String> directionList = new ArrayList<String>();
    private ArrayAdapter<String> adapter;

    private int routeIndex;
    private int stopIndex;

    public DirectionFinder(MapsActivity mapsActivity, List<BusRoute> routes, String destination) {
        this.routes = routes;
        this.destination = destination;
        this.mContext = mapsActivity;
    }


    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager) mContext.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    @SuppressLint("ResourceType")
    public void find() {
        View layout = View.inflate(mContext, R.layout.route_planner, null);
        adapter = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1,
                directionList);

        ListView lv = (ListView) layout.findViewById(R.id.lvDirectionList);
        lv.setAdapter(adapter);

        Location myLocation = getLastKnownLocation();

        getLatLongFromPlace();

        int indexNearMe[] = NearestStop(myLocation.getLatitude(), myLocation.getLongitude());
        if (destinationLocation == null) {
            return;
        }
        int indexNearDestination[] = NearestStop(destinationLocation.getLatitude(), destinationLocation.getLongitude());

        // [] = {route, stop};
        Stop stopNearMe = routes.get(indexNearMe[0]).getStops().get(indexNearMe[1]);
        Stop stopNearDestination = routes.get(indexNearDestination[0]).getStops().get(indexNearDestination[1]);

        directionList.add("\n"
                + "\nGo to " + routes.get(indexNearMe[0]).getName().toString()
                + "\nEntry stop: \n>>" + routes.get(indexNearMe[0]).getStops().get(indexNearMe[1]).getStop()
                + "\n");

        int indexCurrentStop[] = null;
        int indexNextStop[] = NearestStop(stopNearMe.getLatitude(), stopNearMe.getLongitude());
        Stop nextStop = routes.get(indexNextStop[0]).getStops().get(indexNextStop[1]);

        String routeChange = routes.get(indexNearMe[0]).getName().toString();
        String currentRoute = routes.get(indexNextStop[0]).getName().toString();

        while (nextStop != stopNearDestination) {
            indexCurrentStop = indexNextStop;

            indexNextStop = NearestStop2(nextStop.getLatitude(), nextStop.getLongitude(), indexNextStop);
            nextStop = routes.get(indexNextStop[0]).getStops().get(indexNextStop[1]);


            currentRoute = routes.get(indexNextStop[0]).getName().toString();

            if (!routeChange.equals(currentRoute)) {

                directionList.add("\nExit stop: \n>>" + routes.get(indexCurrentStop[0]).getStops().get(indexCurrentStop[1]).getStop()
                        + "\nGo to " + currentRoute
                        + "\nNext stop : \n>>" + routes.get(indexNextStop[0]).getStops().get(indexNextStop[1]).getStop()
                        + "\n");

                routeChange = currentRoute;
            }

        }

        directionList.add("\nFinal stop: \n>>" + routes.get(indexNearDestination[0]).getStops().get(indexNearDestination[1]).getStop()
                + "\n");

        AlertDialog.Builder builder = new AlertDialog.Builder(
                mContext);

        builder.setTitle("Route Planner: " + destination);

        loadRoutes();

        ScrollView scrollPane = new ScrollView(mContext);
        scrollPane.addView(layout);

        builder.setView(scrollPane);

        // set dialog message
        builder
                .setCancelable(false)
                .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Toast.makeText(mContext, "Travel safe! ", Toast.LENGTH_SHORT).show();

                    }
                });

        // create alert dialog
        AlertDialog alertDialog = builder.create();

        // show it
        alertDialog.show();
    }

    private void loadRoutes() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        firebaseDatabase = database.getReference().child("routes");


        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                routes.clear();
                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    BusRoute route = snapshot.getValue(BusRoute.class);
                    routes.add(route);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Listener was cancelled err: " + databaseError.getCode());
            }
        });
    }

    public int[] NearestStop(Double latitude, Double longitude) {
        Double minDif = 999999.0;
        int route = 0;
        int stop = 0;
        stopIndex = 0;
        routeIndex = 0;

        for (BusRoute routeToMap : routes) {
            stopIndex = 0;

            for (Stop stopToMap : routeToMap.getStops()) {


                Double dif = PythagorasEquirectangular(latitude, longitude, stopToMap.getLatitude(), stopToMap.getLongitude());
                if (dif < minDif) {
                    stop = stopIndex;
                    route = routeIndex;
                    minDif = dif;
                }
                stopIndex++;
            }
            routeIndex++;
        }


        int result[] = {route, stop};
        return result;
    }

    public int[] NearestStop2(Double latitude, Double longitude, int indexCurrent[]) {

        routes.get(indexCurrent[0]).getStops().get(indexCurrent[1]).setLongitude(0.0);
        routes.get(indexCurrent[0]).getStops().get(indexCurrent[1]).setLatitude(0.0);

        Double minDif = 999999.0;
        int route = 0;
        int stop = 0;
        stopIndex = 0;
        routeIndex = 0;

        Double distanceToLocation = PythagorasEquirectangular(latitude, longitude,
                destinationLocation.getLatitude(), destinationLocation.getLongitude());

        Double newDistance = 99999999.99;
        while (distanceToLocation < newDistance) {
            for (BusRoute routeToMap : routes) {
                stopIndex = 0;

                for (Stop stopToMap : routeToMap.getStops()) {


                    Double dif = PythagorasEquirectangular(latitude, longitude, stopToMap.getLatitude(), stopToMap.getLongitude());
                    if (dif < minDif) {
                        stop = stopIndex;
                        route = routeIndex;
                        minDif = dif;
                    }
                    stopIndex++;
                }
                routeIndex++;
            }
            newDistance = PythagorasEquirectangular(routes.get(route).getStops().get(stop).getLatitude(), routes.get(route).getStops().get(stop).getLongitude(),
                    destinationLocation.getLatitude(), destinationLocation.getLongitude());
            if (distanceToLocation < newDistance) {
                routes.get(route).getStops().get(stop).setLatitude(0.0);
                routes.get(route).getStops().get(stop).setLongitude(0.0);
                minDif = 999999.0;
                route = 0;
                stop = 0;
                stopIndex = 0;
                routeIndex = 0;
            }
        }


        int result[] = {route, stop};
        return result;
    }


    public Double PythagorasEquirectangular(Double lat1, Double lon1, Double lat2, Double lon2) {
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);
        lon2 = Math.toRadians(lon2);
        int R = 6371; // km
        Double x = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
        Double y = (lat2 - lat1);
        Double d = Math.sqrt(x * x + y * y) * R;
        return d;
    }


    public void getLatLongFromPlace() {
        try {
            Geocoder selected_place_geocoder = new Geocoder(mContext);
            List<Address> address;

            address = selected_place_geocoder.getFromLocationName(destination, 5);

            if (address == null || address.size() == 0) {

                Toast.makeText(mContext, "We could not find the address, please try again. ", Toast.LENGTH_SHORT).show();
            } else {
                destinationLocation = address.get(0);
                Double lat = destinationLocation.getLatitude();
                Double lng = destinationLocation.getLongitude();
            }

        } catch (Exception e) {
            e.printStackTrace();
            fetchLatLongFromService fetch_latlng_from_service_abc = new fetchLatLongFromService(
                    destination.replaceAll("\\s+", ""));
            fetch_latlng_from_service_abc.execute();

        }

    }


//Sometimes happens that device gives location = null

    public class fetchLatLongFromService extends
            AsyncTask<Void, Void, StringBuilder> {
        String place;


        public fetchLatLongFromService(String place) {
            super();
            this.place = place;

        }

        @Override
        protected void onCancelled() {
            // TODO Auto-generated method stub
            super.onCancelled();
            this.cancel(true);
        }

        @Override
        protected StringBuilder doInBackground(Void... params) {
            // TODO Auto-generated method stub
            try {
                HttpURLConnection conn = null;
                StringBuilder jsonResults = new StringBuilder();
                String googleMapUrl = "http://maps.googleapis.com/maps/api/geocode/json?address="
                        + this.place + "&sensor=false";

                URL url = new URL(googleMapUrl);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(
                        conn.getInputStream());
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
                String a = "";
                return jsonResults;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(StringBuilder result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result.toString());
                JSONArray resultJsonArray = jsonObj.getJSONArray("results");

                // Extract the Place descriptions from the results
                // resultList = new ArrayList<String>(resultJsonArray.length());

                JSONObject before_geometry_jsonObj = resultJsonArray
                        .getJSONObject(0);

                JSONObject geometry_jsonObj = before_geometry_jsonObj
                        .getJSONObject("geometry");

                JSONObject location_jsonObj = geometry_jsonObj
                        .getJSONObject("location");

                String lat_helper = location_jsonObj.getString("lat");
                Double lat = Double.valueOf(lat_helper);


                String lng_helper = location_jsonObj.getString("lng");
                Double lng = Double.valueOf(lng_helper);


                LatLng point = new LatLng(lat, lng);


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }
        }
    }
}
