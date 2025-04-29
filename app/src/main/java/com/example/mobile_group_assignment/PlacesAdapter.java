package com.example.mobile_group_assignment;

import android.content.Context;
import android.content.Intent;

import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class PlacesAdapter extends FirestoreRecyclerAdapter<Place, PlacesAdapter.PlaceViewHolder> {
    private final Context context;

    public PlacesAdapter(@NonNull FirestoreRecyclerOptions<Place> options, Context context) {
        super(options);
        this.context = context;

    private final PlacesFirestoreHelper placesHelper;
    public PlacesAdapter(@NonNull FirestoreRecyclerOptions<Place> options, Context context) {
        super(options);
        this.context = context;
        this.placesHelper = new PlacesFirestoreHelper();

    }

    @Override
    protected void onBindViewHolder(@NonNull PlaceViewHolder holder, int position, @NonNull Place model) {
        holder.nameText.setText(model.getName());
        holder.locationText.setText(model.getLocation());
        holder.typeText.setText(model.getType());

        // Load image using Picasso (we'll add this library later)
        if (model.getPhotoUrl() != null && !model.getPhotoUrl().isEmpty()) {
            Picasso.get().load(model.getPhotoUrl()).into(holder.placeImage);
        }


        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditPlaceActivity.class);
            intent.putExtra("PLACE_ID", model.getDocumentId()); // Pass the Firestore document ID
            context.startActivity(intent);
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EditPlaceActivity.class);
            intent.putExtra("PLACE_ID", model.getDocumentId()); // Pass the Firestore document ID
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            showDeleteConfirmationDialog(holder.itemView.getContext(),
                    model.getDocumentId(),
                    model.getPhotoUrl());
        });

    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place, parent, false);
        return new PlaceViewHolder(view);
    }


    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, locationText, typeText;
        ImageView placeImage;
        Button editButton;

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        // Disable change animations to prevent inconsistencies
        recyclerView.setItemAnimator(null);
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, locationText, typeText;
        ImageView placeImage;
        Button editButton, deleteButton;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.placeName);
            locationText = itemView.findViewById(R.id.placeLocation);
            typeText = itemView.findViewById(R.id.placeType);
            placeImage = itemView.findViewById(R.id.placeImage);
            editButton = itemView.findViewById(R.id.editButton);

            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    private void showDeleteConfirmationDialog(Context context, String placeId, String imageUrl) {
        new AlertDialog.Builder(context)
                .setTitle("Delete Place")
                .setMessage("Are you sure you want to delete this place?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deletePlace(placeId, imageUrl);
                })
                .setNegativeButton("Cancel", null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void deletePlace(String placeId, String imageUrl) {
        if (placesHelper != null) {
            placesHelper.deletePlace(placeId, imageUrl, new PlacesFirestoreHelper.FirestoreCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(context, "Place deleted", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(context, "Delete failed: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(context, "Error: Helper not initialized", Toast.LENGTH_SHORT).show();
            Log.e("PlacesAdapter", "placesHelper is null");

        }
    }
}
