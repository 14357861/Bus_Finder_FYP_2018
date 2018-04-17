package com.example.Admin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.cianfdoherty.googlemapsapidemo.LoginActivity;
import com.example.cianfdoherty.googlemapsapidemo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class AdminHomeActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth firebaseAuth;

    private Intent intent;

    private Button bLogout;
    private Button bNotification;
    private Button bRoutes;
    private Button bMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        firebaseAuth = FirebaseAuth.getInstance();

        bLogout = (Button) findViewById(R.id.admin_logout);
        bLogout.setOnClickListener(this);
        bNotification = (Button) findViewById(R.id.admin_notif);
        bNotification.setOnClickListener(this);
        bMap = (Button) findViewById(R.id.admin_map);
        bMap.setOnClickListener(this);
        bRoutes = (Button) findViewById(R.id.admin_routes);
        bRoutes.setOnClickListener(this);

        // Code for the wee email icon ( could be used for something esle at some point
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.admin_logout:
                firebaseAuth.getInstance().signOut();
                this.finish();
                intent = new Intent(AdminHomeActivity.this,LoginActivity.class);
                startActivity(intent);

            case R.id.admin_notif:
                Toast.makeText(AdminHomeActivity.this, "interface not implemented", Toast.LENGTH_SHORT).show();
                break;

            case R.id.admin_map:
                intent = new Intent(AdminHomeActivity.this,MapUploadActivity.class);
                startActivity(intent);
                break;

            case R.id.admin_routes:
                intent = new Intent(AdminHomeActivity.this,ManageRoutesActivity.class);
                startActivity(intent);
                break;

            default:
                break;
        }
    }
}
