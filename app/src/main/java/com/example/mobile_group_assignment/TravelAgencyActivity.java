package com.example.mobile_group_assignment;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class TravelAgencyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_agency);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_create_plan); // Highlight profile

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(TravelAgencyActivity.this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_create_plan) {
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(TravelAgencyActivity.this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }
}