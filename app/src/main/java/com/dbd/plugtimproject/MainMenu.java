package com.dbd.plugtimproject;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainMenu extends AppCompatActivity implements View.OnClickListener {

    private TextView message;
    private Button logout, carInfoBtn, userInfoBtn, addStationBtn, mapsBtn;

    private DatabaseReference mDatabase;

    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        message = findViewById(R.id.message);
        logout = findViewById(R.id.logoutBtnMM);
        carInfoBtn = findViewById(R.id.carInfoBtn);
        userInfoBtn = findViewById(R.id.userInfoBtn);
        addStationBtn = findViewById(R.id.addStationBtn);
        mapsBtn = findViewById(R.id.mapsBtn);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance("https://plugtimproject-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users");

        userId = firebaseUser.getUid();

        logout.setOnClickListener(this);
        carInfoBtn.setOnClickListener(this);
        userInfoBtn.setOnClickListener(this);
        addStationBtn.setOnClickListener(this);
        mapsBtn.setOnClickListener(this);

        mDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);

                if (user != null) {
                    String firstName = user.getFirstName();
                    message.setText("Welcome " + firstName + "!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainMenu.this, "Something went wrong", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutBtnMM:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.carInfoBtn:
                Intent carIntent = new Intent(getApplicationContext(), CarInfo.class);
                carIntent.putExtra("uuid", userId);
                startActivity(carIntent);
                break;
            case R.id.userInfoBtn:
                Intent userIntent = new Intent(getApplicationContext(), ProfileInfo.class);
                userIntent.putExtra("uuid", userId);
                startActivity(userIntent);
                break;
            case R.id.addStationBtn:
                startActivity(new Intent(getApplicationContext(), AddStation.class));
                break;
            case R.id.mapsBtn:
                startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                break;
        }
    }
}