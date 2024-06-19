package com.example.myapplicationsddddddddddddddd;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class TrainingSessionViewModel extends ViewModel {
    private static final String TAG = "TrainingSessionViewModel";
    private final MutableLiveData<List<TrainingSession>> workoutPlanTrainingSessions = new MutableLiveData<>(new ArrayList<>());
    private TrainingSessionRepository repository;
    private FirebaseFirestore db;

    public TrainingSessionViewModel() {
        db = FirebaseFirestore.getInstance();
        repository = new TrainingSessionRepository();
    }

    public LiveData<List<TrainingSession>> getWorkoutPlanTrainingSessions() {
        return workoutPlanTrainingSessions;
    }

    public void loadWorkoutPlan(String workoutPlanName) {
        String userId = repository.getUserId();
        if (workoutPlanName.equals("All")) {
            LiveData<List<TrainingSession>> allSessions = repository.getTrainingSessionsByUserId(userId);
            allSessions.observeForever(sessions -> {
                workoutPlanTrainingSessions.setValue(sessions);
                Log.d(TAG, "Loading all training sessions");
            });
        } else {
            // get workoutplan id
            db.collection("workout_plans")
                    .whereEqualTo("name", workoutPlanName)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful() || task.getResult().isEmpty()) {
                            Log.w(TAG, "No matching workout plan found.");
                            workoutPlanTrainingSessions.setValue(null);
                            return;
                        }

                        List<WorkoutPlan> workoutPlans = task.getResult().toObjects(WorkoutPlan.class);
                        if (!workoutPlans.isEmpty()) {
                            String id = workoutPlans.get(0).getId();
                            Log.d(TAG, "Loading training sessions for workout plan ID: " + id);
                            LiveData<List<TrainingSession>> sessions = repository.getWorkoutPlan(id);
                            sessions.observeForever(trainingSessions -> {
                                workoutPlanTrainingSessions.setValue(trainingSessions);
                            });
                        } else {
                            Log.w(TAG, "Workout plan list is empty.");
                            workoutPlanTrainingSessions.setValue(null);
                        }
                    });
        }
    }

    public LiveData<TrainingSession> getTrainingSessionByExerciseId(String exerciseId) {
        return repository.getTrainingSessionByExerciseId(exerciseId);
    }

    public void insert(TrainingSession trainingSession) {
        repository.insert(trainingSession);
    }

    public void update(TrainingSession trainingSession) {
        repository.update(trainingSession);
    }

    public void deleteExercise(String workoutPlanId, String exerciseName) {
        repository.deleteExercise(workoutPlanId, exerciseName);
    }

    public void getWorkoutPlanTimeframe(String currentName, String selectedTimeframe) {
        // this method was revised by ChatGPT because it had some problems displaying the right timeframe
        String userId = repository.getUserId();
        Log.d("UserId", userId);

        if (currentName.equals("All")) {
            // get all training sessions for current user
            db.collection("training_sessions")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful() || task.getResult().isEmpty()) {
                            Log.w(TAG, "No matching workout plan found.");
                            workoutPlanTrainingSessions.setValue(null);
                            return;
                        }
                        // convert result into list of TrainingSession objects
                        List<TrainingSession> trainingSessionsNames = task.getResult().toObjects(TrainingSession.class);
                        if (!trainingSessionsNames.isEmpty()) {
                            if (selectedTimeframe.equals("All")) {
                                LiveData<List<TrainingSession>> allSessions = repository.getTrainingSessionsByUserId(userId);
                                if (allSessions != null) {
                                    allSessions.observeForever(sessions -> {
                                        // update LiveData with all TrainingSessions
                                        workoutPlanTrainingSessions.setValue(sessions);
                                        Log.d(TAG, "Loading all training sessions");
                                    });
                                } else {
                                    Log.w(TAG, "All sessions LiveData is null");
                                    workoutPlanTrainingSessions.setValue(null);
                                }
                            } else {
                                // get TrainingSessions within selected Timeframe
                                LiveData<List<TrainingSession>> sessions = null;
                                try {
                                    sessions = repository.getTimeFrame(trainingSessionsNames, selectedTimeframe);
                                } catch (ParseException e) {
                                    throw new RuntimeException(e);
                                }
                                if (sessions != null) {
                                    sessions.observeForever(trainingSessions -> {
                                        workoutPlanTrainingSessions.setValue(trainingSessions);
                                    });
                                } else {
                                    Log.w(TAG, "Sessions LiveData is null");
                                    workoutPlanTrainingSessions.setValue(null);
                                }
                            }
                        } else {
                            Log.w(TAG, "Workout plan list is empty.");
                            workoutPlanTrainingSessions.setValue(null);
                        }
                    });
        } else {
            // get specific workoutplans
            db.collection("workout_plans")
                    .whereEqualTo("name", currentName)
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful() || task.getResult().isEmpty()) {
                            Log.w(TAG, "No matching workout plan found.");
                            workoutPlanTrainingSessions.setValue(null);
                            return;
                        }
                        // convert result into a list of WorkoutPlan objects
                        List<WorkoutPlan> workoutPlans = task.getResult().toObjects(WorkoutPlan.class);
                        if (!workoutPlans.isEmpty()) {
                            // get wop id
                            String id = workoutPlans.get(0).getId();
                            Log.d(TAG, "Loading training sessions for workout plan ID: " + id);
                            LiveData<List<TrainingSession>> sessions = repository.getWorkoutPlan(id);
                            sessions.observeForever(trainingSessions -> {
                                // update LiveData with just the TrainingSessions for the specific wop
                                workoutPlanTrainingSessions.setValue(trainingSessions);
                            });
                        } else {
                            Log.w(TAG, "Workout plan list is empty.");
                            workoutPlanTrainingSessions.setValue(null);
                        }
                    });

        }
    }

    // not used atm, kept because it could be useful in the future
    /*
    public LiveData<Map<String, List<TrainingSession>>> getGroupedByExerciseId() {
        MutableLiveData<Map<String, List<TrainingSession>>> groupedData = new MutableLiveData<>();

        workoutPlanTrainingSessions.observeForever(trainingSessions -> {
            Map<String, List<TrainingSession>> groupedMap = new HashMap<>();

            for (TrainingSession session : trainingSessions) {
                String exerciseId = session.getExerciseId();
                if (!groupedMap.containsKey(exerciseId)) {
                    groupedMap.put(exerciseId, new ArrayList<>());
                }
                groupedMap.get(exerciseId).add(session);
            }

            groupedData.setValue(groupedMap);
        });

        return groupedData;
    }

     */
}



