package com.example.mobile_group_assignment;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.app.ActivityOptions;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.squareup.picasso.Picasso;

public class HomePlacesAdapter extends FirestoreRecyclerAdapter<Place, HomePlacesAdapter.PlaceViewHolder> {
    private final Context context;

    public HomePlacesAdapter(@NonNull FirestoreRecyclerOptions<Place> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull PlaceViewHolder holder, int position, @NonNull Place model) {
        try {
            holder.nameText.setText(model.getName());
            holder.typeText.setText(model.getType());

            if (model.getPhotoUrl() != null && !model.getPhotoUrl().isEmpty()) {
                Picasso.get()
                        .load(model.getPhotoUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(holder.placeImage);
            }

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlacesDetailActivity.class);
                intent.putExtra("PLACE_ID", model.getDocumentId());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ActivityOptions options = ActivityOptions
                            .makeSceneTransitionAnimation((Activity)context,
                                    holder.placeImage, "placeImage");
                    context.startActivity(intent, options.toBundle());
                } else {
                    context.startActivity(intent);
                }
            });

        } catch (Exception e) {
            Log.e("HomePlacesAdapter", "Error binding view", e);
        }
    }

    @NonNull
    @Override
    public PlaceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_place_display, parent, false);
        return new PlaceViewHolder(view);
    }

    static class PlaceViewHolder extends RecyclerView.ViewHolder {
        TextView nameText, typeText;
        ImageView placeImage;

        public PlaceViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.placeName);
            typeText = itemView.findViewById(R.id.placeType);
            placeImage = itemView.findViewById(R.id.placeImage);
        }
    }
}
