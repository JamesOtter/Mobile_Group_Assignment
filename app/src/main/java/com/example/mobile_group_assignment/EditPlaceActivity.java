package com.example.mobile_group_assignment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.util.IOUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;

public class EditPlaceActivity extends AppCompatActivity {
    private EditText nameEditText, visitationTimeEditText, durationEditText,
            costEditText, descriptionEditText;
    private Spinner typeSpinner, locationSpinner;
    private ImageView placeImageView, backButton;
    private Button saveButton, uploadImageButton;

    private PlacesFirestoreHelper placesHelper;
    private Place currentPlace;
    private boolean isEditMode = false;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private ImageUploadHelper imageUploadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_place);



        // Initialize views
        initializeViews();

        //back button
        backButton.setOnClickListener(v -> onBackPressed());

        // Initialize Firestore and Image helper
        placesHelper = new PlacesFirestoreHelper();
        imageUploadHelper = new ImageUploadHelper();

        // Check if we're editing an existing place
        checkEditMode();

        // Setup spinners
        setupSpinners();

        // Set up button click listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        backButton = findViewById(R.id.backButton);
        nameEditText = findViewById(R.id.nameEditText);
        visitationTimeEditText = findViewById(R.id.visitationTimeEditText);
        durationEditText = findViewById(R.id.durationEditText);
        costEditText = findViewById(R.id.costEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        typeSpinner = findViewById(R.id.typeSpinner);
        locationSpinner = findViewById(R.id.locationSpinner);
        placeImageView = findViewById(R.id.placeImageView);
        saveButton = findViewById(R.id.saveButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
    }

    private void checkEditMode() {
        if (getIntent().hasExtra("PLACE_ID")) {
            isEditMode = true;
            String placeId = getIntent().getStringExtra("PLACE_ID");
            fetchPlaceData(placeId);
            saveButton.setText("Update Place");
        }
    }

    private void fetchPlaceData(String placeId) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading place data...");
        progressDialog.show();

        placesHelper.getPlaceById(placeId, new PlacesFirestoreHelper.PlaceCallback() {
            @Override
            public void onSuccess(Place place) {
                progressDialog.dismiss();
                currentPlace = place;
                populateFormWithPlaceData(place);
            }

            @Override
            public void onFailure(Exception e) {
                progressDialog.dismiss();
                Toast.makeText(EditPlaceActivity.this,
                        "Failed to load place: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFormWithPlaceData(Place place) {
        nameEditText.setText(place.getName());
        visitationTimeEditText.setText(place.getVisitationTime());
        durationEditText.setText(String.valueOf(place.getDuration()));
        costEditText.setText(String.valueOf(place.getCost()));
        descriptionEditText.setText(place.getDescription());

        // Set spinner selections
        setSpinnerSelection(typeSpinner, R.array.place_types, place.getType());
        setSpinnerSelection(locationSpinner, R.array.malaysia_states, place.getLocation());

        // Load image if exists
        if (place.getPhotoUrl() != null && !place.getPhotoUrl().isEmpty()) {
            Picasso.get().load(place.getPhotoUrl()).into(placeImageView);
        }
    }

    private void setSpinnerSelection(Spinner spinner, int arrayResId, String value) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                arrayResId, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (value != null) {
            int position = adapter.getPosition(value);
            if (position >= 0) {
                spinner.setSelection(position);
            }
        }
    }

    private void setupSpinners() {
        // Type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.place_types, R.layout.custom_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        // Location spinner (Malaysian states)
        ArrayAdapter<CharSequence> locationAdapter = ArrayAdapter.createFromResource(this,
                R.array.malaysia_states, R.layout.custom_spinner_item);
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locationAdapter);
    }

    private void setupButtonListeners() {
        saveButton.setOnClickListener(v -> savePlace());
        uploadImageButton.setOnClickListener(v -> uploadImage());
    }

    private void savePlace() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "You need to be logged in to save places", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get values from form
        String name = nameEditText.getText().toString().trim();
        String type = typeSpinner.getSelectedItem().toString();
        String visitationTime = visitationTimeEditText.getText().toString().trim();
        String location = locationSpinner.getSelectedItem().toString();
        int duration = Integer.parseInt(durationEditText.getText().toString().trim());
        double cost = Double.parseDouble(costEditText.getText().toString().trim());
        String description = descriptionEditText.getText().toString().trim();
        String createdBy = user.getUid();

        if (name.isEmpty() || visitationTime.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading dialog
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Saving place...");
        progressDialog.show();

        if (selectedImageUri != null) {
            // Case 1: New image needs to be uploaded
            uploadImageAndSavePlace(name, type, visitationTime, location, duration,
                    cost, description, createdBy, progressDialog);
        } else if (isEditMode && currentPlace.getPhotoUrl() != null && !currentPlace.getPhotoUrl().isEmpty()) {
            // Case 2: Editing existing place with existing image (keep current photoUrl)
            savePlaceToFirestore(name, type, visitationTime, location, duration,
                    cost, description, createdBy, currentPlace.getPhotoUrl(), progressDialog);
        } else {
            // Case 3: No image selected (empty string for photoUrl)
            savePlaceToFirestore(name, type, visitationTime, location, duration,
                    cost, description, createdBy, "", progressDialog);
        }
    }

    private void uploadImageAndSavePlace(String name, String type, String visitationTime,
                                         String location, int duration, double cost,
                                         String description, String createdBy,
                                         ProgressDialog progressDialog) {
        try {
            InputStream stream = getContentResolver().openInputStream(selectedImageUri);
            byte[] imageData = IOUtils.toByteArray(stream);

            imageUploadHelper.uploadPlaceImage(imageData)
                    .addOnSuccessListener(taskSnapshot -> {
                        taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                            savePlaceToFirestore(name, type, visitationTime, location, duration,
                                    cost, description, createdBy, uri.toString(),
                                    progressDialog);
                        });
                    })
                    .addOnFailureListener(e -> {
                        progressDialog.dismiss();
                        Toast.makeText(this, "Image upload failed: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    });
        } catch (IOException e) {
            progressDialog.dismiss();
            Toast.makeText(this, "Error reading image", Toast.LENGTH_SHORT).show();
        }
    }

    private void savePlaceToFirestore(String name, String type, String visitationTime,
                                      String location, int duration, double cost,
                                      String description, String createdBy, String photoUrl,
                                      ProgressDialog progressDialog) {
        if (isEditMode) {
            // Update existing place
            currentPlace.setName(name);
            currentPlace.setType(type);
            currentPlace.setVisitationTime(visitationTime);
            currentPlace.setLocation(location);
            currentPlace.setDuration(duration);
            currentPlace.setCost(cost);
            currentPlace.setDescription(description);
            currentPlace.setPhotoUrl(photoUrl);

            placesHelper.updatePlace(currentPlace, new PlacesFirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    Toast.makeText(EditPlaceActivity.this, "Place updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onFailure(Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditPlaceActivity.this, "Failed to update place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Create new place
            Place newPlace = new Place(name, photoUrl, type, visitationTime,
                    location, duration, cost, description, createdBy);

            placesHelper.addPlace(newPlace, new PlacesFirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess() {
                    runOnUiThread(() -> {
                        progressDialog.dismiss();
                        Toast.makeText(EditPlaceActivity.this,
                                "Place created successfully",
                                Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(EditPlaceActivity.this, "Failed to create place: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                placeImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
            }
        }
    }
}