package com.dbd.plugtimproject.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.AddStation;
import com.dbd.plugtimproject.activities.StationActivity;
import com.dbd.plugtimproject.models.Station;
import com.dbd.plugtimproject.models.Visit;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MyVisitsAdapter extends RecyclerView.Adapter<MyVisitsAdapter.ViewHolder>{

    private Context mContext;
    private List<Visit> visitList;

    public MyVisitsAdapter(Context mContext, List<Visit> visitList) {
        this.mContext = mContext;
        this.visitList = visitList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.myvisit_item, parent, false);
        return new MyVisitsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
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

            visitImage = itemView.findViewById(R.id.image_my_visits);
            visitText = itemView.findViewById(R.id.text_my_visit);
        }
    }

    private void getText(ViewHolder holder, Visit visit) {
        FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference().child("/stations/" + visit.getStationId())
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Station station = snapshot.getValue(Station.class);
                        if (station != null) {
                            holder.visitText.setText(station.getDescription() + ": " + (visit.getComment().isEmpty() ? "No message" : visit.getComment()));
                        } else {
                            Toast.makeText(mContext, "Couldn't get station. Please try again!", Toast.LENGTH_SHORT).show();

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
