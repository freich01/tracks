package com.example.myapplicationsddddddddddddddd;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class ExerciseViewModel extends AndroidViewModel {
    // mostly a pass-through-class for the ExerciseRepository

    private ExerciseRepository mRepository;
    public ExerciseViewModel(Application application) {
        super(application);
        mRepository = new ExerciseRepository();
    }
    public LiveData<List<Exercise>> getExercisesByWorkoutPlan(String workoutPlanId) {
        return mRepository.getExercisesByWorkoutPlan(workoutPlanId);
    }

    public void insert(Exercise exercise, String workoutPlanId) {
        mRepository.insert(exercise, workoutPlanId);
    }

    public void update(Exercise exercise, String workoutPlanId) {
        mRepository.update(exercise, workoutPlanId);
    }

    public void delete(Exercise exercise, String workoutPlanId) {
        mRepository.delete(exercise, workoutPlanId);
    }
}



