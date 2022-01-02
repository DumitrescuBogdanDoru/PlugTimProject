package com.dbd.plugtimproject;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dbd.plugtimproject.databinding.ActivityMapsBinding;
import com.dbd.plugtimproject.models.Station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

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
                        MarkerOptions marker = new MarkerOptions().position(stationLocation).title(station.getDescription());

                        mMap.addMarker(marker);
                    } else {
                        Toast.makeText(MapsActivity.this, "LocationHelper", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        //@SuppressLint("MissingPermission") Location actualLocation =  fusedLocationClient.getLastLocation().getResult();
        //LatLng actualLatLng = new LatLng(actualLocation.getLatitude(), actualLocation.getLongitude());
        //mMap.moveCamera(CameraUpdateFactory.newLatLng());

    }
}