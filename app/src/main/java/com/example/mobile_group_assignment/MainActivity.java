package com.example.mobile_group_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FirebaseAuth mAuth;

    Button placesListButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();
                    FirebaseUser currentUser = mAuth.getCurrentUser();

                    if (itemId == R.id.nav_home) {
                        return true;
                    } else if (itemId == R.id.nav_create_plan) {
                        startActivity(new Intent(MainActivity.this, SeeTravelPlanActivity.class));
                        return true;
                    } else if (itemId == R.id.nav_create_place) {
                        if (currentUser != null) {
                            startActivity(new Intent(MainActivity.this, PlacesListActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, "Please login to access this feature", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    } else if (itemId == R.id.nav_profile) {
                        startActivity(new Intent(MainActivity.this, ProfileActivity.class));
                        return true;
                    }
                    return false;
                }
            };
}
