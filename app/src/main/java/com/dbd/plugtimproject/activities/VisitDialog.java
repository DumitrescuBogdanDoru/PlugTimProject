package com.dbd.plugtimproject.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.Notification;
import com.dbd.plugtimproject.models.Station;
import com.dbd.plugtimproject.models.User;
import com.dbd.plugtimproject.models.Visit;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.UUID;

public class VisitDialog extends AppCompatDialogFragment {

    private ImageView positiveImage, negativeImage;
    private EditText comment;
    private DatabaseReference mDatabase;

    private static final String TAG = "VisitDialog";

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_visit_dialog, null);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        String uuid = getActivity().getIntent().getStringExtra("uuid");

        positiveImage = view.findViewById(R.id.positive_visit_image);
        negativeImage = view.findViewById(R.id.negative_visit_image);
        comment = view.findViewById(R.id.comment_visit);

        positiveImage.setImageResource(R.drawable.happy);
        positiveImage.setTag("unclicked");
        negativeImage.setImageResource(R.drawable.sad);
        negativeImage.setTag("unclicked");

        positiveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (positiveImage.getTag().equals("unclicked")) {
                    positiveImage.setImageResource(R.drawable.happygreen);
                    negativeImage.setImageResource(R.drawable.sad);
                    positiveImage.setTag("clicked");
                    negativeImage.setTag("unclicked");
                    Log.i(TAG, "Happy clicked");
                } else {
                    positiveImage.setImageResource(R.drawable.happy);
                    positiveImage.setTag("unclicked");
                    Log.i(TAG, "Happy unclicked");
                }
            }
        });

        negativeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (negativeImage.getTag().equals("unclicked")) {
                    negativeImage.setImageResource(R.drawable.sadred);
                    positiveImage.setImageResource(R.drawable.happy);
                    negativeImage.setTag("clicked");
                    positiveImage.setTag("unclicked");
                    Log.i(TAG, "Sad clicked");
                } else {
                    negativeImage.setImageResource(R.drawable.sad);
                    negativeImage.setTag("unclicked");
                    Log.i(TAG, "Sad unclicked");
                }
            }
        });

        builder.setView(view)
                .setTitle("Add Visit")
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String visitId = UUID.randomUUID().toString();
                        String commentText = comment.getText().toString();
                        boolean isPositive = positiveImage.getTag().equals("clicked");
                        Visit visit = new Visit(visitId, FirebaseAuth.getInstance().getCurrentUser().getUid(), commentText, isPositive);
                        mDatabase.child("visits/" + uuid).child(visitId).setValue(visit);

                        sendVisitNotification(uuid);
                    }
                });


        return builder.create();
    }

    private void sendVisitNotification(String stationId) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = firebaseUser.getUid();

        mDatabase.child("/users/" + userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                String text = user.getFirstName() + " " + user.getLastName() + " added a visit to the station";

                mDatabase.child("/stations/" + stationId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Station station = snapshot.getValue(Station.class);
                        Notification notification = new Notification(userId, stationId, text);
                        if (!userId.equals(station.getAddedBy())) {
                            mDatabase.child("/notifications/" + station.getAddedBy() + "/" + UUID.randomUUID()).setValue(notification);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
