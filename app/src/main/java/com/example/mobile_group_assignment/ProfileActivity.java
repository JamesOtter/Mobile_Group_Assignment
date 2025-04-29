package com.example.mobile_group_assignment;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private TextInputEditText editEmail, editPassword;
    private Button btnLogin, btnLogout, btnGoToRegister;
    private TextView txtStatus, txtWelcome;
    private BottomNavigationView bottomNavigationView;
    private MaterialCardView loginCard, profileCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupUIElements();
        setupNavigation();
        setupButtonListeners();
        checkUserStatus();
    }

    private void initializeViews() {
        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnLogout = findViewById(R.id.btnLogout);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        txtStatus = findViewById(R.id.txtStatus);
        txtWelcome = findViewById(R.id.txtWelcome);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        loginCard = findViewById(R.id.loginCard);
        profileCard = findViewById(R.id.profileCard);
    }

    private void setupUIElements() {
        // Set blue theme colors
        int primaryColor = ContextCompat.getColor(this, R.color.blue_500);
        int primaryDarkColor = ContextCompat.getColor(this, R.color.blue_700);

        // Style the buttons
        btnLogin.setBackgroundColor(primaryColor);
        btnLogout.setBackgroundColor(primaryDarkColor);
        btnGoToRegister.setBackgroundColor(primaryDarkColor);

        // Style the cards
        loginCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blue_50));
        profileCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blue_50));

        // Style the text
        txtWelcome.setTextColor(primaryDarkColor);
        txtWelcome.setTypeface(null, Typeface.BOLD);
        txtStatus.setTextColor(primaryDarkColor);
    }

    private void setupNavigation() {
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(ProfileActivity.this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_create_place) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    startActivity(new Intent(ProfileActivity.this, TravelAgencyActivity.class));
                } else {
                    Toast.makeText(this, "Please login to access Create Place", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_create_plan) {
                startActivity(new Intent(ProfileActivity.this, CreatePlanActivity.class));
                return true;
            }
            return false;
        });
    }


    private void setupButtonListeners() {
        btnLogin.setOnClickListener(v -> loginUser());
        btnLogout.setOnClickListener(v -> logoutUser());
        btnGoToRegister.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, RegisterActivity.class));
        });
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }

    private void loginUser() {
        String email = editEmail.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            editEmail.setError("Email cannot be empty");
            editEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Password cannot be empty");
            editPassword.requestFocus();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserStatus();
                        Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void logoutUser() {
        mAuth.signOut();
        updateUI(null);
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            txtWelcome.setText("Welcome Back!");
            txtWelcome.setTextColor(ContextCompat.getColor(this, R.color.white));
            txtStatus.setText("Logged in as: " + user.getEmail());
            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnLogout.setVisibility(View.VISIBLE);
            btnLogin.setVisibility(View.GONE);
            btnGoToRegister.setVisibility(View.GONE);
            loginCard.setVisibility(View.GONE);
            profileCard.setVisibility(View.VISIBLE);
        } else {
            txtWelcome.setText("Welcome To Travel Plan!");
            txtWelcome.setTextColor(ContextCompat.getColor(this, R.color.white));
            txtStatus.setText("Please login or register");
            txtStatus.setTextColor(ContextCompat.getColor(this, R.color.white));
            btnLogout.setVisibility(View.GONE);
            btnLogin.setVisibility(View.VISIBLE);
            btnGoToRegister.setVisibility(View.VISIBLE);
            loginCard.setVisibility(View.VISIBLE);
            profileCard.setVisibility(View.GONE);
        }
    }
}