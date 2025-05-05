package com.example.mobile_group_assignment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // Places Section
    private RecyclerView placesRecyclerView;
    private HomePlacesAdapter placesAdapter;
    private PlacesFirestoreHelper placesHelper;

    // Plans Section
    private RecyclerView planRecyclerView;
    private HomeTravelPlanAdapter planAdapter;
    private List<TravelPlan> planList = new ArrayList<>();
    List<TravelPlan> filteredPlanList = new ArrayList<>();
    private static final int EDIT_REQUEST_CODE = 1001;

    // Firebase
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialization
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        initPlacesSection();
        initPlansSection();

        // Setup Bottom Navigation
        setupBottomNavigation();
    }

    private void initPlacesSection() {
        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(
                this, LinearLayoutManager.HORIZONTAL, false));

        placesHelper = new PlacesFirestoreHelper();
        try {
            FirestoreRecyclerOptions<Place> options = placesHelper.getAllPlacesOptions();
            placesAdapter = new HomePlacesAdapter(options, this);
            placesRecyclerView.setAdapter(placesAdapter);
            placesAdapter.startListening();
        } catch (Exception e) {
            Log.e("MainActivity", "Error setting up places", e);
            Toast.makeText(this, "Error loading places", Toast.LENGTH_SHORT).show();
        }
    }

    private void initPlansSection() {
        planRecyclerView = findViewById(R.id.planRecyclerView);
        planRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        planAdapter = new HomeTravelPlanAdapter(planList, this);

        planAdapter.setOnItemDeleteListener(position -> {
            showDeleteConfirmationDialog(position);
        });

        planAdapter.setOnItemEditListener(position -> {
            editPlan(position);
        });

        planRecyclerView.setAdapter(planAdapter);
        loadTravelPlans();
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Plan")
                .setMessage("Are you sure?")
                .setPositiveButton("Delete", (dialog, which) -> deletePlan(position))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePlan(int position) {
        planList.remove(position);
        planAdapter.notifyItemRemoved(position);
        savePlans();
    }

    private void editPlan(int position) {
        TravelPlan plan = planList.get(position);
        Intent intent = new Intent(this, ManageTravelPlanActivity.class);
        intent.putExtra("editMode", true);
        intent.putExtra("position", position);
        intent.putExtra("selectedState", plan.getDestination());
        // ... (pass all other plan data)
        startActivityForResult(intent, EDIT_REQUEST_CODE);
    }

    private void savePlans() {
        SharedPreferences.Editor editor = getSharedPreferences("TravelPlans", MODE_PRIVATE).edit();
        editor.putString("plans_list", new Gson().toJson(planList));
        editor.apply();
    }

    private void loadTravelPlans() {
        SharedPreferences sharedPreferences = getSharedPreferences("TravelPlans", MODE_PRIVATE);
        String plansJson = sharedPreferences.getString("plans_list", "[]");

        try {
            Type listType = new TypeToken<List<TravelPlan>>(){}.getType();
            List<TravelPlan> travelPlans = new Gson().fromJson(plansJson, listType);

            planList.clear();
            filteredPlanList.clear();

            if (travelPlans != null) {
                planList.addAll(travelPlans);
                filteredPlanList.addAll(travelPlans);
            }
        } catch (JsonSyntaxException e) {
            Log.d("MainActivity", "JSON parsing failed. Raw JSON: " + plansJson);
            Log.e("MainActivity", "JSON parsing error details: ", e);

            try {
                Type legacyType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> legacyPlans = new Gson().fromJson(plansJson, legacyType);

                planList.clear();
                filteredPlanList.clear();

                if (legacyPlans != null) {
                    for (Map<String, Object> planMap : legacyPlans) {
                        TravelPlan travelPlan;

                        Object placesObj = planMap.get("places");
                        List<String> placesList = new ArrayList<>();

                        if (placesObj instanceof String) {
                            placesList.add((String) placesObj);
                        } else if (placesObj instanceof List) {
                            // Convert each item in the list to String
                            List<?> rawList = (List<?>) placesObj;
                            for (Object item : rawList) {
                                placesList.add(String.valueOf(item));
                            }
                        }

                        travelPlan = new TravelPlan(
                                (String) planMap.get("selectedState"),
                                (String) planMap.get("startDate"),
                                (String) planMap.get("endDate"),
                                (String) planMap.get("budget"),
                                (String) planMap.get("travelType"),
                                placesList
                        );

                        // Handle day cards
                        Object dayCardsObj = planMap.get("dayCards");
                        if (dayCardsObj instanceof String) {
                            travelPlan.setDayCardsFromJson((String) dayCardsObj);
                        } else if (dayCardsObj instanceof List) {
                            String dayCardsJson = new Gson().toJson(dayCardsObj);
                            travelPlan.setDayCardsFromJson(dayCardsJson);
                        }


                        planList.add(travelPlan);
                        filteredPlanList.add(travelPlan);
                    }
                }
            } catch (Exception ex) {
                Log.e("MainActivity", "Error loading travel plans", ex);
                // Initialize empty lists if parsing fails
                planList = new ArrayList<>();
                filteredPlanList = new ArrayList<>();
            }
        }

        if (planAdapter != null) {
            planAdapter.notifyDataSetChanged();
        }
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                return true;
            } else if (itemId == R.id.nav_create_plan) {
                startActivity(new Intent(this, SeeTravelPlanActivity.class));
                return true;
            } else if (itemId == R.id.nav_create_place) {
                if (mAuth.getCurrentUser() != null) {
                    startActivity(new Intent(this, PlacesListActivity.class));
                } else {
                    Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (placesAdapter != null) {
            placesAdapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (placesAdapter != null) {
            placesAdapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTravelPlans();
    }
}
