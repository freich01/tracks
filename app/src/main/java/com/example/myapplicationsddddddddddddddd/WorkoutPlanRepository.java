package com.example.myapplicationsddddddddddddddd;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanRepository {
    private FirebaseFirestore db;
    private MutableLiveData<List<WorkoutPlan>> mAllWorkoutPlans;
    private ListenerRegistration listenerRegistration;
    private FirebaseAuth auth;
    private String userId;

    public WorkoutPlanRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mAllWorkoutPlans = new MutableLiveData<>();
        loadAllWorkoutPlans();
    }

    private void loadAllWorkoutPlans() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
        listenerRegistration = db.collection("workout_plans")
                .whereEqualTo("userId", userId) // required so it does not load all plans from all users
                .addSnapshotListener((snapshots, e) -> {
            if (e != null) {
                return;
            }

            if (snapshots != null) {
                List<WorkoutPlan> workoutPlans = new ArrayList<>();
                for (QueryDocumentSnapshot document : snapshots) {
                    WorkoutPlan workoutPlan = document.toObject(WorkoutPlan.class);
                    workoutPlans.add(workoutPlan);
                }
                mAllWorkoutPlans.setValue(workoutPlans);
            }
        });
    }

    public MutableLiveData<List<WorkoutPlan>> getAllWorkoutPlans() {
        return mAllWorkoutPlans;
    }

    public void insert(WorkoutPlan workoutPlan) {
        db.collection("workout_plans").add(workoutPlan);
    }

    public void delete(WorkoutPlan workoutPlan) {
        // delete from workout_plans
        db.collection("workout_plans").document(workoutPlan.getId()).delete();
        // delete from training_sessions so it will get removed from statistics
        db.collection("training_sessions")
                .whereEqualTo("workoutPlanId", workoutPlan.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("training_sessions").document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("FirestoreHelper", "Training Session successfully deleted!"))
                                        .addOnFailureListener(e ->
                                                Log.w("FirestoreHelper", "Error deleting Training Session", e));
                            }
                        } else {
                            Log.w("FirestoreHelper", "Error getting Training Sessions: ", task.getException());
                        }
                    }
                });

        // do the same for exercises (query then delete individually)
        db.collection("exercises")
                .whereEqualTo("workoutPlanId", workoutPlan.getId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                db.collection("exercises").document(document.getId()).delete()
                                        .addOnSuccessListener(aVoid ->
                                                Log.d("FirestoreHelper", "Exercise successfully deleted!"))
                                        .addOnFailureListener(e ->
                                                Log.w("FirestoreHelper", "Error deleting Exercise", e));
                            }
                        } else {
                            Log.w("FirestoreHelper", "Error getting Exercises: ", task.getException());
                        }
                    }
                });

    }


    public void removeListener() {
        if (listenerRegistration != null) {
            listenerRegistration.remove();
        }
    }

    public void updateWorkout(WorkoutPlan workoutPlan) {
        // just pass through the input to substitute the current for the wop (name change)
        db.collection("workout_plans").document(workoutPlan.getId()).set(workoutPlan);
    }
}


