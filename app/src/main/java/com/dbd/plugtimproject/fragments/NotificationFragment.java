package com.dbd.plugtimproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.adapters.NotificationAdapter;
import com.dbd.plugtimproject.models.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotificationFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdapter notificationAdapter;
    private List<Notification> notificationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        recyclerView = view.findViewById(R.id.recyler_view_notification);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(getContext(), notificationList);
        recyclerView.setAdapter(notificationAdapter);

        readNotifications();

        return view;
    }

    private void readNotifications() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        mDatabase.child("notifications/" + firebaseUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Notification notification = dataSnapshot.getValue(Notification.class);
                    if (notification != null) {
                        notification.setText(getTextByNotificationType(notification.getText()));
                    }
                    notificationList.add(notification);
                }
                Collections.reverse(notificationList);
                notificationAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private String getTextByNotificationType(String text) {

        //String code = getActivity().getIntent().getStringExtra("lang");
        String[] strings = text.split(" ");

        switch (strings[2]) {
            case "like":
                /*
                if (code.equals("en")) {
                    return String.format("%s %s %s", strings[0], strings[1],  getString(R.string.hello););
                } else {
                    return String.format("%s %s a apreciat statia ta", strings[0], strings[1]);
                }

                 */
                return String.format("%s %s %s", strings[0], strings[1],  getString(R.string.notification_like));
            case "photo":
                /*
                if (code.equals("en")) {
                    return String.format("%s %s added a photo to your station", strings[0], strings[1]);
                } else {
                    return String.format("%s %s a adaugat o fotografie statiei tale", strings[0], strings[1]);
                }
                */
                return String.format("%s %s %s", strings[0], strings[1],  getString(R.string.notification_photo));
            case "visit":
                /*
                if (code.equals("en")) {
                    return String.format("%s %s added a visit to your station", strings[0], strings[1]);
                } else {
                    return String.format("%s %s a adaugat o vizita la statiei tale", strings[0], strings[1]);
                }
                */
                return String.format("%s %s %s", strings[0], strings[1],  getString(R.string.notification_visit));
        }
        return null;
    }
}