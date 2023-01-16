package com.umak.miers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FirstAidAdapter extends RecyclerView.Adapter<FirstAidAdapter.MyViewHolder> {

    Context context;
    ArrayList<Hotline> list;

    public FirstAidAdapter(Context context, ArrayList<Hotline> list) {
        this.context = context;
        this.list = list;
    }

    public void filterList(ArrayList<Hotline> filterlist) {
        list = filterlist;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.first_aid_items, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Hotline hotline = list.get(position);

        holder.first_aid_name.setText(hotline.getType());
        Picasso.get().load(hotline.getImage()).into(holder.first_aid_image);

        holder.first_aid_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, FirstAidImageActivity.class);
                i.putExtra("type", hotline.getType());
                i.putExtra("image", hotline.getImage());
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView first_aid_name;
        ImageView first_aid_image;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            first_aid_name = itemView.findViewById(R.id.textViewFirstAidType);
            first_aid_image = itemView.findViewById(R.id.imageViewFirstAidImage);
        }
    }
}
