package com.example.mobile_group_assignment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private LinearLayout authContainer;
    private LinearLayout profileContainer;
    private TabLayout authTabs;
    private TextInputEditText etEmail, etPassword, etConfirmPassword;
    private TextInputLayout tilConfirmPassword;
    private MaterialButton btnAuthenticate;
    private TextView tvUserName, tvUserEmail;
    private ProgressBar progressBar;
    private View logoutOption, viewPlansOption, viewPlacesOption;

    // Firebase Auth
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();


        // Initialize UI components
        initializeUI();

        // Bottom Navigation Bar setup
        setupNavigation();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly
        updateUI(mAuth.getCurrentUser());
    }

    private void initializeUI() {
        // Authentication components
        authContainer = findViewById(R.id.authContainer);
        profileContainer = findViewById(R.id.profileContainer);
        authTabs = findViewById(R.id.authTabs);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        btnAuthenticate = findViewById(R.id.btnAuthenticate);
        progressBar = findViewById(R.id.progressBar);

        // Profile components
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        logoutOption = findViewById(R.id.logoutOption);
        viewPlansOption = findViewById(R.id.viewPlans);
        viewPlacesOption = findViewById(R.id.viewPlaces);

        // Set up tab selection listener
        authTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) { // Login
                    btnAuthenticate.setText("LOGIN");
                    tilConfirmPassword.setVisibility(View.GONE);
                } else { // Register
                    btnAuthenticate.setText("REGISTER");
                    tilConfirmPassword.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Set up authentication button click listener
        btnAuthenticate.setOnClickListener(v -> {
            int selectedTab = authTabs.getSelectedTabPosition();
            if (selectedTab == 0) {
                loginUser();
            } else {
                registerUser();
            }
        });


        // Set up Logout option click listener
        logoutOption.setOnClickListener(v -> {
            new AlertDialog.Builder(this) // replace 'context' with your Activity or use 'this' if inside an Activity
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to sign out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        signOut(); // Call your sign out method
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        dialog.dismiss(); // Dismiss the dialog if user cancels
                    })
                    .show();
        });

        // Set up View Plans option click listener
        viewPlansOption.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SeeTravelPlanActivity.class);
            startActivity(intent);
        });

        // Set up View Places option click listener
        viewPlacesOption.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, PlacesListActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Sign in with email and password
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Toast.makeText(ProfileActivity.this, "Login successful!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(ProfileActivity.this, "Authentication failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long");
            etPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Show progress
        progressBar.setVisibility(View.VISIBLE);

        // Create user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                            Toast.makeText(ProfileActivity.this, "Registration successful!",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // If sign in fails, display a message to the user
                            Toast.makeText(ProfileActivity.this, "Registration failed: " +
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();
        updateUI(null);
    }

    private void updateUI(FirebaseUser user) {
        progressBar.setVisibility(View.GONE);

        if (user != null) {
            // User is signed in
            authContainer.setVisibility(View.GONE);
            profileContainer.setVisibility(View.VISIBLE);

            // Update profile information
            String displayName = user.getDisplayName();
            if (TextUtils.isEmpty(displayName)) {
                displayName = "Traveler";
            }
            tvUserName.setText(displayName);
            tvUserEmail.setText(user.getEmail());
        } else {
            // User is signed out
            authContainer.setVisibility(View.VISIBLE);
            profileContainer.setVisibility(View.GONE);

            // Clear form fields
            etEmail.setText("");
            etPassword.setText("");
            etConfirmPassword.setText("");
        }
    }

    private void setupNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.nav_create_place) {
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    startActivity(new Intent(this, PlacesListActivity.class));
                } else {
                    Toast.makeText(this, "Please login to access Create Place", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                return true;
            } else if (itemId == R.id.nav_create_plan) {
                startActivity(new Intent(this, CreatePlanActivity.class));
                return true;
            }
            return false;
        });
    }

}