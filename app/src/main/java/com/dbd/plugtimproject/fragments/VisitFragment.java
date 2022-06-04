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
import com.dbd.plugtimproject.adapters.MyVisitsAdapter;
import com.dbd.plugtimproject.models.Visit;
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

public class VisitFragment extends Fragment {

    private RecyclerView recyclerView;
    private MyVisitsAdapter myVisitsAdapter;
    private List<Visit> visitList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_visit, container, false);

        recyclerView = view.findViewById(R.id.recyler_view_my_visit);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        visitList = new ArrayList<>();
        myVisitsAdapter = new MyVisitsAdapter(getContext(), visitList);
        recyclerView.setAdapter(myVisitsAdapter);

        readMyVisits();

        return view;
    }

    private void readMyVisits() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        mDatabase.child("visits/").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String stationId = dataSnapshot.getKey();
                    mDatabase.child("visits/").child(stationId).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            visitList.clear();
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Visit visit = dataSnapshot.getValue(Visit.class);
                                if (visit.getUserId().equals(firebaseUser.getUid())) {
                                    visitList.add(visit);
                                }
                            }
                            Collections.reverse(visitList);
                            myVisitsAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}