package com.example.myapplicationsddddddddddddddd;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class TrainingSessionListAdapter extends ListAdapter<TrainingSession, TrainingSessionViewHolder> {

    private OnItemClickListener listener;
    private String selectedType = "Max weight";
    private TrainingSessionRepository mRepository;
    private Context context;
    FirebaseAuth auth;

    public TrainingSessionListAdapter(@NonNull DiffUtil.ItemCallback<TrainingSession> diffCallback, Context context) {
        super(diffCallback);
        this.context = context;
        mRepository = new TrainingSessionRepository();
        auth = FirebaseAuth.getInstance();
    }

    @NonNull
    @Override
    public TrainingSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return TrainingSessionViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull TrainingSessionViewHolder holder, int position) {
        String exerciseName = getItem(position).getExerciseName();
        List<TrainingSession> exerciseSessions = new ArrayList<>();

        // collect all sessions with the same exercise name
        for (int i = 0; i < getItemCount(); i++) {
            TrainingSession session = getItem(i);
            if (session.getExerciseName().equals(exerciseName)) {
                exerciseSessions.add(session);
            }
        }

        // bind them to the view holder for display in statistics
        holder.bind(exerciseSessions, selectedType);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(getItem(position));
            }
        });
    }

    public interface OnItemClickListener {
        void onItemClick(TrainingSession trainingSession);
    }

    public static class TrainingSessionDiff extends DiffUtil.ItemCallback<TrainingSession> {

        @Override
        public boolean areItemsTheSame(@NonNull TrainingSession oldItem, @NonNull TrainingSession newItem) {
            if (oldItem == null || newItem == null || oldItem.getId() == null || newItem.getId() == null) {
                return false;
            }
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull TrainingSession oldItem, @NonNull TrainingSession newItem) {
            if (oldItem == null || newItem == null) {
                return false;
            }
            return oldItem.equals(newItem);
        }
    }

    public void setSelectedType(String selectedType) {
        this.selectedType = selectedType;
        notifyDataSetChanged();
    }
}








