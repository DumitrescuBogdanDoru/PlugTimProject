package com.dbd.plugtimproject.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.LocationHelper;
import com.dbd.plugtimproject.models.Station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddStation extends AppCompatActivity implements View.OnClickListener {

    private Button addStationBtn, addImageBtn;
    private EditText description, ports;
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private FusedLocationProviderClient fusedLocationClient;
    protected LocationManager locationManager;
    private ImageView imageAddStation;
    private Uri imageUri;

    boolean isType1 = false;
    boolean isType2 = false;
    boolean isCcs = false;
    boolean isChademo = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        description = findViewById(R.id.description);
        ports = findViewById(R.id.ports);
        imageAddStation = findViewById(R.id.imageAddStation);

        addImageBtn = findViewById(R.id.addImageBtnForm);
        addImageBtn.setOnClickListener(this);
        addStationBtn = findViewById(R.id.addStationBtnForm);
        addStationBtn.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addStationBtnForm:
                addStation();
                break;
            case R.id.addImageBtnForm:
                choosePicture();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            imageAddStation.setImageURI(imageUri);
        }
    }

    private void addStation() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(AddStation.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                    Location location = task.getResult();
                    if (location != null) {
                        String descriptionStation = description.getText().toString();
                        String portsStation = ports.getText().toString();

                        if (checkFields(descriptionStation, portsStation)) {
                            addStationInFirebase(location, descriptionStation, portsStation);
                        } else {
                            Toast.makeText(AddStation.this, "Please try again", Toast.LENGTH_SHORT).show();
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
    }

    private void addStationInFirebase(Location location, String descriptionStation, String portsStation) {
        Geocoder geocoder = new Geocoder(AddStation.this, Locale.getDefault());
        try {
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            // TODO check using latitudine and logitudine if another station is near
            Station station = new Station(descriptionStation, Integer.parseInt(portsStation), new LocationHelper(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()),
                    FirebaseAuth.getInstance().getUid(), isType1, isType2, isCcs, isChademo);

            String random = UUID.randomUUID().toString();

            mDatabase.child("stations").child(random)
                    .setValue(station).addOnCompleteListener(task1 -> {
                if (task1.isSuccessful()) {
                    Toast.makeText(AddStation.this, "Station has been added successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(AddStation.this, "Failed to add your station. Please try again.", Toast.LENGTH_SHORT).show();
                }
            });

            uploadPicture(random);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean checkFields(String descriptionStation, String portsStation) {
        if (descriptionStation.isEmpty() || descriptionStation.length() < 2) {
            description.setError("Please enter a valid name");
            description.requestFocus();
            return false;
        }
        if (Integer.parseInt(portsStation) < 0) {
            ports.setError("Please enter a valid number of ports");
            ports.requestFocus();
            return false;
        }
        return true;
    }

    private void choosePicture() {
        // intent to open gallery from phone
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, 1);
    }

    private void uploadPicture(String uuid) {
        final ProgressDialog pd = new ProgressDialog(AddStation.this);
        pd.setTitle("Uploading image");
        pd.show();

        StorageReference stationReference = storageReference.child("images/stations/" + uuid + "/");
        stationReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    Integer counter = 0;

                    @Override
                    public void onSuccess(ListResult listResult) {
                        counter = listResult.getPrefixes().size();

                        stationReference.child(counter.toString()).putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {
                                    pd.dismiss();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(AddStation.this, "Couldn't load image", Toast.LENGTH_SHORT).show();
                                })
                                .addOnProgressListener(snapshot -> {
                                    double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    pd.setMessage("Progress: " + (int) progress + "%");
                                });
                    }
                });
    }

    public void onCheckBoxChecked(View view) {
        CheckBox checkBox = findViewById(view.getId());

        switch (view.getId()) {
            case R.id.type1CheckBox:
                isType1 = checkBox.isChecked();
                break;
            case R.id.type2CheckBox:
                isType2 = checkBox.isChecked();
                break;
            case R.id.ccsCheckBox:
                isCcs = checkBox.isChecked();
                break;
            case R.id.chademoCheckBox:
                isChademo = checkBox.isChecked();
                break;
        }
    }
}


