package com.dbd.plugtimproject.activities;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.Car;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class CarInfo extends AppCompatActivity {

    private TextView companyInfo, modelInfo, colorInfo, yearInfo;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);

        companyInfo = findViewById(R.id.companyInfo);
        modelInfo = findViewById(R.id.modelInfo);
        colorInfo = findViewById(R.id.colorInfo);
        yearInfo = findViewById(R.id.yearInfo);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cars");

        String uuid = getIntent().getStringExtra("uuid");
        getInfo(uuid);
    }

    private void getInfo(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Car car = snapshot.getValue(Car.class);

                if (car != null) {
                    String company = car.getCompany();
                    companyInfo.setText(company);
                    String model = car.getModel();
                    modelInfo.setText(model);
                    String color = car.getColor();
                    colorInfo.setText(color);
                    String year = car.getYear().toString();
                    yearInfo.setText(year);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CarInfo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}