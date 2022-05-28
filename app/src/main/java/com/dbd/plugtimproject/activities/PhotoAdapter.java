package com.dbd.plugtimproject.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.FileUri;

import java.util.ArrayList;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoHolder> {

    private final ArrayList<FileUri> fileUriList;
    private final Context mContext;

    public PhotoAdapter(ArrayList<FileUri> fileUriList, Context mContext) {
        this.fileUriList = fileUriList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.layout_photo, parent, false);
        return new PhotoHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
        Glide.with(mContext).load(fileUriList.get(position).getUri()).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return fileUriList.size();
    }

    public static class PhotoHolder extends RecyclerView.ViewHolder {

        ImageView imageView;

        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.cardViewPhoto);
        }
    }
}
