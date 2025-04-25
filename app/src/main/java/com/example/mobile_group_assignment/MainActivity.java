package com.example.mobile_group_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {

    Button buttonSeePlans;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSeePlans = findViewById(R.id.buttonSeePlans);

        buttonSeePlans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Navigate to SeeTravelPlanActivity
                Intent intent = new Intent(MainActivity.this, SeeTravelPlanActivity.class);
                startActivity(intent);
            }
        });
    }
}