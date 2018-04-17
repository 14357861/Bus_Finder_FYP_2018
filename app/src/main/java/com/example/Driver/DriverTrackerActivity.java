package com.example.Driver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
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
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cianfdoherty.googlemapsapidemo.LoginActivity;
import com.example.cianfdoherty.googlemapsapidemo.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class DriverTrackerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        android.location.LocationListener,
        OnMapReadyCallback {

    protected GoogleApiClient mGoogleApiClient;
    private DatabaseReference databaseReference;
    private DatabaseReference mDatabaseLocationDetails;
    private DatabaseReference mDatabaseUser;
    private FirebaseAuth firebaseAuth;

    GoogleMap mGoogleMap;

    private Button bLocationTrack;
    private ImageButton bUpdateRoute;
    private ImageButton bUpdateName;
    private TextView tvEmail;
    private TextView tvCurrentRoute;
    private TextView tvCurrentName;
    private EditText etRouteName;
    private EditText etDriverName;
    private Boolean bLocationToggle;
    private String sCurrentRoute;
    private String sCurrentName;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;

    private Location location;
    private double latitude;
    private double longitude;
    private static int time = 5;
    private static final long MIN_TIME_BW_UPDATES = 1000 * time;

    protected LocationManager mlocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_tracker);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //    **INSERT-START**     //
        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        mDatabaseLocationDetails = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("location");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
        //  Database access for user
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        //  Button to enable/disable location tracking
        bLocationTrack = (Button) this.findViewById(R.id.bLocationTrack);
        //  Checks the last known value of the location tracking
        mDatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                bLocationToggle = Boolean.valueOf(dataSnapshot.child("tracking").getValue().toString());
                if(bLocationToggle == TRUE) {
                    bLocationTrack.setText("Disable Tracking");
                } else if (bLocationToggle == FALSE) {
                    bLocationTrack.setText("Enable Tracking");
                }
                sCurrentRoute = dataSnapshot.child("route").getValue().toString();
                sCurrentName = dataSnapshot.child("user").getValue().toString();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        //  Changes text on location tracking button & updates db value
        bLocationTrack.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if ("Enable Tracking".equals(bLocationTrack.getText())) {
                    bLocationTrack.setText("Disable Tracking");
                    bLocationToggle = TRUE;
                    mDatabaseUser.child("tracking").setValue("TRUE");

                } else {
                    bLocationTrack.setText("Enable Tracking");
                    bLocationToggle = FALSE;
                    mDatabaseUser.child("tracking").setValue("FALSE");
                }
            }
        });

        //* Map onCreate Code Start *//
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //    **INSERT-END**     //

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }


    //    **INSERT-START**     //


    private void saveDriverLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        mDatabaseLocationDetails.child("longitude").setValue(longitude);
        mDatabaseLocationDetails.child("latitude").setValue(latitude);
    }

    @SuppressLint("MissingPermission")
    public void getLocation() {
        try {
            mlocationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            isGPSEnabled = mlocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            isNetworkEnabled = mlocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                // First get location from Network Provider
                if (isNetworkEnabled) {
                    //noinspection MissingPermission
                    mlocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, 0, this);
                    Log.d("Network", "Network");
                    if (mlocationManager != null) {
                        //noinspection MissingPermission
                        location = mlocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            if (bLocationToggle) {
                                mGoogleMap.clear();
                                MarkerOptions mp = new MarkerOptions();
                                mp.position(new LatLng(latitude, longitude));
                                mp.title("my position");

                                mGoogleMap.addMarker(mp);

                                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(latitude, longitude), 16));
                                if (firebaseAuth.getCurrentUser() != null) {
                                    saveDriverLocation(location);
                                }
                            }
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (location == null) {
                        //noinspection MissingPermission
                        mlocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, 0, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (mlocationManager != null) {
                            //noinspection MissingPermission
                            location = mlocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                if (bLocationToggle) {
                                    mGoogleMap.clear();
                                    MarkerOptions mp = new MarkerOptions();
                                    mp.position(new LatLng(latitude, longitude));
                                    mp.title("my position");

                                    mGoogleMap.addMarker(mp);

                                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                                            new LatLng(latitude, longitude), 16));
                                    if (firebaseAuth.getCurrentUser() != null) {
                                        saveDriverLocation(location);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(this);

        mGoogleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        getLocation();
    }


    //    **INSERT-END**     //


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
        getMenuInflater().inflate(R.menu.driver_tracker, menu);

        firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        tvEmail = (TextView) findViewById(R.id.tvEmailNav);
        tvEmail.setText(user.getEmail());

        tvCurrentRoute = (TextView) findViewById(R.id.tvCurrentRoute);
        tvCurrentRoute.setText(sCurrentRoute);

        tvCurrentName = (TextView) findViewById(R.id.tvCurrentName);
        tvCurrentName.setText(sCurrentName);

        final String user_id = firebaseAuth.getCurrentUser().getUid();
        mDatabaseUser = FirebaseDatabase.getInstance().getReference().child("users").child(user_id);
        bUpdateRoute = (ImageButton) this.findViewById(R.id.bUpdateRoute);
        etRouteName = (EditText) this.findViewById(R.id.etRouteName);
        bUpdateRoute.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mDatabaseUser.child("route").setValue(etRouteName.getText().toString());

                Toast.makeText(DriverTrackerActivity.this, "Route updated to " + etRouteName.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        etDriverName = (EditText) this.findViewById(R.id.etDriverName);
        bUpdateName = (ImageButton) this.findViewById(R.id.bUpdateName);
        bUpdateName.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                mDatabaseUser.child("user").setValue(etDriverName.getText().toString());

                Toast.makeText(DriverTrackerActivity.this, "Name updated to " + etDriverName.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        return true;
    }



    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_logout) {

            firebaseAuth.signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    @Override
    public void onLocationChanged(Location location) {
        getLocation();
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
