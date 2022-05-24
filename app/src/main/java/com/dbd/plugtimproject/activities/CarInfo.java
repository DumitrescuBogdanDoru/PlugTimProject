package com.dbd.plugtimproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.Car;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CarInfo extends AppCompatActivity {

    private EditText carCompanyInfo, carModelInfo, carColorInfo, carYearInfo;
    String company, model, color, year;
    Button changeCarInfoBtn;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);

        carCompanyInfo = findViewById(R.id.carCompanyInfo);
        carModelInfo = findViewById(R.id.carModelInfo);
        carColorInfo = findViewById(R.id.carColorInfo);
        carYearInfo = findViewById(R.id.carYearInfo);

        changeCarInfoBtn = findViewById(R.id.changeCarInfoBtn);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cars");

        String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getInfo(uuid);

        changeCarInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveChanges(uuid)) {
                    finish();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
            }
        });
    }

    private void getInfo(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Car car = snapshot.getValue(Car.class);

                if (car != null) {
                    company = car.getCompany();
                    carCompanyInfo.setText(company);
                    model = car.getModel();
                    carModelInfo.setText(model);
                    color = car.getColor();
                    carColorInfo.setText(color);
                    year = car.getYear().toString();
                    carYearInfo.setText(year);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CarInfo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean saveChanges(String uuid) {
        String changedCarCompany = carCompanyInfo.getText().toString();
        String changedCarModel = carModelInfo.getText().toString();
        String changedCarColor = carColorInfo.getText().toString();
        String changedCarYear = carYearInfo.getText().toString();

        Map<String, Object> update = new HashMap<>();

        if (!changedCarCompany.equals(company)) {
            if (!changedCarCompany.isEmpty()) {
                update.put("company", changedCarCompany);
            } else {
                carCompanyInfo.setError("Company name is invalid");
                carCompanyInfo.requestFocus();
                return false;
            }
        }

        if (!changedCarModel.equals(model)) {
            if (!changedCarModel.isEmpty()) {
                update.put("model", changedCarModel);
            } else {
                carModelInfo.setError("Model name is invalid");
                carModelInfo.requestFocus();
                return false;
            }
        }


        if (!changedCarColor.equals(color)) {
            if (!changedCarColor.isEmpty()) {
                update.put("color", changedCarColor);
            } else {
                carColorInfo.setError("Color is invalid");
                carColorInfo.requestFocus();
                return false;
            }
        }


        if (!changedCarYear.equals(year)) {
            if (!changedCarCompany.isEmpty() && (Integer.parseInt(changedCarYear) > 1885 && Integer.parseInt(changedCarYear) <= Calendar.getInstance().get(Calendar.YEAR))) {
                update.put("year", Integer.parseInt(changedCarYear));
            } else {
                carYearInfo.setError("Year is invalid");
                carYearInfo.requestFocus();
                return false;
            }
        }

        mDatabase.child(uuid).updateChildren(update);
        return true;
    }
}