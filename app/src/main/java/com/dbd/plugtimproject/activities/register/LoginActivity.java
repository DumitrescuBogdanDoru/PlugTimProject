package com.dbd.plugtimproject.activities.register;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.activities.MainActivity;
import com.dbd.plugtimproject.managers.LanguageManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Date;
import java.util.Objects;

/**
 * Added by: Bogdan Dumitrescu
 * Date: 11/12/2021
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText logUsername;
    private EditText logPassword;

    private FirebaseAuth mAuth;
    private LanguageManager languageManager;

    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Edit Texts
        logUsername = findViewById(R.id.logUsername);
        logPassword = findViewById(R.id.logPassword);

        // Buttons
        Button loginBtnLog = findViewById(R.id.loginBtnLog);
        loginBtnLog.setOnClickListener(this);

        TextView forgotBtnLog = findViewById(R.id.forgotBtnLog);
        forgotBtnLog.setOnClickListener(this);

        TextView registerBtnLog = findViewById(R.id.registerBtnLog);
        registerBtnLog.setOnClickListener(this);

        // Language Change
        languageManager = new LanguageManager(this);
        ImageView ro = findViewById(R.id.ro_btn);
        ro.setOnClickListener(this);
        ImageView en = findViewById(R.id.en_btn);
        en.setOnClickListener(this);

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
        switch (v.getId()) {
            case R.id.registerBtnLog:
                Log.d(TAG, "Started RegisterActivity");
                startActivity(new Intent(this, RegisterActivity.class));
                break;
            case R.id.loginBtnLog:
                loginUser();
                break;
            case R.id.forgotBtnLog:
                Log.d(TAG, "Started ForgotPasswordActivity");
                startActivity(new Intent(this, ForgotPasswordActivity.class));
                break;
            case R.id.ro_btn:
                Log.d(TAG, "Changed language to romanian");
                languageManager.updateResource("ro");
                recreate();
                break;
            case R.id.en_btn:
                Log.d(TAG, "Changed language to english");
                languageManager.updateResource("en");
                recreate();
                break;
        }
    }

    private void loginUser() {
        String email = logUsername.getText().toString().replace(" ", "");
        String password = logPassword.getText().toString().replace(" ", "");

        if (email.isEmpty()) {
            Log.d(TAG, "No email was added");
            logUsername.setError(getString(R.string.email_required_message));
            logUsername.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "Invalid email");
            logUsername.setError(getString(R.string.email_invalid_message));
            logUsername.requestFocus();
            return;
        } else if (password.isEmpty()) {
            Log.d(TAG, "No password was added");
            logPassword.setError(getString(R.string.password_required_message));
            logPassword.requestFocus();
            return;
        } else if (password.length() < 6) {
            Log.d(TAG, "Invalid password");
            logPassword.setError(getString(R.string.password_invalid_message));
            logPassword.requestFocus();
            return;
        }

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                        if (user != null && user.isEmailVerified()) {
                            Log.d(TAG, String.format("User %s was logged in successfully at %s", user.getUid(), new Date()));
                            startActivity(new Intent(this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_email_verification_required), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, String.format("User %s couldn't logged in at %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid(), new Date()));
                        Toast.makeText(LoginActivity.this, getString(R.string.login_email_failed), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}