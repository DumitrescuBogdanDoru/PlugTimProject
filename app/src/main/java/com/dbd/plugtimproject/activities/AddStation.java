package com.dbd.plugtimproject.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.ml.ModelUnquant;
import com.dbd.plugtimproject.models.FileUri;
import com.dbd.plugtimproject.models.LocationHelper;
import com.dbd.plugtimproject.models.Station;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class AddStation extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AddStation";

    private EditText description, ports;
    private DatabaseReference mDatabase;
    private StorageReference storageReference;
    protected LocationManager locationManager;
    private ImageView imageAddStation;
    private Uri imageUri;

    boolean isType1 = false;
    boolean isType2 = false;
    boolean isCcs = false;
    boolean isChademo = false;

    int imageSize = 224;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_station);

        description = findViewById(R.id.description);
        ports = findViewById(R.id.ports);
        imageAddStation = findViewById(R.id.imageAddStation);

        Button addImageBtn = findViewById(R.id.addImageBtnForm);
        addImageBtn.setOnClickListener(this);
        Button addStationBtn = findViewById(R.id.addStationBtnForm);
        addStationBtn.setOnClickListener(this);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
    }

    @SuppressLint("NonConstantResourceId")
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

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                int dimension = Math.min(bitmap.getWidth(), bitmap.getHeight());
                Bitmap image = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension);

                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                if (checkImage(image)) {
                    imageAddStation.setImageURI(imageUri);
                } else {
                    Toast.makeText(AddStation.this, getString(R.string.station_image_not_recognised), Toast.LENGTH_LONG).show();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean checkImage(Bitmap image) {
        try {
            ModelUnquant model = ModelUnquant.newInstance(getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] pixelsValues = new int[imageSize * imageSize];
            image.getPixels(pixelsValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = pixelsValues[pixel++];
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ModelUnquant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidence = outputFeature0.getFloatArray();
            Log.i(TAG, "Electric Station " + confidence[0]);
            Log.i(TAG, "Gas Pump " + confidence[1]);

            if (confidence[0] > confidence[1] && confidence[0] > 0.5) {
                model.close();
                return true;
            } else {
                model.close();
                return false;
            }

            // Releases model resources if no longer used.
        } catch (IOException e) {
            return false;
        }

    }

    private void addStation() {
        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

            // verify that at least one type of port is selected
            if (isType1 || isType2 || isCcs || isChademo) {
                Station station = new Station(descriptionStation, Integer.parseInt(portsStation), new LocationHelper(addressList.get(0).getLatitude(), addressList.get(0).getLongitude()),
                        FirebaseAuth.getInstance().getUid(), isType1, isType2, isCcs, isChademo);

                String random = UUID.randomUUID().toString();

                mDatabase.child("stations").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean exists = false;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            Station existing = dataSnapshot.getValue(Station.class);
                            if (existing != null && existing.getLocationHelper().getLatitude() == station.getLocationHelper().getLatitude() && existing.getLocationHelper().getLongitude() == station.getLocationHelper().getLongitude()) {
                                Toast.makeText(AddStation.this, getString(R.string.add_station_exists), Toast.LENGTH_SHORT).show();
                                exists = true;
                                return;
                            }
                        }
                        if (!exists) {
                            mDatabase.child("stations").child(random)
                                    .setValue(station).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(AddStation.this, getString(R.string.add_station_success), Toast.LENGTH_SHORT).show();
                                    if (imageUri != null) {
                                        uploadPicture(random);
                                    }
                                } else {
                                    Toast.makeText(AddStation.this, getString(R.string.add_station_failed), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else {
                Toast.makeText(AddStation.this, getString(R.string.add_station_ports), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private boolean checkFields(String descriptionStation, String portsStation) {
        if (descriptionStation.isEmpty() || descriptionStation.length() < 2) {
            description.setError(getString(R.string.add_station_name_invalid));
            description.requestFocus();
            return false;
        }
        if ( portsStation.isEmpty() || Integer.parseInt(portsStation) > 25) {
            ports.setError(getString(R.string.add_station_ports_number_invalid));
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

                                    stationReference.child(counter.toString()).getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                FileUri fileUri = new FileUri(uri.toString());
                                                mDatabase.child("/photos/" + uuid + "/" + counter).setValue(fileUri);
                                            });

                                    pd.dismiss();
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

    @SuppressLint("NonConstantResourceId")
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


