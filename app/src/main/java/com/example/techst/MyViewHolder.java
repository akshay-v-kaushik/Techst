package com.example.techst;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class MyViewHolder extends RecyclerView.ViewHolder {

    ImageView recyclerview_image;
    TextView recyclerview_imagename;
    TextView getRecyclerview_scannedtext;
    View v;
    public MyViewHolder(@NonNull  View itemView) {
        super(itemView);
        recyclerview_image = itemView.findViewById(R.id.recyclerview_image);
        recyclerview_imagename = itemView.findViewById(R.id.recyclerview_imagename);
        getRecyclerview_scannedtext = itemView.findViewById(R.id.recyclerview_scannedtext);
        v = itemView;
    }
}
