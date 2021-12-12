package com.dbd.plugtimproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class RegisterCar extends AppCompatActivity implements View.OnClickListener {

    private TextView regCarSkipBtn;
    private Button regCarFinishBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_car);

        regCarSkipBtn = (TextView) findViewById(R.id.regCarSkipBtn);
        regCarSkipBtn.setOnClickListener(this);
        regCarFinishBtn = (Button) findViewById(R.id.regCarFinishBtn);
        regCarFinishBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regCarSkipBtn:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
            case R.id.regCarFinishBtn:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                break;
        }
    }
}