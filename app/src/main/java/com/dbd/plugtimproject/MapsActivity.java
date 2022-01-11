package com.dbd.plugtimproject;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.dbd.plugtimproject.databinding.ActivityMapsBinding;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;

    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;
    protected LocationManager locationManager;

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView descriptionMaps, portsMaps;
    private Button backBtnMaps;

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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

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
                        //MarkerOptions marker = new MarkerOptions().position(stationLocation).title(station.getDescription()).snippet("Number of ports: " + station.getNumberOfPorts().toString());
                        MarkerOptions marker = new MarkerOptions().position(stationLocation);
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

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(MapsActivity.this, Locale.getDefault());
                        try {
                            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            LatLng actual = new LatLng(addressList.get(0).getLatitude(), addressList.get(0).getLongitude());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(actual, 15));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } else {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        mMap.setOnMarkerClickListener(this);
    }

    public void createNewMapsDialog(String description, String ports) {
        dialogBuilder = new AlertDialog.Builder(this);
        final View mapsPopupView = getLayoutInflater().inflate(R.layout.popup, null);

        descriptionMaps = mapsPopupView.findViewById(R.id.descriptionMaps);
        portsMaps = mapsPopupView.findViewById(R.id.portsMaps);
        backBtnMaps = mapsPopupView.findViewById(R.id.backBtnMaps);

        descriptionMaps.setText(description);
        portsMaps.setText(ports);

        dialogBuilder.setView(mapsPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        backBtnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        //String description = marker.getTitle();
        //String ports = marker.getSnippet();

        LatLng position = marker.getPosition();
        double latitude = position.latitude;
        double longitudine = position.longitude;

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
                        String description = "";
                        String ports = "";


                        if (station.getLocationHelper().getLatitude() == latitude && station.getLocationHelper().getLongitude() == longitudine) {
                            description = station.getDescription();
                            ports = station.getNumberOfPorts().toString();
                            createNewMapsDialog(description, ports);
                        }

                    } else {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return false;
    }
}