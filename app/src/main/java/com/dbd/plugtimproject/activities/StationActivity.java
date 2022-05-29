package com.dbd.plugtimproject.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.FileUri;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class StationActivity extends AppCompatActivity implements View.OnClickListener {

    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private RecyclerView recyclerView;
    private PhotoAdapter adapter;
    private Button addPhotosToStationBtn;
    private Button choosePhotoBtn;

    private ArrayList<FileUri> photoList;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String uuid = getIntent().getStringExtra("uuid");

        photoList = new ArrayList<>();

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new PhotoAdapter(photoList, this);
        recyclerView.setAdapter(adapter);

        addPhotosToStationBtn = findViewById(R.id.addPhotosToStationBtn);
        addPhotosToStationBtn.setOnClickListener(this);
        choosePhotoBtn = findViewById(R.id.choosePhotoBtn);
        choosePhotoBtn.setOnClickListener(this);

        getPhotos(uuid);

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.choosePhotoBtn:
                choosePicture();
                break;
            case R.id.addPhotosToStationBtn:
                addPhoto();
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

    private boolean choosePicture() {
        // intent to open gallery from phone
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        startActivityForResult(intent, 1);
        return true;
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
                    public void onSuccess(ListResult listResult) {
                        counter = listResult.getItems().size();

                        stationReference.child(counter.toString()).putFile(imageUri)
                                .addOnSuccessListener(taskSnapshot -> {

                                    stationReference.child(counter.toString()).getDownloadUrl()
                                            .addOnSuccessListener(uri -> {
                                                FileUri fileUri = new FileUri(uri.toString());
                                                mDatabase.child("/photos/" + uuid + "/" + counter).setValue(fileUri);
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
}