package com.example.myapplicationsddddddddddddddd;

import com.google.firebase.firestore.DocumentId;

import java.io.Serializable;

public class WorkoutPlan implements Serializable {
    @DocumentId
    private String id;
    private String name;
    private String userId;

    public WorkoutPlan() {
        // required for Firebase
    }

    public WorkoutPlan(String userId, String id, String name) {
        this.userId = userId;
        this.id = id;
        this.name = name;

    }

    public WorkoutPlan(String userId, String name) {
        this.userId = userId;
        this.name = name;

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

