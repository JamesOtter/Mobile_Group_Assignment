package com.example.mobile_group_assignment;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.Query;

public class PlacesListActivity extends AppCompatActivity {
    private PlacesFirestoreHelper placesHelper;
    private RecyclerView placesRecyclerView;
    private TextView emptyStateText;
    private PlacesAdapter adapter;
    private static final String TAG = "PlacesListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_list);

        // Initialize views
        placesRecyclerView = findViewById(R.id.placesRecyclerView);
        emptyStateText = findViewById(R.id.emptyStateText);
        findViewById(R.id.createPlaceButton).setOnClickListener(v -> {
            startActivity(new Intent(this, EditPlaceActivity.class));
        });

        // Initialize Firestore helper
        placesHelper = new PlacesFirestoreHelper();

        // Check authentication
        checkAuthentication();
    }

    private void checkAuthentication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            showNotLoggedInUI();
        } else {
            setupRecyclerView();
        }
    }

    private void showNotLoggedInUI() {
        emptyStateText.setText("Please login to view your places");
        emptyStateText.setVisibility(View.VISIBLE);
        placesRecyclerView.setVisibility(View.GONE);
        findViewById(R.id.createPlaceButton).setVisibility(View.GONE);
    }

    private void setupRecyclerView() {
        Query query = placesHelper.getPlacesByUser();
        if (query == null) {
            showNotLoggedInUI();
            return;
        }

        FirestoreRecyclerOptions<Place> options = new FirestoreRecyclerOptions.Builder<Place>()
                .setQuery(query, snapshot -> {
                    // Convert snapshot to Place object and set documentId
                    Place place = snapshot.toObject(Place.class);
                    place.setDocumentId(snapshot.getId());
                    return place;
                })
                .build();

        adapter = new PlacesAdapter(options, this);
        placesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        placesRecyclerView.setAdapter(adapter);

        // Show empty state if no places exists
        query.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.w(TAG, "Listen failed.", error);
                return;
            }

            if (value != null && value.isEmpty()) {
                emptyStateText.setText("No places found for this account");
                emptyStateText.setVisibility(View.VISIBLE);
            } else {
                emptyStateText.setVisibility(View.GONE);
            }
        });
    }

    private void openCreatePlaceActivity() {
        // We'll implement this in the next step
        startActivity(new Intent(this, EditPlaceActivity.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (adapter != null) {
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (adapter != null) {
            adapter.stopListening();
        }
    }
}