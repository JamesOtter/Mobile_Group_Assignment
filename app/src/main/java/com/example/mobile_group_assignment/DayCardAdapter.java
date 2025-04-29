package com.example.mobile_group_assignment;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import java.util.List;

public class DayCardAdapter extends RecyclerView.Adapter<DayCardAdapter.DayCardViewHolder> {

    private List<DayCard> dayCardList;
    private Context context;

    public DayCardAdapter(List<DayCard> dayCardList, Context context) {
        this.dayCardList = dayCardList;
        this.context = context;
    }

    @Override
    public DayCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_day_card, parent, false);
        return new DayCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DayCardViewHolder holder, int position) {
        DayCard dayCard = dayCardList.get(position);
        holder.dayTitle.setText(dayCard.getTitle());

        List<String> selectedPlaces = dayCard.getSelectedPlaces();
        if (selectedPlaces != null && !selectedPlaces.isEmpty()) {
            holder.selectedPlace.setText(TextUtils.join(", ", selectedPlaces));
        } else {
            holder.selectedPlace.setText("No places selected");
        }

        List<String> selectedPlaceImageUrls = dayCard.getSelectedPlaceImageUrls();
        if (selectedPlaceImageUrls != null && !selectedPlaceImageUrls.isEmpty()) {
            ImagePagerAdapter imagePagerAdapter = new ImagePagerAdapter(selectedPlaceImageUrls, context);
            holder.imageViewPager.setAdapter(imagePagerAdapter);
        }

        // 🔥 FIX: Make the WHOLE card clickable, including ViewPager2
        holder.itemView.setOnClickListener(v -> {
            if (context instanceof ManageTravelPlanActivity) {
                ((ManageTravelPlanActivity) context).openPlaceSelectionDialog(position);
            }
        });

        // 🔥 Make ViewPager2 itself clickable too
        holder.imageViewPager.setOnClickListener(v -> {
            if (context instanceof ManageTravelPlanActivity) {
                ((ManageTravelPlanActivity) context).openPlaceSelectionDialog(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dayCardList.size();
    }

    public static class DayCardViewHolder extends RecyclerView.ViewHolder {
        TextView dayTitle, selectedPlace;
        ViewPager2 imageViewPager;

        public DayCardViewHolder(View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.dayTitle);
            selectedPlace = itemView.findViewById(R.id.selectedPlace);
            imageViewPager = itemView.findViewById(R.id.imageViewPager);
        }
    }
}
