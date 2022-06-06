package com.dbd.plugtimproject.activities.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Date;
import java.util.Objects;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "RegisterActivity";

    private EditText regUsername, regPassword, regFirstName, regLastName;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

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
                startActivity(new Intent(this, RegisterCarActivity.class));
            } else {
                Log.d(TAG, "Error occurred during user registry");
                Toast.makeText(RegisterActivity.this, getString(R.string.register_error_message), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean registerUser() {
        String email = regUsername.getText().toString();
        String password = regPassword.getText().toString();
        String firstName = regFirstName.getText().toString();
        String lastName = regLastName.getText().toString();

        if (email.isEmpty()) {
            Log.d(TAG, "No email was added");
            regUsername.setError(getString(R.string.email_required_message));
            regUsername.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()) {
            Log.d(TAG, "Invalid email");
            regUsername.setError(getString(R.string.email_invalid_message));
            regUsername.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            Log.d(TAG, "No password was added");
            regPassword.setError(getString(R.string.email_required_message));
            regPassword.requestFocus();
            return false;
        } else if (password.length() < 6) {
            Log.d(TAG, "Invalid password");
            regPassword.setError(getString(R.string.email_required_message));
            regPassword.requestFocus();
            return false;
        } else if (firstName.isEmpty()) {
            Log.d(TAG, "No first name was added");
            regFirstName.setError(getString(R.string.register_first_name_message));
            regFirstName.requestFocus();
            return false;
        } else if (lastName.isEmpty()) {
            Log.d(TAG, "No last name was added");
            regLastName.setError(getString(R.string.register_last_name_message));
            regLastName.requestFocus();
            return false;
        }

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        User user = new User(email, firstName, lastName);
                        mDatabase.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                .setValue(user).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                                if (firebaseUser != null) {
                                    firebaseUser.sendEmailVerification();
                                    Log.d(TAG, String.format("User %s added successfully to database at %s", firebaseUser.getUid(), new Date()));
                                    Toast.makeText(RegisterActivity.this, getString(R.string.register_send_email), Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Log.d(TAG, "Registration failed");
                                Toast.makeText(RegisterActivity.this, String.format("Failed to register user %s at %s. Please try again", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), new Date()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
        return true;
    }
}