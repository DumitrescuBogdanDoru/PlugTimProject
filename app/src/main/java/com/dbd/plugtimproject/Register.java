package com.dbd.plugtimproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity implements View.OnClickListener {

    private Button nxtBtnReg;
    private EditText regUsername, regPassword, regFirstName, regLastName;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nxtBtnReg = findViewById(R.id.nxtBntReg);
        nxtBtnReg.setOnClickListener(this);

        regUsername = findViewById(R.id.regUsername);
        regPassword = findViewById(R.id.regPassword);
        regFirstName = findViewById(R.id.regFirstName);
        regLastName = findViewById(R.id.regLastName);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nxtBntReg:
                if (registerUser()) {
                    startActivity(new Intent(getApplicationContext(), RegisterCar.class));
                } else {
                    Toast.makeText(Register.this, "ERROR", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private boolean registerUser() {
        String username = regUsername.getText().toString();
        String password = regPassword.getText().toString();
        String firstName = regFirstName.getText().toString();
        String lastName = regLastName.getText().toString();
        boolean flag = true;

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

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mAuth.createUserWithEmailAndPassword(username, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user = new User(username, firstName, lastName);
                            mDatabase.child("users").child(mAuth.getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Register.this, "User has been registered successfully", Toast.LENGTH_LONG).show();
                                    } else {
                                        Toast.makeText(Register.this, "Failed to register. Try again", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                    }
                });
        return true;
    }
}