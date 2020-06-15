package com.use.carimasjid.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.use.carimasjid.R;
import com.use.carimasjid.model.DataFilter;

import java.util.ArrayList;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterHolder>{

    private Context context;
    private OnClickListener mListener;
    private ArrayList<DataFilter> list;

    public FilterAdapter(Context context, ArrayList<DataFilter> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public FilterHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_type, null);


        return new FilterHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterHolder holder, final int position) {


        holder.tvNama.setText(list.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class FilterHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


        TextView tvNama;


        public FilterHolder(@NonNull View v) {
            super(v);

            tvNama = v.findViewById(R.id.tv_item_type);
            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position, list.get(position));
                }
            }
        }
    }

    public interface OnClickListener {
        void onItemClick(int position, DataFilter d);

    }
    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

}
