package com.example.mobile_group_assignment;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.List;

public class TravelPlanDetailActivity extends AppCompatActivity {

    // UI components
    private TextView textDestination, textStartDate, textEndDate, textBudgetRange, textTravelType, textPlaces;
    private RecyclerView recyclerViewDayCards;
    private DayCardAdapter dayCardAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_travel_plan_detail);

        // Bind UI components
        textDestination = findViewById(R.id.textDestination);
        textStartDate = findViewById(R.id.textStartDate);
        textEndDate = findViewById(R.id.textEndDate);
        textBudgetRange = findViewById(R.id.textBudgetRange);
        textTravelType = findViewById(R.id.textTravelType);
        textPlaces = findViewById(R.id.textPlaces);
        recyclerViewDayCards = findViewById(R.id.recyclerViewDayCards);

        // Retrieve data from Intent
        String destination = getIntent().getStringExtra("destination");
        String startDate = getIntent().getStringExtra("startDate");
        String endDate = getIntent().getStringExtra("endDate");
        String budgetRange = getIntent().getStringExtra("budgetRange");
        String travelType = getIntent().getStringExtra("travelType");
        String dayCardsJson = getIntent().getStringExtra("dayCards");

        // Log received data for debugging
        Log.d("TravelPlanDetailActivity", "Received dayCardsJson: " + dayCardsJson);

        // Set basic data to TextViews
        textDestination.setText(destination != null ? destination : "No destination");
        textStartDate.setText(startDate != null ? startDate : "No start date");
        textEndDate.setText(endDate != null ? endDate : "No end date");
        textBudgetRange.setText(budgetRange != null ? budgetRange : "No budget range");
        textTravelType.setText(travelType != null ? travelType : "No travel type");

        // Set up RecyclerView
        recyclerViewDayCards.setLayoutManager(new LinearLayoutManager(this));

        // Deserialize dayCardsJson safely
        List<DayCard> dayCards = null;
        if (dayCardsJson != null && !dayCardsJson.isEmpty()) {
            try {
                dayCards = new Gson().fromJson(dayCardsJson, new TypeToken<List<DayCard>>() {}.getType());
            } catch (Exception e) {
                Log.e("TravelPlanDetailActivity", "Failed to parse dayCardsJson", e);
            }
        }

        // Show DayCards or "No day cards available"
        if (dayCards != null && !dayCards.isEmpty()) {
            dayCardAdapter = new DayCardAdapter(dayCards, this);
            recyclerViewDayCards.setAdapter(dayCardAdapter);
            textPlaces.setText(""); // Clear "No day cards" message if list is available
        } else {
            textPlaces.setText("No day cards available.");
        }
    }
}
