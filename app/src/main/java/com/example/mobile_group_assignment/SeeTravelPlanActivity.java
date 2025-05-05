package com.example.mobile_group_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;


import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;

public class SeeTravelPlanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TravelPlanAdapter adapter;
    List<TravelPlan> planList;
    List<TravelPlan> filteredPlanList;
    ImageButton createTravelPlanButton;
    private FirebaseAuth mAuth;
    private static final int EDIT_PLAN_REQUEST = 1001;

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

        setupRecyclerView();
        loadTravelPlans();

        // Handle cases where data might be null
        if (filteredPlanList == null) {
            filteredPlanList = new ArrayList<>();
        }
        if (planList == null) {
            planList = new ArrayList<>();
        }

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

        if (resultCode == RESULT_OK && data != null) {
            String selectedState = data.getStringExtra("selectedState");
            String startDate = data.getStringExtra("startDate");
            String endDate = data.getStringExtra("endDate");
            String selectedBudget = data.getStringExtra("selectedBudget");
            String selectedCategories = data.getStringExtra("selectedCategories");
            String places = data.getStringExtra("places");
            String dayCardsJson = data.getStringExtra("dayCards");

            if (requestCode == EDIT_PLAN_REQUEST) {
                // Handle edited plan
                int position = data.getIntExtra("position", -1);
                Log.d("SeeTravelPlanActivity", "Editing plan at position: " + position);

                if (position != -1) {
                    TravelPlan updatedPlan = new TravelPlan(
                            selectedState,
                            startDate,
                            endDate,
                            selectedBudget,
                            selectedCategories,
                            places
                    );

                    // Update day cards if available
                    if (dayCardsJson != null && !dayCardsJson.isEmpty()) {
                        updatedPlan.setDayCardsFromJson(dayCardsJson);
                    }

                    // Update the original planList
                    planList.set(position, updatedPlan);

                    // Find and update in filteredPlanList
                    int filteredPosition = -1;
                    for (int i = 0; i < filteredPlanList.size(); i++) {
                        TravelPlan plan = filteredPlanList.get(i);
                        // Compare by essential properties instead of object reference
                        if (plan.getDestination().equals(data.getStringExtra("originalDestination")) &&
                                plan.getStartDate().equals(data.getStringExtra("originalStartDate")) &&
                                plan.getEndDate().equals(data.getStringExtra("originalEndDate"))) {
                            filteredPosition = i;
                            break;
                        }
                    }

                    if (filteredPosition != -1) {
                        filteredPlanList.set(filteredPosition, updatedPlan);
                        adapter.notifyItemChanged(filteredPosition);
                    } else {
                        // Refresh whole list if cant find
                        filteredPlanList.clear();
                        filteredPlanList.addAll(planList);
                        adapter.notifyDataSetChanged();
                    }

                    saveAllPlans();
                    Log.d("SeeTravelPlanActivity", "Plan updated: " + updatedPlan.getDestination());
                }
            }
            else if (requestCode == 1) {
                // Handle new plan
                TravelPlan newPlan = new TravelPlan(
                        selectedState,
                        startDate,
                        endDate,
                        selectedBudget,
                        selectedCategories,
                        places
                );
                // Add day cards if available
                if (dayCardsJson != null && !dayCardsJson.isEmpty()) {
                    newPlan.setDayCardsFromJson(dayCardsJson);
                }

                planList.add(newPlan);
                filteredPlanList.add(newPlan);
                adapter.notifyItemInserted(planList.size() - 1);
                saveAllPlans();

                Log.d("SeeTravelPlanActivity", "New plan added: " + newPlan.getDestination());
            }
        }
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
            try {
                Type legacyType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                List<Map<String, Object>> legacyPlans = new Gson().fromJson(plansJson, legacyType);

                planList.clear();
                filteredPlanList.clear();

                if (legacyPlans != null) {
                    for (Map<String, Object> planMap : legacyPlans) {
                        TravelPlan travelPlan = new TravelPlan(
                                (String) planMap.get("selectedState"),
                                (String) planMap.get("startDate"),
                                (String) planMap.get("endDate"),
                                (String) planMap.get("selectedBudget"),
                                (String) planMap.get("selectedCategories"),
                                "" // places can be empty
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
                Log.e("SeeTravelPlan", "Error loading travel plans", ex);
                // Initialize empty lists if parsing fails
                planList = new ArrayList<>();
                filteredPlanList = new ArrayList<>();
            }
        }

        runOnUiThread(() -> {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void openPlanDetails(TravelPlan plan) {
        Intent intent = new Intent(this, TravelPlanDetailActivity.class);

        // Pass all plan data to the detail activity
        intent.putExtra("destination", plan.getDestination());
        intent.putExtra("startDate", plan.getStartDate());
        intent.putExtra("endDate", plan.getEndDate());
        intent.putExtra("budgetRange", plan.getBudgetRange());
        intent.putExtra("travelType", plan.getTravelType());

        // Pass day cards as JSON
        intent.putExtra("dayCards", plan.getDayCardsJson());

        startActivity(intent);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewPlans);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TravelPlanAdapter(filteredPlanList);
        recyclerView.setAdapter(adapter);

        // Set up click listener
        adapter.setOnItemClickListener(position -> {
            TravelPlan selectedPlan = filteredPlanList.get(position);
            openPlanDetails(selectedPlan);
        });

        // Set up delete listener
        adapter.setOnItemDeleteListener(position -> {
            showDeleteConfirmationDialog(position);
        });

        // Set up edit listener
        adapter.setOnItemEditListener(position -> {
            editPlan(position);
        });

        // Add swipe to delete and drag to reorder functionality
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView,
                                  @NonNull RecyclerView.ViewHolder viewHolder,
                                  @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();

                // Update both filtered and original lists to maintain consistency
                Collections.swap(filteredPlanList, fromPosition, toPosition);
                Collections.swap(planList, planList.indexOf(filteredPlanList.get(fromPosition)),
                        planList.indexOf(filteredPlanList.get(toPosition)));

                adapter.moveItem(fromPosition, toPosition);
                saveAllPlans();
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                showDeleteConfirmationDialog(position);
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void showDeleteConfirmationDialog(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Travel Plan")
                .setMessage("Are you sure you want to delete this travel plan?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePlan(position);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    adapter.notifyItemChanged(position); // Reset the swiped item
                })
                .show();
    }

    private void deletePlan(int position) {
        if (position >= 0 && position < filteredPlanList.size()) {
            // Get the plan to be deleted
            TravelPlan planToDelete = filteredPlanList.get(position);

            // Remove from both lists
            planList.remove(planToDelete);
            filteredPlanList.remove(position);

            // Update SharedPreferences
            saveAllPlans();

            // Notify adapter
            adapter.notifyItemRemoved(position);

            // Show confirmation
            Toast.makeText(this, "Plan deleted", Toast.LENGTH_SHORT).show();

            // Refresh the activity if no plans left
            if (filteredPlanList.isEmpty()) {
                recreate();
            }
        }
    }

    private void editPlan(int position) {
        TravelPlan planToEdit = filteredPlanList.get(position);
        Intent intent = new Intent(this, ManageTravelPlanActivity.class);

        // Pass all plan data to the edit activity
        intent.putExtra("editMode", true);
        intent.putExtra("position", position);
        intent.putExtra("selectedState", planToEdit.getDestination());
        intent.putExtra("startDate", planToEdit.getStartDate());
        intent.putExtra("endDate", planToEdit.getEndDate());
        intent.putExtra("selectedBudget", planToEdit.getBudgetRange());
        intent.putExtra("selectedCategories", planToEdit.getTravelType());

        // Pass day cards as JSON
        intent.putExtra("dayCards", planToEdit.getDayCardsJson());

        // Also pass original values for identification
        intent.putExtra("originalDestination", planToEdit.getDestination());
        intent.putExtra("originalStartDate", planToEdit.getStartDate());
        intent.putExtra("originalEndDate", planToEdit.getEndDate());

        startActivityForResult(intent, EDIT_PLAN_REQUEST);
    }

    private void saveAllPlans() {
        SharedPreferences sharedPreferences = getSharedPreferences("TravelPlans", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("plans_list", new Gson().toJson(planList));
        editor.apply();
    }
}
