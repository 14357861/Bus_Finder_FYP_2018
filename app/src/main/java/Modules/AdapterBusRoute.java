package Modules;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.Admin.ManageRoutesActivity;
import com.example.cianfdoherty.googlemapsapidemo.R;
import com.example.Customer.RoutesList;

import java.util.List;

/**
 * Created by CianFDoherty on 25-Feb-18.
 */

public class AdapterBusRoute extends ArrayAdapter<BusRoute> {

    private List<BusRoute> routes;
    private Activity activity;
    private static LayoutInflater inflater = null;

    public AdapterBusRoute(RoutesList routesList, int i, List<BusRoute> routes) {
        super(routesList, i, routes);
        try {
            this.activity = routesList;
            this.routes = routes;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {

        }
    }

    public AdapterBusRoute(ManageRoutesActivity manageRoutesList, int i, List<BusRoute> routes) {
        super(manageRoutesList, i, routes);
        try {
            this.activity = manageRoutesList;
            this.routes = routes;
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        } catch (Exception e) {

        }
    }

    public int getCount() {
        return routes.size();
    }

    public BusRoute getItem(BusRoute position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public static class ViewHolder {
        public TextView display_name;
        public TextView display_number;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.row, null);
                holder = new ViewHolder();

                holder.display_name = (TextView) vi.findViewById(R.id.text);
                //holder.display_number = (TextView) vi.findViewById(R.id.display_number);


                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }



            holder.display_name.setText(routes.get(position).getName());
            //holder.display_number.setText(routes.get(position).);


        } catch (Exception e) {


        }
        return vi;
    }
}
