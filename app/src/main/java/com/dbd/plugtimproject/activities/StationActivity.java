package com.dbd.plugtimproject.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dbd.plugtimproject.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class StationActivity extends AppCompatActivity {
    private DatabaseReference mDatabase;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    private List<Bitmap> imageList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        String uuid =  getIntent().getStringExtra("uuid");
        imageList = getPhotos(uuid);

        for (Bitmap bitmap : imageList) {

        }
    }

    private List<Bitmap> getPhotos(String uuid) {
        StorageReference pathReference = storageReference.child("images/stations/" + uuid);
        List<Bitmap> imageList = new ArrayList<>();

        pathReference.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(@NonNull ListResult listResult) {
                        for (int i = 0; i < listResult.getPrefixes().size(); i++) {
                            Bitmap bitmap = getPhoto(pathReference, uuid, i);
                            imageList.add(bitmap);
                        }
                    }
                });

        return imageList;
    }

    private Bitmap getPhoto(StorageReference pathReference, String uuid, int i) {
        long MAXBYTES = 1024 * 1024 * 20;
        final Bitmap[] bitmap = new Bitmap[1];

        pathReference.child(uuid + "/" + i).getBytes(MAXBYTES).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(@NonNull byte[] bytes) {
                bitmap[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            }
        });

        return bitmap[0];
    }
}