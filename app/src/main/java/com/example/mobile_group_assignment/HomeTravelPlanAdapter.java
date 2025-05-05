package com.example.mobile_group_assignment;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HomeTravelPlanAdapter extends RecyclerView.Adapter<HomeTravelPlanAdapter.ViewHolder> {
    private List<TravelPlan> planList;
    private Context context;
    private OnItemDeleteListener onItemDeleteListener;
    private OnItemEditListener onItemEditListener;

    public interface OnItemDeleteListener {
        void onItemDelete(int position);
    }

    public interface OnItemEditListener {
        void onItemEdit(int position);
    }

    public HomeTravelPlanAdapter(List<TravelPlan> planList, Context context) {
        this.planList = planList;
        this.context = context;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener listener) {
        this.onItemDeleteListener = listener;
    }

    public void setOnItemEditListener(OnItemEditListener listener) {
        this.onItemEditListener = listener;
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

        holder.destinationTextView.setText(plan.getDestination());
        holder.startDateTextView.setText(plan.getStartDate());
        holder.endDateTextView.setText(plan.getEndDate());

        holder.btnDelete.setOnClickListener(v -> {
            if (onItemDeleteListener != null) {
                onItemDeleteListener.onItemDelete(position);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(context, ManageTravelPlanActivity.class);
            intent.putExtra("editMode", true);
            intent.putExtra("position", position);
            intent.putExtra("selectedState", plan.getDestination());
            intent.putExtra("startDate", plan.getStartDate());
            intent.putExtra("endDate", plan.getEndDate());
            intent.putExtra("selectedBudget", plan.getBudgetRange());
            intent.putExtra("selectedCategories", plan.getTravelType());
            intent.putExtra("dayCards", plan.getDayCardsJson()); // MOST IMPORTANT
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, TravelPlanDetailActivity.class);
            intent.putExtra("destination", plan.getDestination());
            intent.putExtra("startDate", plan.getStartDate());
            intent.putExtra("endDate", plan.getEndDate());
            intent.putExtra("budgetRange", plan.getBudgetRange());
            intent.putExtra("travelType", plan.getTravelType());
            intent.putExtra("dayCards", plan.getDayCardsJson());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return planList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView destinationTextView, startDateTextView, endDateTextView;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(View itemView) {
            super(itemView);
            destinationTextView = itemView.findViewById(R.id.textViewDestination);
            startDateTextView = itemView.findViewById(R.id.textViewStartDate);
            endDateTextView = itemView.findViewById(R.id.textViewEndDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}