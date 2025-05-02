package com.example.mobile_group_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SeeTravelPlanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TravelPlanAdapter adapter;
    List<TravelPlan> planList;
    List<TravelPlan> filteredPlanList;
    ImageButton createTravelPlanButton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_travel_plan);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_create_plan);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                startActivity(new Intent(this, MainActivity.class));
                return true;
            } else if (itemId == R.id.nav_create_plan) {
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
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });

        // Initialize views
        recyclerView = findViewById(R.id.recyclerViewPlans);
        createTravelPlanButton = findViewById(R.id.createTravelPlanButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize lists
        planList = new ArrayList<>();
        filteredPlanList = new ArrayList<>();

        // Initialize adapter
        adapter = new TravelPlanAdapter(filteredPlanList);
        recyclerView.setAdapter(adapter);

        // Load saved travel plans
        loadTravelPlans();

        // Search functionality to filter displayed travel plans
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        // Create a new travel plan
        findViewById(R.id.createTravelPlanButton).setOnClickListener(v -> {
            startActivity(new Intent(this, TravelPlanCreatorActivity.class));
        });

        adapter.setOnItemClickListener(position -> {
            Log.d("SeeTravelPlanActivity", "Plan clicked at position: " + position);
            TravelPlan selectedPlan = filteredPlanList.get(position);

            // Create an Intent to navigate to the TravelPlanDetailActivity
            Intent intent = new Intent(SeeTravelPlanActivity.this, TravelPlanDetailActivity.class);

            // Pass the selected plan's data as extras
            intent.putExtra("destination", selectedPlan.getDestination());
            intent.putExtra("startDate", selectedPlan.getStartDate());
            intent.putExtra("endDate", selectedPlan.getEndDate());
            intent.putExtra("budgetRange", selectedPlan.getBudgetRange());
            intent.putExtra("travelType", selectedPlan.getTravelType());

            // Serialize the dayCards list to JSON
            String dayCardsJson = selectedPlan.getDayCardsJson(); // Make sure this method is implemented correctly

            // Pass the dayCards JSON string as an extra
            intent.putExtra("dayCards", dayCardsJson);

            // Log to verify if the intent is correctly created
            Log.d("SeeTravelPlanActivity", "Intent created with dayCards: " + dayCardsJson);

            // Start the activity to show the details
            startActivity(intent);
        });


    }

    // Method to filter the list based on the search query
    private void filterList(String query) {
        filteredPlanList.clear();
        if (query.isEmpty()) {
            filteredPlanList.addAll(planList);
        } else {
            for (TravelPlan plan : planList) {
                if (plan.getDestination().toLowerCase().contains(query.toLowerCase())) {
                    filteredPlanList.add(plan);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String selectedState = data.getStringExtra("selectedState"); // destination
            String startDate = data.getStringExtra("startDate");
            String endDate = data.getStringExtra("endDate");
            String selectedBudget = data.getStringExtra("selectedBudget");
            String selectedCategories = data.getStringExtra("selectedCategories");
            String places = data.getStringExtra("places");

            // Create a new travel plan object
            TravelPlan newPlan = new TravelPlan(selectedState, startDate, endDate, selectedBudget, selectedCategories, places);

            // Add the new plan to both lists
            planList.add(newPlan);
            filteredPlanList.add(newPlan);

            // Log to verify that the new plan is being added
            Log.d("SeeTravelPlanActivity", "New plan added: " + newPlan.getDestination());


            // Notify the adapter to update the view
            adapter.notifyDataSetChanged();
        }
    }

    private void loadTravelPlans() {
        SharedPreferences sharedPreferences = getSharedPreferences("TravelPlans", MODE_PRIVATE);

        // Retrieve the stored JSON string for travel plans, or initialize an empty list if no plans exist
        String plansJson = sharedPreferences.getString("plans_list", "[]");

        // Use Gson to deserialize the plans into a list of maps
        List<Map<String, String>> travelPlans = new Gson().fromJson(plansJson, new TypeToken<List<Map<String, String>>>() {}.getType());

        // Check if the list of travel plans is not empty
        if (travelPlans != null && !travelPlans.isEmpty()) {
            // Iterate over each saved plan and create TravelPlan objects
            for (Map<String, String> planMap : travelPlans) {
                String selectedState = planMap.get("selectedState");
                String startDate = planMap.get("startDate");
                String endDate = planMap.get("endDate");
                String budget = planMap.get("budget");
                String travelType = planMap.get("travelType");
                String places = planMap.get("places");

                // If number of days is available, get it; otherwise, set a default value
                String numberOfDays = planMap.get("numberOfDays");

                // Create a new TravelPlan object and add it to the list
                TravelPlan travelPlan = new TravelPlan(selectedState, startDate, endDate, budget, travelType, places);

                // Deserialize the dayCards JSON string into the list of DayCard objects
                String dayCardsJson = planMap.get("dayCards");
                Log.d("SeeTravelPlanActivity", "DayCards JSON: " + dayCardsJson);  // Log the JSON string

                if (dayCardsJson != null && !dayCardsJson.isEmpty()) {
                    List<DayCard> dayCards = new Gson().fromJson(dayCardsJson, new TypeToken<List<DayCard>>() {}.getType());

                    // Log the deserialized dayCards to ensure they are correctly converted
                    if (dayCards != null) {
                        Log.d("SeeTravelPlanActivity", "Deserialized DayCards: " + dayCards.size() + " dayCards loaded.");
                    } else {
                        Log.d("SeeTravelPlanActivity", "Failed to deserialize DayCards.");
                    }

                    travelPlan.setDayCards(dayCards != null ? dayCards : new ArrayList<>());
                } else {
                    Log.d("SeeTravelPlanActivity", "No dayCards data found for this plan.");
                    travelPlan.setDayCards(new ArrayList<>());
                }

                planList.add(travelPlan);
                filteredPlanList.add(travelPlan);
            }

            // Notify the adapter to update the RecyclerView
            adapter.notifyDataSetChanged();
        } else {
            Log.d("SeeTravelPlanActivity", "No travel plans found in SharedPreferences.");
        }
    }




}
