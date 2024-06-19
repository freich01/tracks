package com.example.myapplicationsddddddddddddddd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanAdapter extends RecyclerView.Adapter<WorkoutPlanAdapter.WorkoutPlanViewHolder> {

    private final OnWorkoutPlanDeleteListener deleteListener;
    private final OnWorkoutPlanUpdateListener updateListener;
    private List<WorkoutPlan> workoutPlans = new ArrayList<>();
    private SharedPreferences sharedPreferences;
    private OnWorkoutPlanClickListener clickListener;
    private String changeName;

    public WorkoutPlanAdapter(OnWorkoutPlanDeleteListener deleteListener, OnWorkoutPlanUpdateListener updateListener) {
        this.deleteListener = deleteListener;
        this.updateListener = updateListener;
    }

    @NonNull
    @Override
    public WorkoutPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.workout_plan_item, parent, false);
        return new WorkoutPlanViewHolder(itemView, deleteListener, updateListener);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkoutPlanViewHolder holder, int position) {
        WorkoutPlan currentWorkoutPlan = workoutPlans.get(position);
        holder.bind(currentWorkoutPlan);
    }

    @Override
    public int getItemCount() {
        return workoutPlans.size();
    }

    public void setWorkoutPlans(List<WorkoutPlan> workoutPlans) {
        this.workoutPlans = workoutPlans;
        notifyDataSetChanged();
    }

    public void setOnWorkoutPlanClickListener(OnWorkoutPlanClickListener listener) {
        this.clickListener = listener;
    }

    public class WorkoutPlanViewHolder extends RecyclerView.ViewHolder {
        private final Button workoutPlanButton;
        private final TextView dateTextView;
        private final OnWorkoutPlanDeleteListener deleteListener;
        private final OnWorkoutPlanUpdateListener updateListener;

        public WorkoutPlanViewHolder(@NonNull View itemView, OnWorkoutPlanDeleteListener deleteListener, OnWorkoutPlanUpdateListener updateListener) {
            super(itemView);
            this.deleteListener = deleteListener;
            this.updateListener = updateListener;
            workoutPlanButton = itemView.findViewById(R.id.workout_plan_button);
            dateTextView = itemView.findViewById(R.id.date_text_view);
        }

        public void bind(WorkoutPlan workoutPlan) {
            workoutPlanButton.setText(workoutPlan.getName());
            workoutPlanButton.setOnClickListener(v -> {
                Context context = itemView.getContext();
                Intent intent = new Intent(context, WorkoutPlanActivity.class);
                intent.putExtra("workoutPlanId", workoutPlan.getId());
                intent.putExtra("workoutPlanName", workoutPlan.getName());
                Log.d("WorkoutPlanAdapter", "WorkoutPlanId sent: " + workoutPlan.getId());
                context.startActivity(intent);
            });

            workoutPlanButton.setOnLongClickListener(v -> {
                showActionDialog(workoutPlan);
                return true;
            });

            sharedPreferences = itemView.getContext().getSharedPreferences("DateLastTrained", Context.MODE_PRIVATE);
            String dateLastTrained = sharedPreferences.getString(String.valueOf(workoutPlan.getId()), "Date last trained:-");
            dateTextView.setText(dateLastTrained);
        }

        private void showActionDialog(WorkoutPlan workoutPlan) {
            // kind of like a popup window (long click)
            Context context = itemView.getContext();
            new AlertDialog.Builder(context)
                    .setTitle("Manage Workout Plan")
                    .setMessage("What would you like to do with this workout plan?")
                    .setPositiveButton("Change Name", (dialog, which) -> updateNameWorkoutPlanDialog(workoutPlan))
                    .setNegativeButton("Delete", (dialog, which) -> deleteWorkoutPlanConfirmation(workoutPlan))
                    .setNeutralButton("Cancel", null)
                    .show();
        }

        private void deleteWorkoutPlanConfirmation(WorkoutPlan workoutPlan) {
            // confirms if the user is really sure about deleting the workout plan
            Context context = itemView.getContext();
            new AlertDialog.Builder(context)
                    .setTitle("Delete Workout Plan")
                    .setMessage("Are you sure you want to delete this workout plan and all its exercises as well as statistical entries??")
                    .setNegativeButton("Yes", (dialog, which) -> deleteWorkoutPlan(workoutPlan))
                    .setNeutralButton("No", null)
                    .show();
        }

        private void deleteWorkoutPlan(WorkoutPlan workoutPlan) {
            // user was really sure..
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // remove from RecyclerView
                workoutPlans.remove(position);
                notifyItemRemoved(position);
                // remove from the database
                deleteListener.onWorkoutPlanDelete(workoutPlan);
            }
        }

        private void updateNameWorkoutPlanDialog(WorkoutPlan workoutPlan) {
            Context context = itemView.getContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Enter new workout plan name");

            // set up the input
            final EditText input = new EditText(context);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Save", (dialog, which) -> {
                changeName = input.getText().toString();
                workoutPlan.setName(changeName);
                updateNameWorkoutPlan(workoutPlan);

            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }

        private void updateNameWorkoutPlan(WorkoutPlan workoutPlan) {
            int position = getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                // update the database
                updateListener.onWorkoutPlanUpdate(workoutPlan);
                notifyItemChanged(position);
            }
        }
    }

    public interface OnWorkoutPlanDeleteListener {
        void onWorkoutPlanDelete(WorkoutPlan workoutPlan);
    }

    public interface OnWorkoutPlanUpdateListener {
        void onWorkoutPlanUpdate(WorkoutPlan workoutPlan);
    }

    public interface OnWorkoutPlanClickListener {
        void onWorkoutPlanClick(WorkoutPlan workoutPlan);
    }
}

