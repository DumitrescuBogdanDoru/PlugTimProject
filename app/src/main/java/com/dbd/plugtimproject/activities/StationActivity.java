package com.dbd.plugtimproject.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.adapters.PhotoAdapter;
import com.dbd.plugtimproject.models.FileUri;
import com.dbd.plugtimproject.models.LocationHelper;
import com.dbd.plugtimproject.models.Notification;
import com.dbd.plugtimproject.models.Station;
import com.dbd.plugtimproject.models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class StationActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private StorageReference storageReference;

    private PhotoAdapter adapter;
    private TextView name, ports, portTypes, address, addedByStation;

    // like
    private TextView noOfLikes;
    private ImageView like;

    private ArrayList<FileUri> photoList;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String uuid = getIntent().getStringExtra("uuid");

        photoList = new ArrayList<>();

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PhotoAdapter(photoList, this);
        recyclerView.setAdapter(adapter);

        Button addPhotosToStationBtn = findViewById(R.id.addPhotosToStationBtn);
        addPhotosToStationBtn.setOnClickListener(this);
        Button choosePhotoBtn = findViewById(R.id.choosePhotoBtn);
        choosePhotoBtn.setOnClickListener(this);

        name = findViewById(R.id.nameStation);
        ports = findViewById(R.id.portsStation);
        portTypes = findViewById(R.id.portTypesStation);
        address = findViewById(R.id.addressStation);
        addedByStation = findViewById(R.id.addedByStation);

        noOfLikes = findViewById(R.id.noOfLikes);
        like = findViewById(R.id.likeIcon);
        like.setOnClickListener(this);

        Button getDirectionsBtn = findViewById(R.id.getDirectionsStationBtn);
        getDirectionsBtn.setOnClickListener(this);

        getInfo(uuid);
        getPhotos(uuid);
        getLikes(uuid);
        nrOfLikes(uuid);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choosePhotoBtn:
                choosePicture();
                break;
            case R.id.addPhotosToStationBtn:
                addPhoto();
                break;
            case R.id.getDirectionsStationBtn:
                getDirections();
                break;
            case R.id.likeIcon:
                likeStation();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
        }
    }

    private void getInfo(String uuid) {
        mDatabase.child("stations/" + uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Station station = snapshot.getValue(Station.class);

                if (station != null) {
                    try {
                        name.setText(station.getDescription());
                        ports.setText("Number of Ports: " + station.getNumberOfPorts());
                        portTypes.setText("Port Types: " + getPortTypes(station));
                        address.setText("Address: " + getAddress(station.getLocationHelper()));
                        getUser(station.getAddedBy());
                    } catch (IOException e) {
                        e.printStackTrace();
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
            types.add("Type1");
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

    private String getAddress(LocationHelper locationHelper) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(locationHelper.getLatitude(), locationHelper.getLongitude(), 1);

        return addresses.get(0).getAddressLine(0);
    }

    private void getPhotos(String uuid) {
        mDatabase.child("/photos/" + uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    FileUri fileUri = dataSnapshot.getValue(FileUri.class);
                    photoList.add(fileUri);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUser(String uuid) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.child("/users/" + uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    if (snapshot.getKey().equals(firebaseUser.getUid())) {
                        addedByStation.setText("Added by me");
                    } else {
                        User user = snapshot.getValue(User.class);
                        addedByStation.setText("Added by " + user.getFirstName() + " " + user.getLastName());
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getLikes(String uuid) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        mDatabase.child("likes/" + uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(firebaseUser.getUid()).exists() && snapshot.child(firebaseUser.getUid()).getValue(Boolean.class)) {
                    like.setImageResource(R.drawable.ic_like);
                    like.setTag("like");
                } else {
                    like.setImageResource(R.drawable.ic_unlike);
                    like.setTag("unlike");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void nrOfLikes(String uuid) {
        mDatabase.child("likes/" + uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer counter = 0;

                for (DataSnapshot data : snapshot.getChildren()) {
                    if (data.exists() && data.getValue(Boolean.class)) {
                        counter++;
                    }
                }

                noOfLikes.setText(counter + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void likeStation() {
        String uuid = getIntent().getStringExtra("uuid");
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (like.getTag().equals("unlike")) {
            mDatabase.child("likes/" + uuid)
                    .child(firebaseUser.getUid()).setValue(true);
            sendNotification(FirebaseAuth.getInstance().getCurrentUser().getUid(), uuid, false);

        } else if (like.getTag().equals("like")) {
            mDatabase.child("likes/" + uuid)
                    .child(firebaseUser.getUid()).setValue(false);
        }
    }

    private void choosePicture() {
        // intent to open gallery from phone
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, 1);
    }

    private void addPhoto() {
        String uuid = getIntent().getStringExtra("uuid");

        final ProgressDialog pd = new ProgressDialog(StationActivity.this);
        pd.setTitle("Uploading image");
        pd.show();

        StorageReference stationReference = storageReference.child("images/stations/" + uuid + "/");
        stationReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    Integer counter;

                    @Override
                    public void onSuccess(@NonNull ListResult listResult) {
                        counter = listResult.getItems().size();

                        stationReference.child(counter.toString()).putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {

                                    stationReference.child(counter.toString()).getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                FileUri fileUri = new FileUri(uri.toString());
                                                mDatabase.child("/photos/" + uuid + "/" + counter).setValue(fileUri);
                                                sendNotification(FirebaseAuth.getInstance().getCurrentUser().getUid(), uuid, true);
                                            });

                                    pd.dismiss();
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    pd.dismiss();
                                    Toast.makeText(StationActivity.this, "Couldn't load image", Toast.LENGTH_SHORT).show();
                                })
                                .addOnProgressListener(snapshot -> {
                                    double progress = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                                    pd.setMessage("Progress: " + (int) progress + "%");
                                });
                    }
                });
    }

    private void sendNotification(String userId, String stationId, boolean isPhotoAdded) {

        if (isPhotoAdded) {
            mDatabase.child("/users/" + userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    String text = user.getFirstName() + " " + user.getLastName() + " added a photo to the station";

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
        } else {
            mDatabase.child("/users/" + userId).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    String text = user.getFirstName() + " " + user.getLastName() + " appreciates this station";

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

    private void getDirections() {
        String uuid = getIntent().getStringExtra("uuid");

        mDatabase.child("stations/" + uuid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Station station = snapshot.getValue(Station.class);

                if (station != null) {
                    Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse(String.format("http://maps.google.com/maps?daddr=%s,%s", station.getLocationHelper().getLatitude(), station.getLocationHelper().getLongitude())));
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}