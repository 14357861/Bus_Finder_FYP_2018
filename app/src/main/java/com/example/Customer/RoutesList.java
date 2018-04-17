package com.example.Customer;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cianfdoherty.googlemapsapidemo.R;

import java.util.ArrayList;
import java.util.List;

import Modules.AdapterBusRoute;
import Modules.BusRoute;

public class RoutesList extends AppCompatActivity {
    private List<BusRoute> routes = new ArrayList<BusRoute>();
    private AdapterBusRoute adapterBusRoute;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_routes_list);
        // below code makes toolbar white ??
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent i = getIntent();
        routes = (List<BusRoute>) i.getSerializableExtra("LIST");

        populateListView();
        registerClickCallback();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void populateListView() {


        adapterBusRoute = new AdapterBusRoute (RoutesList.this, 0, routes);

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
