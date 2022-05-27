package com.dbd.plugtimproject.activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.dbd.plugtimproject.R;

public class StationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_station);


        String uuid =  getIntent().getStringExtra("uuid");
    }
}