package com.example.mobile_group_assignment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TravelPlanAdapter extends RecyclerView.Adapter<TravelPlanAdapter.ViewHolder> {

    private List<TravelPlan> planList;
    private OnItemClickListener onItemClickListener;

    // Constructor
    public TravelPlanAdapter(List<TravelPlan> planList) {
        this.planList = planList;
    }

    // Set the item click listener
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the item layout for each travel plan
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_travel_plan, parent, false);
        return new ViewHolder(view);  // Create a non-static ViewHolder
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (planList != null && !planList.isEmpty()) {
            TravelPlan plan = planList.get(position);

            // Log to verify the content of the list
            Log.d("TravelPlanAdapter", "Binding plan: " + plan.getDestination());

            holder.destinationTextView.setText(plan.getDestination());
            holder.startDateTextView.setText(plan.getStartDate());
            holder.endDateTextView.setText(plan.getEndDate());
        }
    }

    @Override
    public int getItemCount() {
        return planList != null ? planList.size() : 0;  // Return 0 if the list is null or empty
    }

    // Remove static from the ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView destinationTextView, startDateTextView, endDateTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            destinationTextView = itemView.findViewById(R.id.textViewDestination);
            startDateTextView = itemView.findViewById(R.id.textViewStartDate);
            endDateTextView = itemView.findViewById(R.id.textViewEndDate);

            // Handle item click
            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(getAdapterPosition());
                }
            });
        }
    }

    // Interface for item click listener
    public interface OnItemClickListener {
        void onItemClick(int position);
    }
}
