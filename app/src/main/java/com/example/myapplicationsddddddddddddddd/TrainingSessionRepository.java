package com.example.myapplicationsddddddddddddddd;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TrainingSessionRepository {
    private static final String TAG = "TrainingSessionRepository";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String userId;

    public TrainingSessionRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        userId = auth.getCurrentUser().getUid();
    }

    public String getUserId() {
        return userId;
    }

    // not used atm
    public LiveData<List<TrainingSession>> getAllTrainingSessions() {
        MutableLiveData<List<TrainingSession>> allTrainingSessions = new MutableLiveData<>();
        db.collection("training_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TrainingSession> trainingSessions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TrainingSession trainingSession = document.toObject(TrainingSession.class);
                            trainingSessions.add(trainingSession);
                        }
                        allTrainingSessions.setValue(trainingSessions);
                    } else {
                        Log.e(TAG, "Error getting training sessions: ", task.getException());
                    }
                });
        return allTrainingSessions;
    }

    public LiveData<TrainingSession> getTrainingSessionByExerciseId(String exerciseId) {
        MutableLiveData<TrainingSession> trainingSession = new MutableLiveData<>();
        db.collection("training_sessions")
                .whereEqualTo("exerciseId", exerciseId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TrainingSession session = document.toObject(TrainingSession.class);
                            session.setId(document.getId());
                            trainingSession.setValue(session);
                            break; // only one document per exerciseId
                        }
                    } else {
                        trainingSession.setValue(null); // no existing session was found in the database
                    }
                });
        return trainingSession;
    }

    public void update(TrainingSession trainingSession) {
        if (trainingSession.getId() == null) {
            throw new IllegalArgumentException("TrainingSession ID cannot be null for update");
        }
        db.collection("training_sessions").document(trainingSession.getId()).set(trainingSession)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Training session updated"))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating training session", e));
    }

    public LiveData<List<TrainingSession>> getWorkoutPlan(String workoutPlanId) {
        // as the workoutPlanId is (hopefully) distinct this data will only get displayed to the user that has created the workoutPlan in the first place
        MutableLiveData<List<TrainingSession>> workoutPlanSessions = new MutableLiveData<>();
        db.collection("training_sessions")
                .whereEqualTo("workoutPlanId", workoutPlanId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TrainingSession> sessions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TrainingSession session = document.toObject(TrainingSession.class);
                            sessions.add(session);
                        }
                        workoutPlanSessions.setValue(sessions);
                    } else {
                        Log.e(TAG, "Error getting training sessions for workout plan ID: " + workoutPlanId, task.getException());
                    }
                });
        return workoutPlanSessions;
    }

    public void insert(TrainingSession trainingSession) {
        // TrainingSession is inserted after checking that no Session exists for this exerciseId (other method)
        db.collection("training_sessions").add(trainingSession)
                .addOnSuccessListener(documentReference -> {
                    trainingSession.setId(documentReference.getId());
                    db.collection("training_sessions").document(trainingSession.getId()).set(trainingSession)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "Training session inserted"))
                            .addOnFailureListener(e -> Log.e(TAG, "Error inserting training session", e));
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error adding training session", e));
    }

    public void deleteExercise(String workoutPlanId, String exerciseName) {
        db.collection("training_sessions")
                .whereEqualTo("workoutPlanId", workoutPlanId)
                .whereEqualTo("exerciseName", exerciseName)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            db.collection("training_sessions").document(document.getId()).delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "DocumentSnapshot successfully deleted!"))
                                    .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                        }
                    } else {
                        Log.e(TAG, "Error getting documents for deletion by workoutPlanId and exerciseName: ", task.getException());
                    }
                });
    }

    public LiveData<List<TrainingSession>> getTrainingSessionsByUserId(String userId) {
        MutableLiveData<List<TrainingSession>> trainingSessions = new MutableLiveData<>();
        db.collection("training_sessions")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<TrainingSession> sessions = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            TrainingSession session = document.toObject(TrainingSession.class);
                            sessions.add(session);
                        }
                        trainingSessions.setValue(sessions);
                    } else {
                        Log.e(TAG, "Error getting training sessions by userId: ", task.getException());
                    }
                });
        return trainingSessions;
    }

    public LiveData<List<TrainingSession>> getTimeFrame(List<TrainingSession> trainingSessionsNames, String selectedTimeFrame) throws ParseException {
        // when selecting a timeframe in Statistics, this method makes sure that only the dates in the timeframe will get displayed
        MutableLiveData<List<TrainingSession>> returnValue = new MutableLiveData<>();
        List<TrainingSession> filteredSessions = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();
        LocalDate lastPossibleDate;

        if (selectedTimeFrame.equals("All")) {
            returnValue.setValue(trainingSessionsNames);
        } else if (selectedTimeFrame.equals("Last 7 days")) {
            lastPossibleDate = currentDate.minusDays(7);

            for (TrainingSession session : trainingSessionsNames) {
                for (String dateString : session.getTrainingDate()) {
                    LocalDate date = LocalDate.parse(dateString, DATE_FORMAT);
                    if (date.isAfter(lastPossibleDate) || date.isEqual(lastPossibleDate)) {
                        filteredSessions.add(session);
                        break;
                    }
                }
            }
            returnValue.setValue(filteredSessions);

        } else if (selectedTimeFrame.equals("Last 30 days")) {
            lastPossibleDate = currentDate.minusDays(30);

            for (TrainingSession session : trainingSessionsNames) {
                for (String dateString : session.getTrainingDate()) {
                    LocalDate date = LocalDate.parse(dateString, DATE_FORMAT);
                    if (date.isAfter(lastPossibleDate) || date.isEqual(lastPossibleDate)) {
                        filteredSessions.add(session);
                        break;
                    }
                }
            }
            returnValue.setValue(filteredSessions);
        } else if (selectedTimeFrame.equals("Last year")) {
            lastPossibleDate = currentDate.minusDays(365);

            for (TrainingSession session : trainingSessionsNames) {
                for (String dateString : session.getTrainingDate()) {
                    LocalDate date = LocalDate.parse(dateString, DATE_FORMAT);
                    if (date.isAfter(lastPossibleDate) || date.isEqual(lastPossibleDate)) {
                        filteredSessions.add(session);
                        break;
                    }
                }
            }
            returnValue.setValue(filteredSessions);
        }

        return returnValue;
    }
}

