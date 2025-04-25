package com.example.mobile_group_assignment;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.SearchView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class SeeTravelPlanActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TravelPlanAdapter adapter;
    List<TravelPlan> planList;
    List<TravelPlan> filteredPlanList;
    ImageButton createTravelPlanButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_travel_plan);

        recyclerView = findViewById(R.id.recyclerViewPlans);
        createTravelPlanButton = findViewById(R.id.createTravelPlanButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        planList = new ArrayList<>();
        filteredPlanList = new ArrayList<>();

        // Initially load some sample plans
        planList.add(new TravelPlan("Kuala Lumpur", "2025-05-10", "Visit Petronas Towers"));
        planList.add(new TravelPlan("Langkawi", "2025-06-15", "Island hopping and beach relaxation"));
        planList.add(new TravelPlan("Penang", "2025-07-20", "Explore George Town"));

        filteredPlanList.addAll(planList);
        adapter = new TravelPlanAdapter(filteredPlanList);
        recyclerView.setAdapter(adapter);

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

        createTravelPlanButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeeTravelPlanActivity.this, TravelPlanCreatorActivity.class);
            startActivityForResult(intent, 1); // Deprecated but works for now
        });
    }

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
            String destination = data.getStringExtra("destination");
            String date = data.getStringExtra("date");
            String description = data.getStringExtra("description");

            TravelPlan newPlan = new TravelPlan(destination, date, description);
            planList.add(newPlan);
            filteredPlanList.add(newPlan);
            adapter.notifyDataSetChanged();
        }
    }
}
