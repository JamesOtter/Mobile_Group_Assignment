package com.example.mobile_group_assignment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TravelPlanAdapter extends RecyclerView.Adapter<TravelPlanAdapter.ViewHolder> {

    List<TravelPlan> planList;

    public TravelPlanAdapter(List<TravelPlan> planList) {
        this.planList = planList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_travel_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TravelPlan plan = planList.get(position);
        holder.textDestination.setText(plan.getDestination());
        holder.textDate.setText(plan.getDate());
        holder.textNotes.setText(plan.getNotes());  // Corrected
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textDestination, textDate, textNotes;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textDestination = itemView.findViewById(R.id.textDestination);
            textDate = itemView.findViewById(R.id.textDate);
            textNotes = itemView.findViewById(R.id.textNotes);
        }
    }
}
