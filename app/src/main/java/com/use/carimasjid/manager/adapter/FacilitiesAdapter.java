package com.use.carimasjid.manager.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.use.carimasjid.R;
import com.use.carimasjid.manager.ManagerMainActivity;

import java.util.ArrayList;

public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.FacilitiesHolder>{

    private final String TAG = "FacilitiesAdapter";

    private Context context;
    private OnClickListener mListener;
    public ArrayList<String> itemChecked = new ArrayList<>();
    private ArrayList<String> list;

    public FacilitiesAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FacilitiesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_fr_step_three, null);


        return new FacilitiesHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilitiesHolder holder, final int position) {

        Log.d(TAG, "FacilitiesAdapter "+ ManagerMainActivity.LIST_FACILITIES);

        String fas[] = ManagerMainActivity.LIST_FACILITIES.split(",");
        for(int i = 0; i<fas.length; i++){
            if(list.get(position).equals(fas[i])){
                holder.cb.setChecked(true);
                itemChecked.add(list.get(position));
            }

        }

        holder.cb.setText(list.get(position));
        holder.cb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox checkBox = (CheckBox) view;
                if(checkBox.isChecked()){
                    itemChecked.add(list.get(position));
                }else if(!checkBox.isChecked()){
                    itemChecked.remove(list.get(position));

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FacilitiesHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        CheckBox cb;


        public FacilitiesHolder(@NonNull View v) {
            super(v);

            cb = v.findViewById(R.id.item_cb);

            cb.setOnClickListener(this);

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

}
