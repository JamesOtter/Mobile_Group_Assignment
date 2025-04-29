package com.example.mobile_group_assignment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class TravelPlanCreatorActivity extends AppCompatActivity {

    private RadioGroup stateRadioGroup, budgetRadioGroup;
    private Button  btnNext;

    private ImageButton btnStartDate,btnEndDate;
    private TextView tvStartDate, tvEndDate, tvAttractions;
    private CheckBox cbNature, cbFood, cbShopping, cbCulture, cbAdventure;
    private Spinner locationSpinner;

    private String selectedState = "";
    private String selectedBudget = "";
    private String startDate = "";
    private String endDate = "";
    private StringBuilder selectedCategories = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_plan_creator);

        // Initialize views
        locationSpinner = findViewById(R.id.locationSpinner);
        setupSpinners();
        setSpinnerSelection(locationSpinner, R.array.malaysia_states);

        // Date selection
        tvStartDate = findViewById(R.id.tv_start_date);
        tvEndDate = findViewById(R.id.tv_end_date);
        btnStartDate = findViewById(R.id.btn_start_date);
        btnEndDate = findViewById(R.id.btn_end_date);

        // Budget selection
        budgetRadioGroup = findViewById(R.id.budget_radio_group);

        // Travel categories
        cbNature = findViewById(R.id.cb_nature);
        cbFood = findViewById(R.id.cb_food);
        cbShopping = findViewById(R.id.cb_shopping);
        cbCulture = findViewById(R.id.cb_culture);
        cbAdventure = findViewById(R.id.cb_adventure);

        // Final button
        btnNext = findViewById(R.id.btn_next);

        // Set up listeners
        btnStartDate.setOnClickListener(v -> showDatePickerDialog(tvStartDate, true));
        btnEndDate.setOnClickListener(v -> showDatePickerDialog(tvEndDate, false));
        btnNext.setOnClickListener(v -> saveTravelPlan());

        // Add the listener for location spinner
        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Get the selected state
                selectedState = parentView.getItemAtPosition(position).toString();
                // Log to verify if the state is updated correctly
                Log.d("SelectedState", selectedState);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Optional: Handle when nothing is selected (if needed)
            }
        });
    }

    private void setSpinnerSelection(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void setupSpinners() {
        // Location spinner (Malaysian states)
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this,
                R.array.malaysia_states, android.R.layout.simple_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
    }

    private void showDatePickerDialog(TextView textView, boolean isStartDate) {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    String selected = dayOfMonth + "/" + (month + 1) + "/" + year;
                    textView.setText(selected);
                    if (isStartDate) {
                        startDate = selected;
                    } else {
                        endDate = selected;
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void saveTravelPlan() {
        // Get selected budget
        int selectedBudgetId = budgetRadioGroup.getCheckedRadioButtonId();
        if (selectedBudgetId != -1) {
            RadioButton selectedBudgetRadio = findViewById(selectedBudgetId);
            selectedBudget = selectedBudgetRadio.getText().toString();
        }

        // Get selected categories
        selectedCategories.setLength(0);
        if (cbNature.isChecked()) selectedCategories.append("Nature, ");
        if (cbFood.isChecked()) selectedCategories.append("Food, ");
        if (cbShopping.isChecked()) selectedCategories.append("Shopping, ");
        if (cbCulture.isChecked()) selectedCategories.append("Culture, ");
        if (cbAdventure.isChecked()) selectedCategories.append("Adventure, ");
        if (selectedCategories.length() > 2) {
            selectedCategories.setLength(selectedCategories.length() - 2); // Remove last comma
        }

        // Validate required fields
        if (selectedState.isEmpty() ||
                startDate.isEmpty() ||
                endDate.isEmpty() ||
                selectedBudget.isEmpty() ||
                selectedCategories.length() == 0) {

            // Show warning dialog
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Incomplete Travel Plan")
                    .setMessage("Please complete all fields before proceeding.")
                    .setPositiveButton("OK", null)
                    .show();
            return; // Do not proceed if incomplete
        }

        // Save to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("TravelPlan", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedState", selectedState);
        editor.putString("startDate", startDate);
        editor.putString("endDate", endDate);
        editor.putString("selectedBudget", selectedBudget);
        editor.putString("selectedCategories", selectedCategories.toString());
        editor.apply();

        // Proceed to next page
        Intent intent = new Intent(this, ManageTravelPlanActivity.class);
        startActivity(intent);
    }
}
