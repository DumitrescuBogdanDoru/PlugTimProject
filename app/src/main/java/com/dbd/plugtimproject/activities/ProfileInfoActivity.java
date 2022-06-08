package com.dbd.plugtimproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ProfileInfoActivity extends AppCompatActivity {

    private static final String TAG = "ProfileInfoActivity";

    private EditText profileEmailInfo, profileFirstNameInfo, profileLastNameInfo;
    private String email, firstName, lastName;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        profileEmailInfo = findViewById(R.id.profile_info_email);
        profileFirstNameInfo = findViewById(R.id.profile_info_first_name);
        profileLastNameInfo = findViewById(R.id.profile_info_last_name);
        Button changeProfileInfoBtn = findViewById(R.id.profile_info_save_btn);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        String uuid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
        getInfo(uuid);

        changeProfileInfoBtn.setOnClickListener(v -> {
            if (saveChanges(uuid)) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });
    }

    private void getInfo(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user != null) {
                    Log.d(TAG, "Getting data for the user from Firebase");
                    email = user.getUsername();
                    profileEmailInfo.setText(email);
                    firstName = user.getFirstName();
                    profileFirstNameInfo.setText(firstName);
                    lastName = user.getLastName();
                    profileLastNameInfo.setText(lastName);
                } else {
                    Log.e(TAG, String.format("Couldn't get data for the user %s", snapshot.getKey()));
                    Toast.makeText(ProfileInfoActivity.this, getString(R.string.user_not_found), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileInfoActivity.this, getString(R.string.general_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean saveChanges(String uuid) {
        String changedEmail = profileEmailInfo.getText().toString();
        String changedFirstName = profileFirstNameInfo.getText().toString();
        String changedLastName = profileLastNameInfo.getText().toString();

        Map<String, Object> update = new HashMap<>();

        if (!changedEmail.equals(email)) {
            if (Patterns.EMAIL_ADDRESS.matcher(changedEmail).matches() && changedEmail.length() > 1) {
                update.put("username", changedEmail);
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                if (firebaseUser != null) {
                    Log.d(TAG, String.format("Email updated for user %s", firebaseUser.getUid()));
                    firebaseUser.updateEmail(changedEmail).addOnSuccessListener(unused -> firebaseUser.sendEmailVerification());
                }
            } else {
                Log.e(TAG, String.format("Email %s is invalid for user %s", changedEmail, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                profileEmailInfo.setError(getString(R.string.email_invalid_message));
                profileEmailInfo.requestFocus();
                return false;
            }
        }

        if (!changedFirstName.equals(firstName)) {
            if (!changedFirstName.isEmpty() && changedFirstName.length() > 1) {
                Log.d(TAG, String.format("First name updated for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                update.put("firstName", changedFirstName);
            } else {
                Log.e(TAG, String.format("First name: %s is invalid for user %s", changedFirstName, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                profileFirstNameInfo.setError(getString(R.string.register_first_name_message));
                profileFirstNameInfo.requestFocus();
                return false;
            }
        }

        if (!changedLastName.equals(lastName)) {
            if (!changedLastName.isEmpty() && changedLastName.length() > 1) {
                Log.d(TAG, String.format("Last Name updated for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                update.put("lastName", changedLastName);
            } else {
                Log.e(TAG, String.format("Last Name: %s is invalid for user %s", changedLastName, Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                profileLastNameInfo.setError(getString(R.string.register_last_name_message));
                profileLastNameInfo.requestFocus();
                return false;
            }
        }

        if (!update.isEmpty()){
            mDatabase.child(uuid).updateChildren(update)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(ProfileInfoActivity.this, getString(R.string.save_changes_message), Toast.LENGTH_SHORT).show();
                        Log.d(TAG, String.format("Updated data in firebase for user for user %s", Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid()));
                    });
        }
        return true;
    }
}