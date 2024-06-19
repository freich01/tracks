package com.example.myapplicationsddddddddddddddd;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ExerciseRepository {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    private MutableLiveData<List<Exercise>> exercisesLiveData = new MutableLiveData<>();
    private String userId;

    public ExerciseRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public MutableLiveData<List<Exercise>> getExercisesByWorkoutPlan(String workoutPlanId) {
        fetchExercises(workoutPlanId);
        return exercisesLiveData;
    }

    private void fetchExercises(String workoutPlanId) {
        // because the workoutPlanId is unique (at least I hope so) it is possible to leave out the userId for this query
        db.collection("exercises")
                .whereEqualTo("workoutPlanId", workoutPlanId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Exercise> exercises = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Exercise exercise = document.toObject(Exercise.class);
                            exercise.setId(document.getId());
                            exercises.add(exercise);
                        }
                        fetchMultiTrainExercises(exercises, workoutPlanId);
                    }
                });
    }

    private void fetchMultiTrainExercises(List<Exercise> exercises, String workoutPlanId) {

        db.collection("exercises")
                .whereEqualTo("multiTrain", true)
                .whereEqualTo("workoutPlanId", workoutPlanId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Exercise exercise = document.toObject(Exercise.class);
                            exercise.setId(document.getId());
                            // avoid adding duplicates
                            if (!exercises.contains(exercise)) {
                                exercises.add(exercise);
                            }
                        }
                        exercisesLiveData.setValue(exercises);
                    }
                });
    }

    public void insert(Exercise exercise, String workoutPlanId) {
        db.collection("exercises").add(exercise).addOnSuccessListener(documentReference -> {
            // set the exercise id that was assigned when inserting
            exercise.setId(documentReference.getId());
            db.collection("exercises").document(documentReference.getId()).set(exercise)
                    .addOnSuccessListener(aVoid -> {
                        fetchExercises(workoutPlanId); // refresh exercises after inserting
                    });
        });
    }

    public void update(Exercise exercise, String workoutPlanId) {
        db.collection("exercises").document(exercise.getId()).set(exercise).addOnSuccessListener(aVoid -> {
            fetchExercises(workoutPlanId); // refresh exercises after updating

        });
    }

    public void delete(Exercise exercise, String workoutPlanId) {
        db.collection("exercises").document(exercise.getId()).delete().addOnSuccessListener(aVoid -> {
            fetchExercises(workoutPlanId); // refresh exercises after deleting
        });
    }
}





