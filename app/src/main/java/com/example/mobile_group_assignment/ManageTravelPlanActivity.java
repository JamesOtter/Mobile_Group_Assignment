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

public class ManageTravelPlanActivity extends AppCompatActivity {

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

        firestore = FirebaseFirestore.getInstance();
        dayCards = new ArrayList<>();

        dayRecyclerView = findViewById(R.id.dayRecyclerView);
        dayRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        dayCardAdapter = new DayCardAdapter(dayCards, this);
        dayRecyclerView.setAdapter(dayCardAdapter);

        btnSave = findViewById(R.id.btn_save);

        // Load Travel Plan Info from SharedPreferences
        loadTravelPlanData();

        // Create day cards (Day 1, Day 2, etc.) based on start and end date
        createDayCards();

        // Save Button clicked
        btnSave.setOnClickListener(v -> {
            saveTravelPlanData();
            Intent intent = new Intent(ManageTravelPlanActivity.this, SeeTravelPlanActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        });
    }

    private void loadTravelPlanData() {
        SharedPreferences sharedPreferences = getSharedPreferences("TravelPlan", MODE_PRIVATE);
        selectedState = sharedPreferences.getString("selectedState", "");
        startDate = sharedPreferences.getString("startDate", "");
        endDate = sharedPreferences.getString("endDate", "");
        selectedBudget = sharedPreferences.getString("selectedBudget", "");
        selectedCategories = sharedPreferences.getString("selectedCategories", "");
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
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Retrieve the existing list of saved plans, or initialize a new one if none exists
        String plansJson = sharedPreferences.getString("plans_list", "[]");
        List<Map<String, String>> travelPlans = new Gson().fromJson(plansJson, new TypeToken<List<Map<String, String>>>() {}.getType());

        // Create a new map to store the current travel plan
        Map<String, String> currentPlan = new HashMap<>();
        currentPlan.put("selectedState", selectedState);
        currentPlan.put("startDate", startDate);
        currentPlan.put("endDate", endDate);
        currentPlan.put("budget", selectedBudget);
        currentPlan.put("travelType", selectedCategories);

        // Calculate the number of days for the current plan
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (start != null && end != null) {
                long diffInMillies = Math.abs(end.getTime() - start.getTime());
                long diffDays = (diffInMillies / (1000 * 60 * 60 * 24)) + 1;
                currentPlan.put("numberOfDays", String.valueOf(diffDays));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Collect all selected places and add them to the current plan
        List<String> allSelectedPlaces = new ArrayList<>();
        for (DayCard dayCard : dayCards) {
            List<String> selectedPlaces = dayCard.getSelectedPlaces();
            if (selectedPlaces != null && !selectedPlaces.isEmpty()) {
                allSelectedPlaces.addAll(selectedPlaces);
            }
        }

        if (!allSelectedPlaces.isEmpty()) {
            String places = TextUtils.join(", ", allSelectedPlaces);
            currentPlan.put("places", places);
            Log.d("SaveTravelPlan", "Updated places saved: " + places);
        } else {
            currentPlan.remove("places");  // If no places selected, remove the key
        }

        // 1. Serialize the DayCard objects to JSON
        String dayCardsJson = new Gson().toJson(dayCards);

        // 2. Save the dayCards JSON inside the current plan
        currentPlan.put("dayCards", dayCardsJson);

        // 3. Log to verify
        Log.d("SaveTravelPlan", "Serialized DayCards: " + dayCardsJson);

        // 4. Add the current plan to the list of travel plans
        travelPlans.add(currentPlan);

        // 5. Serialize the entire updated travel plans list
        String updatedPlansJson = new Gson().toJson(travelPlans);

        // 6. Save the updated plans list into SharedPreferences
        editor.putString("plans_list", updatedPlansJson);

        // 7. Apply all changes
        editor.apply();


        // Show a confirmation message
        Toast.makeText(this, "Travel plan saved successfully!", Toast.LENGTH_SHORT).show();
    }


    // Called when user clicks on a day card to select places
    public void openPlaceSelectionDialog(final int dayPosition) {
        List<String> placesNameList = new ArrayList<>();
        List<String> placesImageUrlList = new ArrayList<>();

        firestore.collection("places")
                .whereEqualTo("location", selectedState)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String name = document.getString("name");
                        String imageUrl = document.getString("photoUrl");
                        if (name != null && imageUrl != null) {
                            placesNameList.add(name);
                            placesImageUrlList.add(imageUrl);
                        }
                    }

                    if (!placesNameList.isEmpty()) {
                        showPlacesSelectionDialog(dayPosition, placesNameList, placesImageUrlList);
                    } else {
                        showNoPlacesFoundDialog();
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to load places!", Toast.LENGTH_SHORT).show();
                });
    }

    private void showPlacesSelectionDialog(int dayPosition, List<String> placesNameList, List<String> placesImageUrlList) {
        String[] placesArray = placesNameList.toArray(new String[0]);
        boolean[] checkedItems = new boolean[placesArray.length];

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Places for " + dayCards.get(dayPosition).getTitle());
        builder.setMultiChoiceItems(placesArray, checkedItems, (dialog, which, isChecked) -> {
            // Do nothing on real-time selection
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
