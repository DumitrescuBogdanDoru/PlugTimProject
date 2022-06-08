package com.dbd.plugtimproject.activities.register;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

/**
 * Added by: Bogdan Dumitrescu
 * Date: 28/12/2021
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText forgotEmail;
    private FirebaseAuth mAuth;

    private static final String TAG = "ForgotPasswordActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        forgotEmail = findViewById(R.id.forUsername);
        Button resetBtnFor = findViewById(R.id.resetBtnFor);

        mAuth = FirebaseAuth.getInstance();
        resetBtnFor.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = forgotEmail.getText().toString().trim();

        // Email validation
        if (email.isEmpty()) {
            Log.d(TAG, "No email was added");
            forgotEmail.setError(getString(R.string.email_required_message));
            forgotEmail.requestFocus();
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Log.d(TAG, "Invalid email");
            forgotEmail.setError(getString(R.string.email_invalid_message));
            forgotEmail.requestFocus();
            return;
        }

        // Sending email to reset the password
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, String.format("Reset password email was sent to the user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                Toast.makeText(this, getString(R.string.forgot_password_email_sent), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Log.d(TAG, String.format("Failed to reset password for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                Toast.makeText(this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
            }
        });

    }
}