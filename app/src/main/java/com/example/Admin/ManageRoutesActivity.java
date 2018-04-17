package com.example.Admin;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.Customer.RoutesList;
import com.example.cianfdoherty.googlemapsapidemo.LoginActivity;
import com.example.cianfdoherty.googlemapsapidemo.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Modules.AdapterBusRoute;
import Modules.BusRoute;
import Modules.Waypoint;

public class ManageRoutesActivity extends AppCompatActivity {
    private List<BusRoute> routes = new ArrayList<BusRoute>();
    private AdapterBusRoute adapterBusRoute;
    private Intent intent;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference firebaseDatabase;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_routes);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        loadRoutes();
        registerClickCallback();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                intent = new Intent(ManageRoutesActivity.this,CreateRouteActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
                populateListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("Listener was cancelled err: " + databaseError.getCode());
            }
        });
    }

    private void populateListView() {

        adapterBusRoute = new AdapterBusRoute (ManageRoutesActivity.this, 0, routes);

        ListView listView = (ListView) findViewById(R.id.lvRouteList);
        listView.setAdapter(adapterBusRoute);

    }

    private void registerClickCallback() {
        mContext = this;
        ListView list = (ListView) findViewById(R.id.lvRouteList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setCancelable(true);
                builder.setTitle("Save route to database?");

                LinearLayout layout = new LinearLayout(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);

                final TextView from = new TextView (mContext);
                builder.setView(from);
                from.setText("\n    From:   "+adapterBusRoute.getItem(position).getFrom().toString());

                final TextView to = new TextView(mContext);
                builder.setView(to);
                to.setText("\n     To:      "+adapterBusRoute.getItem(position).getTo().toString());

                layout.addView(from);
                layout.addView(to);

                builder.setTitle(adapterBusRoute.getItem(position).getName().toString())
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // continue with delete
                            }
                        });
                builder.setView(layout).show();


            }
        });
    }

}
