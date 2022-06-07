package com.dbd.plugtimproject.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.fragments.MapsFragment;
import com.dbd.plugtimproject.fragments.NotificationFragment;
import com.dbd.plugtimproject.fragments.ProfileFragment;
import com.dbd.plugtimproject.fragments.VisitFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton floatingActionButton;
    private FusedLocationProviderClient fusedLocationClient;
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setBackground(null);
        // disable holder item
        bottomNavigationView.getMenu().getItem(2).isEnabled();

        bottomNavigationView.setOnNavigationItemSelectedListener(this);

        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), AddStation.class)));

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Log.d(TAG, String.format("Getting location. Permission already given by user %s.", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        //Log.d(TAG, String.format("Localization completed. Loading maps fragment for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                        loadFragment(new MapsFragment());
                    }
                });
            }
        } else {
            //Log.d(TAG, String.format("Requesting location permission to user %s.", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.map:
                //Log.d(TAG, String.format("Maps fragment loaded for user %s.", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                fragment = new MapsFragment();
                break;

            case R.id.chat:
                //Log.d(TAG, String.format("Visit fragment loaded for user %s.", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                fragment = new VisitFragment();
                break;

            case R.id.notifications:
                //Log.d(TAG, String.format("Notification fragment loaded for user %s.", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                fragment = new NotificationFragment();
                break;

            case R.id.profile:
                //Log.d(TAG, String.format("Profile fragment loaded for user %s.", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                fragment = new ProfileFragment();
                break;
        }

        return loadFragment(fragment);
    }
}