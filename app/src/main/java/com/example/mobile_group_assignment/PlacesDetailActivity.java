package com.example.mobile_group_assignment;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;

public class PlacesDetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places_detail);

        String placeId = getIntent().getStringExtra("PLACE_ID");
        if (placeId == null) {
            Toast.makeText(this, "Place not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize all the views
        ImageView placeImage = findViewById(R.id.placeImage);
        TextView nameView = findViewById(R.id.placeName);
        TextView locationView = findViewById(R.id.placeLocation);
        TextView typeView = findViewById(R.id.placeType);
        TextView visitationTimeView = findViewById(R.id.placeVisitationTime);
        TextView durationView = findViewById(R.id.placeDuration);
        TextView costView = findViewById(R.id.placeCost);
        TextView descriptionView = findViewById(R.id.placeDescription);

        PlacesFirestoreHelper helper = new PlacesFirestoreHelper();
        helper.getPlaceById(placeId, new PlacesFirestoreHelper.PlaceCallback() {
            @Override
            public void onSuccess(Place place) {
                // Update all views with place details
                nameView.setText(place.getName());
                locationView.setText(place.getLocation());
                typeView.setText(place.getType());
                visitationTimeView.setText(place.getVisitationTime());
                durationView.setText(String.format("%d minutes", place.getDuration()));
                costView.setText(String.format("$%.2f", place.getCost()));
                descriptionView.setText(place.getDescription());

                // Load image
                if (place.getPhotoUrl() != null && !place.getPhotoUrl().isEmpty()) {
                    Picasso.get()
                            .load(place.getPhotoUrl())
                            .placeholder(R.drawable.placeholder_image)
                            .into(placeImage);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(PlacesDetailActivity.this,
                        "Failed to load place details: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
