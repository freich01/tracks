package com.example.myapplicationsddddddddddddddd;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ExerciseListAdapter extends ListAdapter<Exercise, ExerciseViewHolder> {

    private OnItemClickListener listener;
    private Context context;
    private SharedPreferences sharedPreferences;
    private String savedDateString;

    public ExerciseListAdapter(@NonNull DiffUtil.ItemCallback<Exercise> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public ExerciseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return ExerciseViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull ExerciseViewHolder holder, int position) {
        Exercise current = getItem(position);
        holder.bind(current);

        // background of the exercise is set following this schema:
        // not trained today -> almost white
        // not trained today and last training -> grey
        // trained today -> green
        if (isTrainedToday(current.getDateLastTrained())) {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.green_background));
        } else if (isTrainedLastTraining(current.getDateLastTrained(), position)) {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.light_grey_background));
        } else {
            holder.itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.grey_background));
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(current);
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    private boolean isTrainedToday(Date dateLastTrained) {
        // necessary for green/almost white background
        if (dateLastTrained == null) {
            return false;
        }

        Calendar today = Calendar.getInstance();
        Calendar lastTrained = Calendar.getInstance();
        lastTrained.setTime(dateLastTrained);

        return today.get(Calendar.YEAR) == lastTrained.get(Calendar.YEAR)
                && today.get(Calendar.MONTH) == lastTrained.get(Calendar.MONTH)
                && today.get(Calendar.DAY_OF_MONTH) == lastTrained.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isTrainedLastTraining(Date dateLastTrained, int position) {

        Exercise current = getItem(position);

        if (dateLastTrained == null) {
            return false;
        }

        if (savedDateString == null) {
            sharedPreferences = context.getSharedPreferences("DateLastTrained", Context.MODE_PRIVATE);
            savedDateString = sharedPreferences.getString(String.valueOf(current.getWorkoutPlanId()) + "before", "");

            // Remove "Date last trained: " from String
            if (savedDateString.startsWith("Date last trained: ")) {
                savedDateString = savedDateString.replace("Date last trained: ", "");
                Log.d("savedDateString", savedDateString);
            }
        }

        // cast the String to a date
        Calendar dateLastTrainedWorkoutPlan = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd/MM/yy", Locale.ENGLISH);
        if (!savedDateString.equals("")) {

            try {
                dateLastTrainedWorkoutPlan.setTime(formatter.parse(savedDateString));

            } catch (ParseException e) {
                throw new RuntimeException(e);
            }

            Calendar lastTrained = Calendar.getInstance();
            lastTrained.setTime(dateLastTrained);

            return dateLastTrainedWorkoutPlan.get(Calendar.YEAR) == lastTrained.get(Calendar.YEAR)
                    && dateLastTrainedWorkoutPlan.get(Calendar.MONTH) == lastTrained.get(Calendar.MONTH)
                    && dateLastTrainedWorkoutPlan.get(Calendar.DAY_OF_MONTH) == lastTrained.get(Calendar.DAY_OF_MONTH);
        }
        return true;
    }

    public interface OnItemClickListener {
        void onItemClick(Exercise exercise);
    }

    public static class ExerciseDiff extends DiffUtil.ItemCallback<Exercise> {

        @Override
        public boolean areItemsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
            // Ensure that both items and their ids are not null before comparing
            if (oldItem == null || newItem == null || oldItem.getId() == null || newItem.getId() == null) {
                return false;
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Exercise oldItem, @NonNull Exercise newItem) {
            // Ensure that both items are not null before comparing their contents
            if (oldItem == null || newItem == null) {
                return false;
            }
            return oldItem.equals(newItem);
        }
    }
}





