package com.use.carimasjid.user.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.androidnetworking.widget.ANImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.use.carimasjid.R;
import com.use.carimasjid.api.UrlApi;
import com.use.carimasjid.model.DataMasjid;
import com.use.carimasjid.user.MapsActivity;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeHolder>{

    private static final String TAG = "HomeAdapter";

    private Context context;
    private OnClickListener mListener;
    private ArrayList<DataMasjid> list;

    public HomeAdapter(Context context, ArrayList<DataMasjid> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public HomeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_home, null);


        return new HomeHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeHolder holder, final int position) {
        DataMasjid d = list.get(position);
        holder.tvName.setText(d.getName());
        holder.tvDist.setText(d.getJarak());


        try {
            String[] s = d.getAdress().split(",");
            for(String ss : s){
//                Log.d(TAG, "string "+ss);
            }

            holder.tvAdress.setText(s[0]+", "+s[1]+", "+s[2]);

        }catch (Exception e){

        }


        Glide.with(context)
                .load(UrlApi.BASE_URL_IMAGES+d.getImage())
                .apply(new RequestOptions()
                        .placeholder(R.mipmap.my_location)
                        .error(R.mipmap.my_location))
                .into(holder.img);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class HomeHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        TextView tvName, tvAdress, tvDist;
        ImageView img;


        public HomeHolder(@NonNull View v) {
            super(v);
            tvName = v.findViewById(R.id.user_home_item_name);
            tvAdress = v.findViewById(R.id.user_home_item_adress);
            tvDist = v.findViewById(R.id.user_home_item_distance);
            img = v.findViewById(R.id.user_home_item_img);
            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }

    public interface OnClickListener {
        void onItemClick(int position);

    }
    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setFilter(List<DataMasjid> newList){
        list = new ArrayList<>();
        list.addAll(newList);
        notifyDataSetChanged();
    }
}
