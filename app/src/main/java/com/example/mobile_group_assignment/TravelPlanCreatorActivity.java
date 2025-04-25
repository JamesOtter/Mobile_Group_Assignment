package com.example.mobile_group_assignment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.*;

public class TravelPlanCreatorActivity extends AppCompatActivity {

    private LinearLayout destinationLayout;
    private TextView startDateText, endDateText;
    private SeekBar budgetSeekBar;
    private Spinner categorySpinner;
    private LinearLayout attractionsLayout;
    private Button savePlanBtn;

    private String selectedDestination = "";
    private String startDate = "";
    private String endDate = "";
    private int budget = 1000;
    private String category = "";
    private List<String> selectedAttractions = new ArrayList<>();

    private String[] destinations = {"Kuala Lumpur", "Langkawi", "Penang"};
    private int[] destinationImages = {R.drawable.kl, R.drawable.langkawi, R.drawable.penang};

    private Map<String, String[]> attractionMap = new HashMap<String, String[]>() {{
        put("Kuala Lumpur", new String[]{"Petronas Towers", "KL Tower", "Batu Caves"});
        put("Langkawi", new String[]{"Eagle Square", "Sky Bridge", "Underwater World"});
        put("Penang", new String[]{"George Town", "Kek Lok Si", "Penang Hill"});
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScrollView scrollView = new ScrollView(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(layout);
        setContentView(scrollView);

        destinationLayout = new LinearLayout(this);
        destinationLayout.setOrientation(LinearLayout.HORIZONTAL);
        layout.addView(destinationLayout);
        setupDestinations();

        startDateText = new TextView(this);
        startDateText.setText("Start Date: Not set");
        startDateText.setOnClickListener(v -> pickDate(startDateText, true));
        layout.addView(startDateText);

        endDateText = new TextView(this);
        endDateText.setText("End Date: Not set");
        endDateText.setOnClickListener(v -> pickDate(endDateText, false));
        layout.addView(endDateText);

        budgetSeekBar = new SeekBar(this);
        budgetSeekBar.setMax(5000);
        budgetSeekBar.setProgress(1000);
        layout.addView(budgetSeekBar);
        TextView budgetLabel = new TextView(this);
        budgetLabel.setText("Budget: RM1000");
        layout.addView(budgetLabel);
        budgetSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                budget = progress;
                budgetLabel.setText("Budget: RM" + progress);
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        categorySpinner = new Spinner(this);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"Nature", "Food", "Shopping", "Culture"});
        categorySpinner.setAdapter(spinnerAdapter);
        layout.addView(categorySpinner);

        attractionsLayout = new LinearLayout(this);
        attractionsLayout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(attractionsLayout);

        savePlanBtn = new Button(this);
        savePlanBtn.setText("Save Plan");
        savePlanBtn.setOnClickListener(v -> savePlan());
        layout.addView(savePlanBtn);
    }

    private void setupDestinations() {
        for (int i = 0; i < destinations.length; i++) {
            ImageView img = new ImageView(this);
            img.setImageResource(destinationImages[i]);
            img.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
            int index = i;
            img.setOnClickListener(v -> {
                selectedDestination = destinations[index];
                Toast.makeText(this, "Selected: " + selectedDestination, Toast.LENGTH_SHORT).show();
                showAttractions(selectedDestination);
            });
            destinationLayout.addView(img);
        }
    }

    private void pickDate(TextView targetView, boolean isStart) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.getTime());
                    if (isStart) {
                        startDate = date;
                    } else {
                        endDate = date;
                    }
                    targetView.setText((isStart ? "Start" : "End") + " Date: " + date);
                },
                calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void showAttractions(String location) {
        attractionsLayout.removeAllViews();
        String[] attractions = attractionMap.get(location);
        if (attractions != null) {
            for (String place : attractions) {
                CheckBox box = new CheckBox(this);
                box.setText(place);
                box.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) selectedAttractions.add(place);
                    else selectedAttractions.remove(place);
                });
                attractionsLayout.addView(box);
            }
        }
    }

    private void savePlan() {
        category = categorySpinner.getSelectedItem().toString();
        String notes = "Budget: RM" + budget + "\nCategory: " + category + "\nPlaces: " + String.join(", ", selectedAttractions);
        String dateRange = startDate + " to " + endDate;

        TravelPlan travelPlan = new TravelPlan(selectedDestination, dateRange, notes);

        // Save in SharedPreferences as JSON
        SharedPreferences prefs = getSharedPreferences("MyTravelPlans", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        String planString = travelPlan.getDestination() + "||" + travelPlan.getDate() + "||" + travelPlan.getNotes();
        editor.putString("latest_plan", planString);
        editor.apply();

        Toast.makeText(this, "Plan saved!", Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(() -> {
            startActivity(new android.content.Intent(this, SeeTravelPlanActivity.class));
            finish();
        }, 1000);
    }

}
