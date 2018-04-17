package com.example.Customer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cianfdoherty.googlemapsapidemo.LoginActivity;
import com.example.cianfdoherty.googlemapsapidemo.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import Modules.BusRoute;
import Modules.DirectionFinder;
import Modules.Stop;
import Modules.Waypoint;

import static android.widget.Toast.LENGTH_SHORT;

public class MapsActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, LocationListener {

    private GoogleMap mMap;
    private Button btnFindPath;
    private EditText etOrigin;
    private TextView tvUserName;
    private TextView tvEmail;
    private List<Marker> driverMarkers = new ArrayList<>();
    private List<Marker> stopMarkers = new ArrayList<>();
    private final List<BusRoute> routes = new ArrayList<BusRoute>();
    protected LocationManager mlocationManager;

    private int busIcons[] =
            {
                    R.drawable.bus_marker_icon_blue,
                    R.drawable.bus_marker_icon_red,
                    R.drawable.bus_marker_icon_yellow,
                    R.drawable.bus_marker_icon_green,
                    R.drawable.bus_marker_icon_purple
            };
    private int routeColours[] =
            {
                    Color.BLUE,
                    Color.RED,
                    Color.GREEN,
                    Color.BLACK,
                    Color.CYAN
            };

    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mlocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

        //* Map onCreate Code Start *//
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnFindPath = (Button) findViewById(R.id.btnFindPath);
        etOrigin = (EditText) findViewById(R.id.etOrigin);


        btnFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendRequest();
            }
        });
        //  Decide whether or not to use intent or listener for loading driver locations

        loadRoutes();
        loadDrivers();

        //* Map onCreate Code End*//


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void loadDrivers() {

        firebaseDatabase = FirebaseDatabase.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();

        firebaseDatabase.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int i = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userType = snapshot.child("type").getValue().toString();
                    if (userType.equals("driver")) {

                        LatLng latLng = new LatLng(((Double) snapshot.child("location").child("latitude").getValue()),
                                (Double) snapshot.child("location").child("longitude").getValue());
                        String name = snapshot.child("user").getValue().toString();
                        String route = snapshot.child("route").getValue().toString();
                        driverMarkers.add(mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(busIcons[i]))
                                .title(name+" - "+route)
                                .position(latLng)));
                        i++;
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        firebaseDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int i = 0;

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userType = snapshot.child("type").getValue().toString();
                    if (userType.equals("driver")) {

                        LatLng latLng = new LatLng(((Double) snapshot.child("location").child("latitude").getValue()),
                                (Double) snapshot.child("location").child("longitude").getValue());
                        String name = snapshot.child("user").getValue().toString();

                        driverMarkers.get(i).setPosition(latLng);

                        i++;
                    }
                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Listener was cancelled err: " + databaseError.getCode());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.maps_activity2, menu);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        tvUserName = (TextView) findViewById(R.id.tvUserNameNav);
        tvEmail = (TextView) findViewById(R.id.tvEmailNav);
        tvUserName.setText(user.getDisplayName());
        tvEmail.setText(user.getEmail());

        return true;
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_routes) {
            Intent intent = new Intent(getApplicationContext(), RoutesList.class);
            intent.putExtra("LIST", (Serializable) routes);
            startActivity(intent);
            //startActivity(new Intent(getApplicationContext(), RoutesList.class));
        } else if (id == R.id.nav_feedback) {
            Toast.makeText(this, "Not yet implemented", LENGTH_SHORT).show();
        } else if (id == R.id.nav_route_map) {
            Intent intent = new Intent(getApplicationContext(), MapListActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_logout) {

            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void loadRoutes() {
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        firebaseDatabase = database.getReference().child("routes");


        firebaseDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                int i = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {

                    BusRoute route = snapshot.getValue(BusRoute.class);
                    routes.add(route);
                }

                PolylineOptions polylineOptions[] = new PolylineOptions[routes.size()];

                for (BusRoute routeToMap : routes) {

                    polylineOptions[i] = new PolylineOptions().
                            geodesic(true).
                            color(routeColours[i % 3]).
                            width(10).
                            clickable(true);
                    for (Stop stopToMap : routeToMap.getStops()) {
                        stopMarkers.add(mMap.addMarker(new MarkerOptions()
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.bus_stop))
                                .title(stopToMap.getStop())
                                .position(new LatLng(stopToMap.getLatitude(), stopToMap.getLongitude()))));
                    }

                    for (Waypoint pointToMap : routeToMap.getLocations())
                        polylineOptions[i].add(new LatLng(pointToMap.getLatitude(), pointToMap.getLongitude()));

                    mMap.addPolyline(polylineOptions[i]);
                    i = i + 1;

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Listener was cancelled err: " + databaseError.getCode());
            }
        });
    }

    // *Start MAP code*//
    // function for "Where would you like to go?"
    private void sendRequest() {

        String destination = etOrigin.getText().toString();
        if (destination.isEmpty()) {
            Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
            return;
        }

        new DirectionFinder(this, routes, destination).find();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onLocationChanged(Location location) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(location.getLatitude(), location.getLongitude()), 15));
        mlocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
