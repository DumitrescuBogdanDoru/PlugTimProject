package com.dbd.plugtimproject.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.StationActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private Context mContext;
    private List<com.dbd.plugtimproject.models.Notification> notificationList;

    public NotificationAdapter(Context mContext, List<com.dbd.plugtimproject.models.Notification> notificationList) {
        this.mContext = mContext;
        this.notificationList = notificationList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.notification_item, parent, false);
        return new NotificationAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        com.dbd.plugtimproject.models.Notification notification = notificationList.get(position);

        holder.notificationText.setText(notification.getText());
        getStationPhoto(holder.stationImage, notification.getStationId());

        // start station activity when the notification is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext.getApplicationContext(), StationActivity.class);
            intent.putExtra("uuid", notification.getStationId());
            mContext.startActivity(intent);
        });
    }


    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView stationImage;
        private TextView notificationText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            stationImage = itemView.findViewById(R.id.image_station_notification);
            notificationText = itemView.findViewById(R.id.text_notification);
        }
    }

    private void getStationPhoto(ImageView stationImage, String stationId) {
        FirebaseStorage storage = FirebaseStorage.getInstance();

        StorageReference pathReference = storage.getReference().child("images/stations/" + stationId + "/0");

        long MAXBYTES = 1024 * 1024 * 20;

        pathReference.getBytes(MAXBYTES).addOnSuccessListener(bytes -> {
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            stationImage.setImageBitmap(bitmap);
        });
    }
}
