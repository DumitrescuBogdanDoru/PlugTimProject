package com.dbd.plugtimproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import java.util.Objects;

public class CarInfoActivity extends AppCompatActivity {

    private static final String TAG = "CarInfoActivity";

    private EditText carCompanyInfo, carModelInfo, carColorInfo, carYearInfo;
    String company, model, color, year;
    Button changeCarInfoBtn;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_info);

        carCompanyInfo = findViewById(R.id.car_info_company);
        carModelInfo = findViewById(R.id.car_info_model);
        carColorInfo = findViewById(R.id.car_info_color);
        carYearInfo = findViewById(R.id.car_info_year);

        changeCarInfoBtn = findViewById(R.id.car_info_save_btn);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("cars");

        String uuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        getInfo(uuid);

        changeCarInfoBtn.setOnClickListener(v -> {
            if (saveChanges(uuid)) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
            }
        });
    }

    private void getInfo(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Car car = snapshot.getValue(Car.class);

                if (car != null) {
                    Log.d(TAG, "Getting data for the car from Firebase");
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
                Toast.makeText(CarInfoActivity.this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
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
            if (changedCarCompany.length() > 1) {
                Log.d(TAG, String.format("Car company updated for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                update.put("company", changedCarCompany);
            } else {
                Log.e(TAG, String.format("Car company: %s is invalid for user %s", changedCarCompany, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                carCompanyInfo.setError(getString(R.string.car_info_company_required));
                carCompanyInfo.requestFocus();
                return false;
            }
        }

        if (!changedCarModel.equals(model)) {
            if (changedCarModel.length() > 1) {
                Log.d(TAG, String.format("Car model updated for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                update.put("model", changedCarModel);
            } else {
                Log.e(TAG, String.format("Car model: %s is invalid for user %s", changedCarModel, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                carModelInfo.setError(getString(R.string.car_info_model_required));
                carModelInfo.requestFocus();
                return false;
            }
        }


        if (!changedCarColor.equals(color)) {
            if (changedCarColor.length() > 1) {
                Log.d(TAG, String.format("Car color updated for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                update.put("color", changedCarColor);
            } else {
                Log.e(TAG, String.format("Car color: %s is invalid for user %s", changedCarColor, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                carColorInfo.setError(getString(R.string.car_info_color_required));
                carColorInfo.requestFocus();
                return false;
            }
        }

        if (!changedCarYear.equals(year)) {
            if (!changedCarYear.isEmpty() && (Integer.parseInt(changedCarYear) > 1997 && Integer.parseInt(changedCarYear) <= Calendar.getInstance().get(Calendar.YEAR))) {
                Log.d(TAG, String.format("Car year updated for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                update.put("year", Integer.parseInt(changedCarYear));
            } else {
                Log.e(TAG, String.format("Car year: %s is invalid for user %s", changedCarYear, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                carYearInfo.setError(getString(R.string.car_info_year_required));
                carYearInfo.requestFocus();
                return false;
            }
        }

        if (!update.isEmpty()) {
            mDatabase.child(uuid).updateChildren(update);
            Toast.makeText(CarInfoActivity.this, getString(R.string.save_changes_message), Toast.LENGTH_SHORT).show();
            Log.d(TAG, String.format("Updated data in firebase for car's user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
        }

        return true;
    }
}