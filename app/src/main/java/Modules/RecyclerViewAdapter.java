package Modules;

        import android.content.Context;
        import android.support.v7.widget.RecyclerView;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import com.bumptech.glide.Glide;
        import com.example.cianfdoherty.googlemapsapidemo.R;

        import java.util.List;


/**
 * Created by CianFDoherty on 29-Mar-18.
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    Context context;
    List<MapImage> MapImageList;

    public RecyclerViewAdapter(Context context, List<MapImage> TempList) {

        this.MapImageList = TempList;

        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MapImage UploadInfo = MapImageList.get(position);

        holder.imageNameTextView.setText(UploadInfo.getMapName());

        //Loading image from Glide library.
        Glide.with(context).load(UploadInfo.getMapURL()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {

        return MapImageList.size();
    }

    public String getItem(int position){
        return MapImageList.get(position).imageURL;
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public TextView imageNameTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.imageView);

            imageNameTextView = (TextView) itemView.findViewById(R.id.ImageNameTextView);
        }
    }
}
