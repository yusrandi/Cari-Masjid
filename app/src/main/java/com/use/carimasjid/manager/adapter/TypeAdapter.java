package com.use.carimasjid.manager.adapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.use.carimasjid.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeAdapter extends RecyclerView.Adapter<TypeAdapter.TypeViewHolder> {

    private static final String TAG = "TypeAdapter";

    private Context context;
    private ArrayList<String> list = new ArrayList<>();
    private OnItemClickListener mListener;


    public TypeAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public TypeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_type, viewGroup, false);

        return new TypeViewHolder(v);


    }

    @Override
    public void onBindViewHolder(@NonNull TypeViewHolder holder, final int i) {

        holder.tvType.setText(list.get(i));

    }


    @Override
    public int getItemCount() {
        return list.size();
    }

    public class TypeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvType;



        public TypeViewHolder(@NonNull View view) {
            super(view);
             tvType = view.findViewById(R.id.tv_item_type);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null) {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    mListener.onItemClick(position);
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void setFilter(List<String> newList){
        list = new ArrayList<>();
        list.addAll(newList);
        notifyDataSetChanged();
    }



}
