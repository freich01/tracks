package com.example.myapplicationsddddddddddddddd;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.List;

public class TrainingSession {
    @DocumentId
    private String id;
    private String userId;
    private String workoutPlanId;
    private String exerciseName;
    private List<Double> maxWeight;
    private List<Double> totalWeight;
    private List<Double> averageWeightPerRep;
    private List<String> trainingDate;
    private String exerciseId;

    public TrainingSession() {
        // default constructor is required by Firebase
        maxWeight = new ArrayList<>();
        totalWeight = new ArrayList<>();
        averageWeightPerRep = new ArrayList<>();
        trainingDate = new ArrayList<>();
    }

    public TrainingSession(String userId, String workoutPlanId, String exerciseId, String exerciseName, Double maxWeight, Double totalWeight, Double averageWeightPerRep, String trainingDate) {
        this();
        this.userId = userId;
        this.workoutPlanId = workoutPlanId;
        this.exerciseId = exerciseId;
        this.exerciseName = exerciseName;
        this.maxWeight.add(maxWeight);
        this.totalWeight.add(totalWeight);
        this.averageWeightPerRep.add(averageWeightPerRep);
        this.trainingDate.add(trainingDate);
    }

    @Exclude
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getWorkoutPlanId() {
        return workoutPlanId;
    }

    public void setWorkoutPlanId(String workoutPlanId) {
        this.workoutPlanId = workoutPlanId;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public List<Double> getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(List<Double> maxWeight) {
        this.maxWeight = maxWeight;
    }

    public List<Double> getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(List<Double> totalWeight) {
        this.totalWeight = totalWeight;
    }

    public List<Double> getAverageWeightPerRep() {
        return averageWeightPerRep;
    }

    public void setAverageWeightPerRep(List<Double> averageWeightPerRep) {
        this.averageWeightPerRep = averageWeightPerRep;
    }

    public List<String> getTrainingDate() {
        return trainingDate;
    }

    public void setTrainingDate(List<String> trainingDate) {
        this.trainingDate = trainingDate;
    }

    public String getExerciseId() {
        return exerciseId;
    }

    public void setExerciseId(String exerciseId) {
        this.exerciseId = exerciseId;
    }
}





