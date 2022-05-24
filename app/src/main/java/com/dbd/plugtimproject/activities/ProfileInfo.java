package com.dbd.plugtimproject.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;
import com.dbd.plugtimproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileInfo extends AppCompatActivity {

    private EditText profileEmailInfo, profileFirstNameInfo, profileLastNameInfo;
    private String email, firstName, lastName;
    private Button changeProfileInfoBtn;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        profileEmailInfo = findViewById(R.id.profileEmailInfo);
        profileFirstNameInfo = findViewById(R.id.profileFirstNameInfo);
        profileLastNameInfo = findViewById(R.id.profileLastNameInfo);
        changeProfileInfoBtn = findViewById(R.id.changeProfileInfoBtn);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getInfo(uuid);

        changeProfileInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (saveChanges(uuid)) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }

    private void getInfo(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user != null) {
                    email = user.getUsername();
                    profileEmailInfo.setText(email);
                    firstName = user.getFirstName();
                    profileFirstNameInfo.setText(firstName);
                    lastName = user.getLastName();
                    profileLastNameInfo.setText(lastName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileInfo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean saveChanges(String uuid) {
        String changedEmail = profileEmailInfo.getText().toString();
        String changedFirstName = profileFirstNameInfo.getText().toString();
        String changedLastName = profileLastNameInfo.getText().toString();

        Map<String, Object> update = new HashMap<>();

        if (!changedEmail.equals(email)) {
            if (Patterns.EMAIL_ADDRESS.matcher(changedEmail).matches() && !changedEmail.isEmpty()) {
                update.put("username", changedEmail);
                FirebaseAuth.getInstance().getCurrentUser().updateEmail(changedEmail);
            } else {
                profileEmailInfo.setError("Email is invalid");
                profileEmailInfo.requestFocus();
                return false;
            }
        }

        if (!changedFirstName.equals(firstName)) {
            if (!changedEmail.isEmpty() && changedEmail.length() > 1) {
                update.put("firstName", changedFirstName);
            } else {
                profileFirstNameInfo.setError("First name is invalid");
                profileFirstNameInfo.requestFocus();
                return false;
            }
        }

        if (!changedLastName.equals(lastName)) {
            if (!changedLastName.isEmpty() && changedLastName.length() > 1) {
                update.put("lastName", changedLastName);
            } else {
                profileLastNameInfo.setError("Last name is invalid");
            }
        }

        mDatabase.child(uuid).updateChildren(update);
        return true;
    }
}