package com.example.mobile_group_assignment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.lang.reflect.Type;

public class ManageTravelPlanActivity extends AppCompatActivity {

    private static final String TAG = "ManageTravelPlan";

    private boolean isEditMode = false;
    private int editPosition = -1;
    private RecyclerView dayRecyclerView;
    private DayCardAdapter dayCardAdapter;
    private List<DayCard> dayCards;

    private FirebaseFirestore firestore;
    private String selectedState;
    private String startDate;
    private String endDate;
    private String selectedBudget;
    private String selectedCategories;

    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managet_travel_plan);

        // Initialize views
        btnSave = findViewById(R.id.btn_save);
        if (btnSave == null) {
            throw new IllegalStateException("Layout must include a Button with id btn_save");
        }

        // Check if in edit mode
        isEditMode = getIntent().getBooleanExtra("editMode", false);
        editPosition = getIntent().getIntExtra("position", -1);

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance();
        dayCards = new ArrayList<>();

        // Initialize RecyclerView
        dayRecyclerView = findViewById(R.id.dayRecyclerView);
        dayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dayCardAdapter = new DayCardAdapter(dayCards, this);
        dayRecyclerView.setAdapter(dayCardAdapter);

        if (isEditMode) {
            selectedState = getIntent().getStringExtra("selectedState");
            startDate = getIntent().getStringExtra("startDate");
            endDate = getIntent().getStringExtra("endDate");
            selectedBudget = getIntent().getStringExtra("selectedBudget");
            selectedCategories = getIntent().getStringExtra("selectedCategories");

            Log.d(TAG, "Edit mode - Selected state: " + selectedState);

            // Load day cards from JSON
            String dayCardsJson = getIntent().getStringExtra("dayCards");
            if (dayCardsJson != null && !dayCardsJson.isEmpty()) {
                Type type = new TypeToken<List<DayCard>>(){}.getType();
                List<DayCard> existingDayCards = new Gson().fromJson(dayCardsJson, type);
                if (existingDayCards != null) {
                    dayCards.addAll(existingDayCards);
                    dayCardAdapter.notifyDataSetChanged();
                }
            }
        } else {
            // Load from SharedPreferences for create mode
            loadTravelPlanData();
            Log.d(TAG, "Create mode - Selected state: " + selectedState);
            createDayCards();
        }

        // Set up save button
        btnSave.setOnClickListener(v -> saveTravelPlanData());
    }

    private void loadTravelPlanData() {
        Intent intent = getIntent();
        if (intent.hasExtra("selectedState")) {
            selectedState = intent.getStringExtra("selectedState");
            startDate = intent.getStringExtra("startDate");
            endDate = intent.getStringExtra("endDate");
            selectedBudget = intent.getStringExtra("selectedBudget");
            selectedCategories = intent.getStringExtra("selectedCategories");

            Log.d(TAG, "Loaded state from Intent: " + selectedState);
        } else {
            SharedPreferences sharedPreferences = getSharedPreferences("TravelPlan", MODE_PRIVATE);
            selectedState = sharedPreferences.getString("selectedState", "");
            startDate = sharedPreferences.getString("startDate", "");
            endDate = sharedPreferences.getString("endDate", "");
            selectedBudget = sharedPreferences.getString("selectedBudget", "");
            selectedCategories = sharedPreferences.getString("selectedCategories", "");

            Log.d(TAG, "Loaded state from SharedPreferences: " + selectedState);
        }

        // Extra validation to ensure we have data
        if (selectedState == null || selectedState.isEmpty()) {
            Log.e(TAG, "No state was loaded from either Intent or SharedPreferences!");
            Toast.makeText(this, "Error: Failed to load destination information", Toast.LENGTH_LONG).show();
            finish(); // Close this activity as we can't proceed
            return;
        }

        // Make sure SharedPreferences is also updated with current values
        SharedPreferences.Editor editor = getSharedPreferences("TravelPlan", MODE_PRIVATE).edit();
        editor.putString("selectedState", selectedState);
        editor.putString("startDate", startDate);
        editor.putString("endDate", endDate);
        editor.putString("selectedBudget", selectedBudget);
        editor.putString("selectedCategories", selectedCategories);
        editor.apply();
    }

    private void createDayCards() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start != null && end != null) {
                long diffInMillies = Math.abs(end.getTime() - start.getTime());
                long diffDays = (diffInMillies / (1000 * 60 * 60 * 24)) + 1;

                for (int i = 0; i < diffDays; i++) {
                    dayCards.add(new DayCard("Day " + (i + 1)));
                }
                dayCardAdapter.notifyDataSetChanged();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            Toast.makeText(this, "Invalid date format!", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveTravelPlanData() {
        SharedPreferences sharedPreferences = getSharedPreferences("TravelPlans", MODE_PRIVATE);
        String plansJson = sharedPreferences.getString("plans_list", "[]");

        Type type = new TypeToken<List<TravelPlan>>(){}.getType();
        List<TravelPlan> travelPlans = new Gson().fromJson(plansJson, type);
        if (travelPlans == null) travelPlans = new ArrayList<>();

        // Collect all selected places from all day cards
        List<String> allSelectedPlaces = new ArrayList<>();
        for (DayCard dayCard : dayCards) {
            if (dayCard.getSelectedPlaces() != null) {
                allSelectedPlaces.addAll(dayCard.getSelectedPlaces());
            }
        }

        TravelPlan newPlan = new TravelPlan(
                selectedState,
                startDate,
                endDate,
                selectedBudget,
                selectedCategories,
                allSelectedPlaces,
                dayCards
        );

        if (isEditMode && editPosition != -1) {
            // Update existing plan
            travelPlans.set(editPosition, newPlan);
        } else {
            // Add new plan
            travelPlans.add(newPlan);
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("plans_list", new Gson().toJson(travelPlans));
        editor.apply();

        // Create result intent
        Intent resultIntent = new Intent();
        if (isEditMode) {
            resultIntent.putExtra("position", editPosition);
            resultIntent.putExtra("originalDestination", getIntent().getStringExtra("destination"));
            resultIntent.putExtra("originalStartDate", getIntent().getStringExtra("startDate"));
            resultIntent.putExtra("originalEndDate", getIntent().getStringExtra("endDate"));
        }

        resultIntent.putExtra("selectedState", selectedState);
        resultIntent.putExtra("startDate", startDate);
        resultIntent.putExtra("endDate", endDate);
        resultIntent.putExtra("selectedBudget", selectedBudget);
        resultIntent.putExtra("selectedCategories", selectedCategories);
        resultIntent.putExtra("dayCards", newPlan.getDayCardsJson());
        resultIntent.putExtra("places", TextUtils.join(", ", allSelectedPlaces));

        setResult(RESULT_OK, resultIntent);

        if (!isEditMode) {
            // For new plans, go back to home directly
            Intent homeIntent = new Intent(this, MainActivity.class);  // Replace MainActivity with your home activity name
            homeIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(homeIntent);
        }

        finish();

        Toast.makeText(this,
                isEditMode ? "Plan updated" : "Plan saved",
                Toast.LENGTH_SHORT).show();
    }

    // Called when user clicks on a day card to select places
    public void openPlaceSelectionDialog(final int dayPosition) {
        List<String> placesNameList = new ArrayList<>();
        List<String> placesImageUrlList = new ArrayList<>();

        // Debug
        Log.d(TAG, "Querying Firestore for places in state: " + selectedState);

        if (selectedState == null || selectedState.isEmpty()) {
            Log.e(TAG, "Selected state is null or empty!");
            Toast.makeText(this, "Error: No state selected", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("places")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean foundPlaces = false;

                    // Log the number of documents found
                    Log.d(TAG, "Total documents in 'places' collection: " + queryDocumentSnapshots.size());

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String location = document.getString("location");

                        // Debug Log each document's location
                        Log.d(TAG, "Document location: " + location);

                        // Check if location matches our state
                        if (location != null && location.equalsIgnoreCase(selectedState)) {
                            String name = document.getString("name");
                            String imageUrl = document.getString("photoUrl");
                            if (name != null && imageUrl != null) {
                                placesNameList.add(name);
                                placesImageUrlList.add(imageUrl);
                                foundPlaces = true;
                            }
                        }
                    }

                    if (foundPlaces) {
                        showPlacesSelectionDialog(dayPosition, placesNameList, placesImageUrlList);
                    } else {
                        Log.w(TAG, "No places found for state: " + selectedState);
                        showNoPlacesFoundDialog();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to query Firestore", e);
                    Toast.makeText(this, "Failed to load places: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showPlacesSelectionDialog(int dayPosition, List<String> placesNameList, List<String> placesImageUrlList) {
        String[] placesArray = placesNameList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[placesArray.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Places for " + dayCards.get(dayPosition).getTitle());
        builder.setMultiChoiceItems(placesArray, checkedItems, (dialog, which, isChecked) -> {
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            List<String> selectedPlaces = new ArrayList<>();
            List<String> selectedImageUrls = new ArrayList<>();

            AlertDialog alertDialog = (AlertDialog) dialog;
            for (int i = 0; i < placesArray.length; i++) {
                if (alertDialog.getListView().isItemChecked(i)) {
                    selectedPlaces.add(placesNameList.get(i));
                    selectedImageUrls.add(placesImageUrlList.get(i));
                }
            }

            DayCard selectedDayCard = dayCards.get(dayPosition);
            selectedDayCard.setSelectedPlaces(selectedPlaces);
            selectedDayCard.setSelectedPlaceImageUrls(selectedImageUrls);

            if (selectedPlaces.size() > 4) {
                showTooManyPlacesWarning();
            }

            dayCardAdapter.notifyItemChanged(dayPosition);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showTooManyPlacesWarning() {
        new AlertDialog.Builder(this)
                .setTitle("Too Many Places")
                .setMessage("Selecting more than 4 places might make your day too rushed. Please plan carefully.")
                .setPositiveButton("OK", null)
                .show();
    }

    private void showNoPlacesFoundDialog() {
        new AlertDialog.Builder(this)
                .setTitle("No Places Found")
                .setMessage("No attractions found for the selected state.")
                .setPositiveButton("OK", null)
                .show();
    }
}