package com.example.Admin;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.cianfdoherty.googlemapsapidemo.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import Modules.BusRoute;
import Modules.DirectionsJSONParser;
import Modules.Waypoint;

public class CreateRouteActivity extends AppCompatActivity implements OnMapReadyCallback {
    private Intent intent;
    private GoogleMap map;
    private ArrayList<LatLng> markerPoints;
    private Context mContext;
    private BusRoute route1 = new BusRoute();
    private String name;
    private String id;
    private String from;
    private String to;
    private DatabaseReference firebaseDatabase;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_route);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Initializing
        markerPoints = new ArrayList<LatLng>();
        mContext = this;

        // Getting reference to SupportMapFragment of the activity_main
        SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fm.getMapAsync(this);
        // Getting reference to Button
        Button btnDraw = (Button) findViewById(R.id.bDrawRoute);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setCancelable(true);
                builder.setTitle("Save route to database?");


                LinearLayout layout = new LinearLayout(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);

                // Set up the input for name
                final EditText inputName = new EditText(mContext);
                inputName.setInputType(InputType.TYPE_CLASS_TEXT);
                inputName.setHint("Route Name");
                layout.addView(inputName);
                // Set up the input for id
                final EditText inputId = new EditText(mContext);
                inputId.setInputType(InputType.TYPE_CLASS_TEXT);
                inputId.setHint("Route Id");
                layout.addView(inputId);
                // Set up the input for where route is from
                final EditText inputFrom = new EditText(mContext);
                inputFrom.setInputType(InputType.TYPE_CLASS_TEXT);
                inputFrom.setHint("From");
                layout.addView(inputFrom);
                // Set up the input for where route is going
                final EditText inputTo = new EditText(mContext);
                inputTo.setInputType(InputType.TYPE_CLASS_TEXT);
                inputTo.setHint("To");
                layout.addView(inputTo);

                builder.setView(layout);


                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        name = inputName.getText().toString();
                        id = inputId.getText().toString();
                        from = inputFrom.getText().toString();
                        to = inputTo.getText().toString();

                        if (route1 != null) {
                            if (!(name.equals("")) && !(id.equals("")) && !(to.equals("")) && !(from.equals(""))) {
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                DatabaseReference currentUserDB = firebaseDatabase.child("routes").child(name);
                                currentUserDB.child("id").setValue(id);
                                currentUserDB.child("name").setValue(name);
                                currentUserDB.child("from").setValue(from);
                                currentUserDB.child("to").setValue(to);

                                currentUserDB.child("waypoints").setValue(route1.getLocations());
                                name = null;
                                id = null;
                                from = null;
                                to = null;
                                // Removes all the points from Google Map
                                map.clear();
                                route1 = null;
                                // Removes all the points in the ArrayList
                                markerPoints.clear();
                            } else {
                                Toast.makeText(CreateRouteActivity.this, "Route wasnt saved, please fill in required fields ;)", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(CreateRouteActivity.this, "Route wasnt saved, please create one first ;)", Toast.LENGTH_SHORT).show();
                            name = null;
                            id = null;
                            from = null;
                            to = null;
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // Click event handler for Button btn_draw
        btnDraw.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Checks, whether start and end locations are captured
                if (markerPoints.size() >= 2) {
                    LatLng origin = markerPoints.get(0);

                    LatLng dest = markerPoints.get(markerPoints.size()-1);

                    // Getting URL to the Google Directions API
                    String url = getDirectionsUrl(origin, dest);

                    DownloadTask downloadTask = new DownloadTask();

                    // Start downloading json data from Google Directions API
                    downloadTask.execute(url);
                }

            }
        });
    }

    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        // Enable MyLocation Button in the Map
        map = googleMap;
        map.setMyLocationEnabled(true);

        // Setting onclick event listener for the map
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {

                // Up to 23 waypoints allowed in each request, whether client-side or server-side queries.
                if (markerPoints.size() >= 20) {
                    return;
                }

                markerPoints.add(point);

                // Creating MarkerOptions
                MarkerOptions options = new MarkerOptions();

                options.position(point);
                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                map.addMarker(options);

            }
        });

        // The map will be cleared on long click
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng point) {
                // Removes all the points from Google Map
                map.clear();
                route1 = null;
                // Removes all the points in the ArrayList
                markerPoints.clear();

            }
        });
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        String sensor = "sensor=false";

        // Waypoints
        String waypoints = "";
        for (int i = 1; i < markerPoints.size(); i++) {
            LatLng point = (LatLng) markerPoints.get(i);
            if (i == 1)
                waypoints = "waypoints=";
            waypoints += point.latitude + "," + point.longitude + "|";
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints;

        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    /**
     * A method to download json data from url
     */
    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();

            br.close();

        } catch (Exception e) {
            Log.d("Exception while downloading url", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(2);
                lineOptions.color(Color.RED);
            }
            // Code for populating the BusRoute Object to be potentially saved
            if (points != null) {
                for (int i = 0; i < points.size(); i++) {
                    Waypoint point1 = new Waypoint();
                    route1 = new BusRoute();
                    point1.setLatitude(points.get(i).latitude);
                    point1.setLongitude(points.get(i).longitude);
                    try {
                        route1.addLocation(point1);
                    } catch (NullPointerException e) {
                        //Toast.makeText(CreateRouteActivity.this, "Caught the NullPointerException!", Toast.LENGTH_SHORT).show();
                        Toast.makeText(CreateRouteActivity.this, "Error populating route. Try again, be careful!", Toast.LENGTH_SHORT).show();
                        map.clear();
                        markerPoints.clear();
                        return;
                    }
                }
            }


            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions == null) {
                Toast.makeText(CreateRouteActivity.this, "Error drawing route. Try again, be careful!", Toast.LENGTH_SHORT).show();
                // Clearing map if there is a problem drawing the route
                map.clear();
                markerPoints.clear();
            } else {
                map.addPolyline(lineOptions);
            }
        }
    }
}
