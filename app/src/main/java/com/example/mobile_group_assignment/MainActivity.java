package com.example.mobile_group_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private boolean isAdmin = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        // Check admin status when activity starts
        checkAdminStatus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check admin status whenever activity comes to foreground
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            isAdmin = false;
            updateMenuVisibility();
            return;
        }

        // Check Firestore for user's admin status
        db.collection("Users").document(currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            String isUser = document.getString("isUser");
                            isAdmin = !"1".equals(isUser);
                        } else {
                            isAdmin = false;
                        }
                    } else {
                        isAdmin = false;
                        Toast.makeText(MainActivity.this, "Error checking user status", Toast.LENGTH_SHORT).show();
                    }
                    updateMenuVisibility();
                });

    }

    private void updateMenuVisibility() {
        Menu menu = bottomNavigationView.getMenu();
        MenuItem createPlanItem = menu.findItem(R.id.nav_create_plan);
        createPlanItem.setVisible(isAdmin);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    int itemId = item.getItemId();

                    if (itemId == R.id.nav_home) {
                        // Already on home
                        return true;
                    } else if (itemId == R.id.nav_create_plan) {
                        // Double check admin status before proceeding
                        if (isAdmin) {
                            startActivity(new Intent(MainActivity.this, TravelAgencyActivity.class));
                        } else {
                            Toast.makeText(MainActivity.this, "Admin access required", Toast.LENGTH_SHORT).show();
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