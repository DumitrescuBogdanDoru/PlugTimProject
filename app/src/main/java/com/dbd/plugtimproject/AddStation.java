package com.dbd.plugtimproject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dbd.plugtimproject.models.LocationHelper;
import com.dbd.plugtimproject.models.Station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddStation extends AppCompatActivity {

    private Button addStationBtn;
    private EditText description, ports;
    private DatabaseReference mDatabase;
    private FusedLocationProviderClient fusedLocationClient;
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        addStationBtn = findViewById(R.id.addStationBtnForm);
        description = findViewById(R.id.description);
        ports = findViewById(R.id.ports);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        addStationBtn.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(AddStation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                        Location location = task.getResult();
                        if (location != null) {
                            String descriptionStation = description.getText().toString();
                            String portsStation = ports.getText().toString();
                            Geocoder geocoder = new Geocoder(AddStation.this, Locale.getDefault());
                            try {
                                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                                Station station = new Station(descriptionStation, Integer.parseInt(portsStation), new LocationHelper(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()), FirebaseAuth.getInstance().getUid());

                                String random = UUID.randomUUID().toString();

                                mDatabase.child("stations").child(random)
                                        .setValue(station).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Toast.makeText(AddStation.this, "Station has been added successfully", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(AddStation.this, "Failed to add your station. Please try again.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }


                        } else {
                            Toast.makeText(AddStation.this, "Couldn't get your location", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(AddStation.this, "Couldn't get your location", Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(AddStation.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
            }
        });
    }
}


