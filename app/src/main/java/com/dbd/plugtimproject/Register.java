package com.dbd.plugtimproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText regUsername, regPassword, regFirstName, regLastName;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private String email, pass;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button nxtBtnReg = findViewById(R.id.nxtBntReg);
        nxtBtnReg.setOnClickListener(this);

        regUsername = findViewById(R.id.regUsername);
        regPassword = findViewById(R.id.regPassword);
        regFirstName = findViewById(R.id.regFirstName);
        regLastName = findViewById(R.id.regLastName);

    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.nxtBntReg) {
            if (registerUser()) {
                Intent intent = new Intent(getApplicationContext(), RegisterCar.class);
                intent.putExtra("email", email);
                startActivity(intent);
            } else {
                Toast.makeText(Register.this, "ERROR", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean registerUser() {
        String username = regUsername.getText().toString();
        String password = regPassword.getText().toString();
        String firstName = regFirstName.getText().toString();
        String lastName = regLastName.getText().toString();

        if (username.isEmpty()) {
            regUsername.setError("Email is required");
            regUsername.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
            regUsername.setError("Email is invalid");
            regUsername.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            regPassword.setError("Password is required");
            regPassword.requestFocus();
            return false;
        } else if (password.length() < 6) {
            regPassword.setError("Password must have at least 6 characters");
            regPassword.requestFocus();
            return false;
        } else if (firstName.isEmpty()) {
            regFirstName.setError("First Name is required");
            regFirstName.requestFocus();
            return false;
        } else if (lastName.isEmpty()) {
            regLastName.setError("Last Name is required");
            regLastName.requestFocus();
            return false;
        }

        // using email as link for the owner to the car
        email = username;
        pass = password;

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(username, firstName, lastName);
                        mDatabase.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                .setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                firebaseUser.sendEmailVerification();
                                Toast.makeText(Register.this, "A verification email has been sent. Please check your email", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(Register.this, "Failed to register. Please try again", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        return true;
    }
}