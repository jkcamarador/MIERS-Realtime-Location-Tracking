package com.umak.miers;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class CallAdapter extends RecyclerView.Adapter<CallAdapter.MyViewHolder> {

    Context context;

    ArrayList<Hotline> list;
    static int PERMISSION_CODE = 100;

    public CallAdapter(Context context, ArrayList<Hotline> list) {
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
        View v = LayoutInflater.from(context).inflate(R.layout.call_items, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Hotline hotline = list.get(position);

        holder.barangay.setText(hotline.getSource());
        holder.number = hotline.getHotline();

        holder.call_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = ("tel:" + holder.number);

                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_CODE);
                } else {
                    Intent i = new Intent(Intent.ACTION_CALL);
                    i.setData(Uri.parse(number));
                    context.startActivity(i);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView barangay;
        ImageView call_button;
        String number;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            barangay = itemView.findViewById(R.id.textViewOneTapPlace);
            call_button = itemView.findViewById(R.id.imageViewCall);
        }
    }
}
