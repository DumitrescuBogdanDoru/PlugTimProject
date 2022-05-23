package com.dbd.plugtimproject.activities;

import android.os.Bundle;
import android.widget.TextView;
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

public class ProfileInfo extends AppCompatActivity {

    private TextView emailInfo, firstNameInfo, lastNameInfo;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_info);

        emailInfo = findViewById(R.id.companyInfo);
        firstNameInfo = findViewById(R.id.modelInfo);
        lastNameInfo = findViewById(R.id.colorInfo);

        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        getInfo(uuid);
    }

    private void getInfo(String userId) {
        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user != null) {
                    String email = user.getUsername();
                    emailInfo.setText(email);
                    String firstName = user.getFirstName();
                    firstNameInfo.setText(firstName);
                    String lastName = user.getLastName();
                    lastNameInfo.setText(lastName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ProfileInfo.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }
}