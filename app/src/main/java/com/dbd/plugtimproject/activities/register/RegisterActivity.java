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
import com.dbd.plugtimproject.activities.MainActivity;
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
    private DatabaseReference mReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Button nxtBtnReg = findViewById(R.id.register_next_btn);
        nxtBtnReg.setOnClickListener(this);

        regUsername = findViewById(R.id.register_username);
        regPassword = findViewById(R.id.register_password);
        regFirstName = findViewById(R.id.register_first_name);
        regLastName = findViewById(R.id.register_last_name);

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/");
        mReference = mDatabase.getReference();

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && currentUser.isEmailVerified()) {
            startActivity(new Intent(this, MainActivity.class));
        } else {
            mAuth.signOut();
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.register_next_btn) {
            if (registerUser()) {
                startActivity(new Intent(getApplicationContext(), RegisterCarActivity.class));
            } else {
                Log.d(TAG, "Error occurred during user registry");
            }
        }
    }

    private boolean registerUser() {
        String email = regUsername.getText().toString().replace(" ", "");
        String password = regPassword.getText().toString().replace(" ", "");
        String firstName = regFirstName.getText().toString().replace(" ", "");
        String lastName = regLastName.getText().toString().replace(" ", "");

        if (email.isEmpty()) {
            Log.d(TAG, "No email was added");
            regUsername.setError(getString(R.string.email_required_message));
            regUsername.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "Invalid email");
            regUsername.setError(getString(R.string.email_invalid_message));
            regUsername.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            Log.d(TAG, "No password was added");
            regPassword.setError(getString(R.string.password_required_message));
            regPassword.requestFocus();
            return false;
        } else if (password.length() < 6) {
            Log.d(TAG, "Invalid password");
            regPassword.setError(getString(R.string.password_invalid_message));
            regPassword.requestFocus();
            return false;
        } else if (firstName.length() < 2) {
            Log.d(TAG, "No first name was added");
            regFirstName.setError(getString(R.string.register_first_name_message));
            regFirstName.requestFocus();
            return false;
        } else if (lastName.length() < 2) {
            Log.d(TAG, "No last name was added");
            regLastName.setError(getString(R.string.register_last_name_message));
            regLastName.requestFocus();
            return false;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {
                            firebaseUser.sendEmailVerification();
                            Log.d(TAG, String.format("An email was sent to user %s at %s", firebaseUser.getUid(), new Date()));
                            Toast.makeText(RegisterActivity.this, getString(R.string.register_send_email), Toast.LENGTH_SHORT).show();

                            User user = new User(email, firstName, lastName);
                            mReference.child("users").child(Objects.requireNonNull(mAuth.getCurrentUser()).getUid())
                                    .setValue(user).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    Log.d(TAG, String.format("User %s added successfully to database at %s", firebaseUser.getUid(), new Date()));
                                } else {
                                    Log.d(TAG, String.format("Registration failed for user %s at %s",
                                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), new Date()));
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, Objects.requireNonNull(task.getException()).getMessage());
                        Toast.makeText(RegisterActivity.this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
                    }
                });
        return true;
    }
}