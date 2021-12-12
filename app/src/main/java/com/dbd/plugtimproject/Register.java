package com.dbd.plugtimproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Register extends AppCompatActivity implements View.OnClickListener{

    private Button nxtBtnReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nxtBtnReg = (Button) findViewById(R.id.nxtBntReg);
        nxtBtnReg.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nxtBntReg:
                startActivity(new Intent(getApplicationContext(), RegisterCar.class));
                break;
        }
    }
}