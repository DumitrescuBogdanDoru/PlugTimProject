package com.dbd.plugtimproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.models.Car;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterCar extends AppCompatActivity implements View.OnClickListener {

    private TextView regCarSkipBtn;
    private Button regCarFinishBtn;

    private EditText regCarCompany, regCarModel, regCarColor, regCarYear;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_car);

        regCarSkipBtn = findViewById(R.id.regCarSkipBtn);
        regCarSkipBtn.setOnClickListener(this);
        regCarFinishBtn = findViewById(R.id.regCarFinishBtn);
        regCarFinishBtn.setOnClickListener(this);

        regCarCompany = findViewById(R.id.regCarCompany);
        regCarModel = findViewById(R.id.regCarModel);
        regCarColor = findViewById(R.id.regCarColor);
        regCarYear = findViewById(R.id.regCarYear);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regCarSkipBtn:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.regCarFinishBtn:
                Intent intent = getIntent();
                String email = intent.getStringExtra("email");
                String password = intent.getStringExtra("password");
                if (registerCar(email, password)) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                break;
        }
    }

    private boolean registerCar(String email, String password) {
        String company = regCarCompany.getText().toString();
        String model = regCarModel.getText().toString();
        String color = regCarColor.getText().toString();
        String year = regCarYear.getText().toString();

        if (company.isEmpty()) {
            regCarCompany.setError("Company name is required");
            regCarCompany.requestFocus();
            return false;
        } else if (model.isEmpty()) {
            regCarModel.setError("Model name is required");
            regCarModel.requestFocus();
            return false;
        } else if (color.isEmpty()) {
            regCarColor.setError("Color is required");
            regCarColor.requestFocus();
            return false;
        } else if (year.isEmpty()) {
            regCarYear.setError("Year is required");
            regCarYear.requestFocus();
            return false;
        } else if (Integer.parseInt(year) < 1885 || Integer.parseInt(year) > Calendar.getInstance().get(Calendar.YEAR)) {
            regCarYear.setError("Please enter a valid year");
            regCarYear.requestFocus();
            return false;
        }

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            Car car = new Car(company, model, color, Integer.parseInt(year), email);
                            mDatabase.child("cars").child(user.getUid())
                                    .setValue(car).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Toast.makeText(RegisterCar.this, "Car has been added successfully", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RegisterCar.this, "Failed to add your car. Please try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(RegisterCar.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        return true;
    }
}