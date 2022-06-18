package com.dbd.plugtimproject.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.StationActivity;
import com.dbd.plugtimproject.models.Station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MapsFragment extends Fragment implements GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private static final String TAG = "MapsFragment";
    private DatabaseReference mDatabase;
    private GoogleMap mMap;

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {

        @SuppressLint("MissingPermission")
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            mMap = googleMap;

            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                Location location = task.getResult();
                if (location != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

                    // Getting all the stations and pointing them on map
                    mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
                    List<Station> stationList = new ArrayList<>();

                    mDatabase.child("stations").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            stationList.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                Station station = postSnapshot.getValue(Station.class);
                                stationList.add(station);
                            }

                            for (Station station : stationList) {
                                if (station.getLocationHelper() != null) {
                                    LatLng stationLocation = new LatLng(station.getLocationHelper().getLatitude(), station.getLocationHelper().getLongitude());
                                    MarkerOptions marker = new MarkerOptions().position(stationLocation);
                                    mMap.addMarker(marker);
                                } else {
                                    Log.e(TAG, String.format("Couldn't get locationHelper for stations %s", station.getDescription()));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            });

            mMap.setOnMarkerClickListener(MapsFragment.this);
            mMap.setOnInfoWindowClickListener(MapsFragment.this);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        LatLng position = marker.getPosition();
        double latitude = position.latitude;
        double longitude = position.longitude;

        mDatabase.child("stations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Station station = dataSnapshot.getValue(Station.class);
                    if (station != null) {
                        if (station.getLocationHelper().getLatitude() == latitude && station.getLocationHelper().getLongitude() == longitude) {
                            marker.setTitle(station.getDescription());
                            marker.setSnippet(getPortTypes(station));
                            marker.showInfoWindow();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });

        return true;
    }

    @Override
    public void onInfoWindowClick(@NonNull Marker marker) {
        LatLng position = marker.getPosition();
        double latitude = position.latitude;
        double longitude = position.longitude;

        mDatabase.child("stations").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Station dbStation = dataSnapshot.getValue(Station.class);
                    if (dbStation.getLocationHelper().getLatitude() == latitude && dbStation.getLocationHelper().getLongitude() == longitude) {
                        Intent intent = new Intent(getActivity(), StationActivity.class);
                        intent.putExtra("uuid", dataSnapshot.getKey());
                        startActivity(intent);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private String getPortTypes(Station station) {
        List<String> types = new ArrayList<>();
        StringBuilder portTypes = new StringBuilder();

        if (station.isType1()) {
            types.add("Type1");
        }
        if (station.isType2()) {
            types.add("Type2");
        }
        if (station.isCcs()) {
            types.add("Ccs");
        }
        if (station.isChademo()) {
            types.add("Chademo");
        }

        for (String type : types) {
            portTypes.append(type).append(" ");
        }

        return portTypes.toString();
    }
}