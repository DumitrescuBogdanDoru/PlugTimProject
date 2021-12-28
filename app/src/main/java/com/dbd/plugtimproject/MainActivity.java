package com.dbd.plugtimproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText logUsername;
    private EditText logPassword;
    private Button loginBtnLog;
    private TextView forgotBtnLog;
    private TextView registerBtnLog;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logUsername = findViewById(R.id.logUsername);
        logPassword = findViewById(R.id.logPassword);

        loginBtnLog = findViewById(R.id.loginBtnLog);
        loginBtnLog.setOnClickListener(this);

        forgotBtnLog = findViewById(R.id.forgotBtnLog);
        forgotBtnLog.setOnClickListener(this);

        registerBtnLog = findViewById(R.id.registerBtnLog);
        registerBtnLog.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerBtnLog:
                startActivity(new Intent(getApplicationContext(), Register.class));
                break;
            case R.id.loginBtnLog:
                loginUser();
                break;
            case R.id.forgotBtnLog:
                break;
        }
    }

    private boolean loginUser() {
        String email = logUsername.getText().toString().trim();
        String password = logPassword.getText().toString().trim();

        if (email.isEmpty()) {
            logUsername.setError("Email is required");
            logUsername.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            logUsername.setError("Email is invalid");
            logUsername.requestFocus();
            return false;
        } else if (password.isEmpty()) {
            logPassword.setError("Password is required");
            logPassword.requestFocus();
            return false;
        } else if (password.length() < 6) {
            logPassword.setError("Password must have at least 6 characters");
            logPassword.requestFocus();
            return false;
        }

        mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainMenu.class));
                        } else {
                            Toast.makeText(MainActivity.this, "Login failed. Please try again", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        return true;
    }
}