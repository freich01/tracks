package com.example.myapplicationsddddddddddddddd;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class WorkoutPlanViewModel extends AndroidViewModel {

    private WorkoutPlanRepository mRepository;
    private MutableLiveData<List<WorkoutPlan>> mAllWorkoutPlans;

    public WorkoutPlanViewModel(Application application) {
        super(application);
        mRepository = new WorkoutPlanRepository();
        mAllWorkoutPlans = mRepository.getAllWorkoutPlans();
    }

    public LiveData<List<WorkoutPlan>> getAllWorkoutPlans() {
        return mAllWorkoutPlans;
    }

    public void insert(WorkoutPlan workoutPlan) {
        mRepository.insert(workoutPlan);
    }

    public void deleteWorkoutPlan(WorkoutPlan workoutPlan) {
        mRepository.delete(workoutPlan);
    }

    public void setWorkoutPlans(List<WorkoutPlan> workoutPlans) {
        mAllWorkoutPlans.setValue(workoutPlans);
    }

    public void removeListener() {
        mRepository.removeListener();
    }

    public void updateWorkoutPlan(WorkoutPlan workoutPlan) {
        mRepository.updateWorkout(workoutPlan);
    }
}


