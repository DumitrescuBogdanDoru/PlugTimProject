package com.dbd.plugtimproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText logUsername;
    private EditText logPassword;
    private Button loginBtnLog;
    private TextView forgotBtnLog;
    private TextView registerBtnLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logUsername = findViewById(R.id.logUsername);
        logPassword = findViewById(R.id.logPassword);
        loginBtnLog = findViewById(R.id.loginBtnLog);
        forgotBtnLog = findViewById(R.id.forgotBtnLog);
        registerBtnLog = findViewById(R.id.registerBtnLog);

        registerBtnLog.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.registerBtnLog:
                startActivity(new Intent(getApplicationContext(), Register.class));
                break;
        }
    }
}