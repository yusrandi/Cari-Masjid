package com.use.carimasjid.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.use.carimasjid.R;

import java.util.ArrayList;

public class FacilitiesAdapter extends RecyclerView.Adapter<FacilitiesAdapter.FacilitiesHolder>{

    private Context context;
    private OnClickListener mListener;
    private ArrayList<String> list;

    public FacilitiesAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FacilitiesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_dialog_home_user, null);


        return new FacilitiesHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FacilitiesHolder holder, final int position) {

        holder.cb.setText(list.get(position));

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
