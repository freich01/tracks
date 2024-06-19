package com.example.myapplicationsddddddddddddddd;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.TextView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ExerciseViewHolder extends RecyclerView.ViewHolder {
    private final TextView exerciseNameTextView;
    private final GridLayout gridLayout;
    private final TextView notesTextView;

    public ExerciseViewHolder(View itemView) {
        super(itemView);
        exerciseNameTextView = itemView.findViewById(R.id.exercise_name);
        gridLayout = itemView.findViewById(R.id.gridLayout);
        notesTextView = itemView.findViewById(R.id.notes);
    }

    public void bind(Exercise exercise) {
        exerciseNameTextView.setText(exercise.getName());
        notesTextView.setText(exercise.getNotes());

        gridLayout.removeAllViews(); // clear previous views

        List<String> weights = exercise.getWeights();
        List<String> reps = exercise.getReps();

        for (int i = 0; i < weights.size(); i++) {
            // create and add weight TextView
            TextView weightTextView = new TextView(itemView.getContext());
            weightTextView.setText(String.valueOf(weights.get(i)));
            weightTextView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.border_box));
            GridLayout.LayoutParams weightLayoutParams = new GridLayout.LayoutParams();
            weightLayoutParams.width = 0;
            weightLayoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            weightLayoutParams.setMargins(8, 8, 8, 8);
            weightTextView.setLayoutParams(weightLayoutParams);
            weightTextView.setPadding(32, 16, 0, 16);
            weightTextView.setTextSize(16);

            gridLayout.addView(weightTextView);

            // create and add reps TextView
            TextView repsTextView = new TextView(itemView.getContext());
            repsTextView.setText(String.valueOf(reps.get(i)));
            repsTextView.setBackground(ContextCompat.getDrawable(itemView.getContext(), R.drawable.border_box));

            GridLayout.LayoutParams repsLayoutParams = new GridLayout.LayoutParams();
            repsLayoutParams.width = 0;
            repsLayoutParams.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            repsLayoutParams.setMargins(8, 8, 8, 8);
            repsTextView.setLayoutParams(repsLayoutParams);
            repsTextView.setPadding(32, 16, 0, 16);
            repsTextView.setTextSize(16);

            gridLayout.addView(repsTextView);
        }
    }

    public static ExerciseViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new ExerciseViewHolder(view);
    }
}








