package com.dbd.plugtimproject.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.StationActivity;
import com.dbd.plugtimproject.models.User;
import com.dbd.plugtimproject.models.Visit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StationVisitsAdapter extends RecyclerView.Adapter<StationVisitsAdapter.ViewHolder> {

    private Context mContext;
    private List<Visit> visitList;

    public StationVisitsAdapter(Context mContext, List<Visit> visitList) {
        this.mContext = mContext;
        this.visitList = visitList;
    }

    @NonNull
    @Override
    public StationVisitsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_visit_station, parent, false);
        return new StationVisitsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StationVisitsAdapter.ViewHolder holder, int position) {
        Visit visit = visitList.get(position);

        getText(holder, visit);
        holder.visitImage.setImageResource(getImage(visit));

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(mContext.getApplicationContext(), StationActivity.class);
            intent.putExtra("uuid", visit.getStationId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return visitList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView visitImage;
        private TextView visitText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            visitImage = itemView.findViewById(R.id.image_visit_station);
            visitText = itemView.findViewById(R.id.text_visit_station);
        }
    }

    private void getText(StationVisitsAdapter.ViewHolder holder, Visit visit) {

        FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("users/" + visit.getUserId())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        if (user != null) {
                            holder.visitText.setText(user.getFirstName() + " " + user.getLastName() + ": " + (visit.getComment().isEmpty() ? mContext.getString(R.string.visit_adapter_message) : visit.getComment()));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private int getImage(Visit visit) {
        return visit.isPositive() ? R.drawable.happygreen : R.drawable.sadred;
    }
}
