package com.example.myapplicationsddddddddddddddd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WorkoutPlanActivity extends AppCompatActivity {
    private static final int EDIT_EXERCISE_REQUEST_CODE = 2;
    private static final int NEW_EXERCISE_ACTIVITY_REQUEST_CODE = 1;
    private TrainingSessionViewModel trainingSessionViewModel;
    private ExerciseListAdapter adapter;
    private ExerciseViewModel exerciseViewModel;
    private String workoutPlanId, workoutPlanName,userId;
    FirebaseAuth auth;
    androidx.appcompat.app.ActionBar actionBar;
    private Button homeBottomButton, statisticsBottomButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_plan);
        auth = FirebaseAuth.getInstance();
        Intent intent = getIntent();
        workoutPlanId = intent.getStringExtra("workoutPlanId");
        workoutPlanName = intent.getStringExtra("workoutPlanName");
        homeBottomButton = findViewById(R.id.buttonHome);
        statisticsBottomButton = findViewById(R.id.buttonStatistics);

        trainingSessionViewModel = new ViewModelProvider(this).get(TrainingSessionViewModel.class);

        // set correct workout plan name at the top
        TextView textViewWorkoutPlanName = findViewById(R.id.textViewWorkoutPlanName);
        textViewWorkoutPlanName.setText(workoutPlanName);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent1 = new Intent(WorkoutPlanActivity.this, NewExerciseActivity.class);
            intent1.putExtra("workoutPlanId", workoutPlanId);
            Log.d("WPA", "workoutPlanId sent:" + workoutPlanId);
            startActivityForResult(intent1, NEW_EXERCISE_ACTIVITY_REQUEST_CODE);
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        adapter = new ExerciseListAdapter(new ExerciseListAdapter.ExerciseDiff());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        exerciseViewModel = new ViewModelProvider(this).get(ExerciseViewModel.class);
        exerciseViewModel.getExercisesByWorkoutPlan(workoutPlanId).observe(this, exercises -> {
            adapter.submitList(exercises);
        });

        adapter.setOnItemClickListener(exercise -> {
            Intent intent1 = new Intent(WorkoutPlanActivity.this, EditExerciseActivity.class);
            intent1.putExtra(EditExerciseActivity.EXTRA_EXERCISE, exercise);
            startActivityForResult(intent1, EDIT_EXERCISE_REQUEST_CODE);
        });

        // back button in the top bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Home");
        }

        homeBottomButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(WorkoutPlanActivity.this, MainActivity.class);
            startActivity(intent1);
            finish();
        });

        statisticsBottomButton.setOnClickListener(v -> {
            Intent intent1 = new Intent(WorkoutPlanActivity.this, StatisticsActivity.class);
            startActivity(intent1);
            finish();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
        }
        super.onActivityResult(requestCode, resultCode, data);
        // new exercise
        if (requestCode == NEW_EXERCISE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null && data.hasExtra(NewExerciseActivity.EXTRA_REPLY)) {
            Exercise newExercise = data.getParcelableExtra(NewExerciseActivity.EXTRA_REPLY);
            if (newExercise != null && workoutPlanId != null) {
                newExercise.setWorkoutPlanId(workoutPlanId);
                exerciseViewModel.insert(newExercise, workoutPlanId);
            } else {
                Log.e("WPA", "New exercise or workout plan ID is null.");
            }
        } // edit exercise
        else if (requestCode == EDIT_EXERCISE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            if (data.hasExtra(EditExerciseActivity.EXTRA_REPLY)) {
                Exercise exercise = data.getParcelableExtra(EditExerciseActivity.EXTRA_REPLY);
                if (exercise != null && workoutPlanId != null) {
                    exercise.setWorkoutPlanId(workoutPlanId);
                    String dateLastTrained = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    // String dateLastTrained = "14/06/2024"; // for testing purposes

                    TrainingSession newTrainingSession = new TrainingSession(
                            userId,
                            exercise.getWorkoutPlanId(),
                            exercise.getId(),
                            exercise.getName(),
                            compute(exercise.getWeights(), exercise.getReps(), "maxWeight"), // part of new concept, computed values get added to a list in TrainingSession
                            compute(exercise.getWeights(), exercise.getReps(), "totalWeight"),
                            compute(exercise.getWeights(), exercise.getReps(), "averageWeightPerRep"),
                            dateLastTrained
                    );

                    Log.d("WPA", "Attempt insertion");

                    trainingSessionViewModel.getTrainingSessionByExerciseId(exercise.getId()).observe(this, existingSession -> {
                        if (existingSession != null) {
                            Log.d("WPA", "Checking for existing date entry");

                            // find the index of the existing date
                            List<String> dates = existingSession.getTrainingDate();
                            int dateIndex = dates.indexOf(dateLastTrained);

                            if (dateIndex != -1) {
                                // overwrite the existing entry for the date
                                Log.d("WPA", "Overwriting existing session for date " + dateLastTrained);
                                existingSession.getMaxWeight().set(dateIndex, newTrainingSession.getMaxWeight().get(0));
                                existingSession.getTotalWeight().set(dateIndex, newTrainingSession.getTotalWeight().get(0));
                                existingSession.getAverageWeightPerRep().set(dateIndex, newTrainingSession.getAverageWeightPerRep().get(0));
                            } else {
                                // add new entry
                                Log.d("WPA", "Adding new session for date " + dateLastTrained);
                                existingSession.getMaxWeight().add(newTrainingSession.getMaxWeight().get(0));
                                existingSession.getTotalWeight().add(newTrainingSession.getTotalWeight().get(0));
                                existingSession.getAverageWeightPerRep().add(newTrainingSession.getAverageWeightPerRep().get(0));
                                existingSession.getTrainingDate().add(dateLastTrained);
                            }
                            // update instead of creating new one (also part of new concept)
                            trainingSessionViewModel.update(existingSession);
                        } else {
                            Log.d("WPA", "Inserting new session");
                            trainingSessionViewModel.insert(newTrainingSession);
                        }
                    });
                    exerciseViewModel.update(exercise, workoutPlanId);
                } else {
                    Log.e("WPA", "Exercise or workout plan ID is null.");
                }
            } // delete exercise and all the corresponding TrainingSessions
            else if (data.hasExtra(EditExerciseActivity.EXTRA_DELETE)) {
                Exercise exercise = data.getParcelableExtra(EditExerciseActivity.EXTRA_DELETE);
                if (exercise != null && workoutPlanId != null) {
                    trainingSessionViewModel.deleteExercise(workoutPlanId, exercise.getName());
                    exerciseViewModel.delete(exercise, workoutPlanId);
                } else {
                    Log.e("WPA", "Exercise or workout plan ID is null.");
                }
            }
        }

        // notify the adapter that changes have been made so it can reload
        exerciseViewModel.getExercisesByWorkoutPlan(workoutPlanId).observe(this, exercises -> {
            adapter.submitList(exercises);
            adapter.notifyDataSetChanged();
        });
    }

    private Double compute(List<String> weights, List<String> reps, String type) {
        Double computedValue = 0.0;
        Double totalReps = 0.0;

        for (int i = 0; i < weights.size(); i++) {
            String weightsi = weights.get(i);
            String repsi = reps.get(i);
            // the tip for this error came from ChatGPT
            weightsi = weightsi.replace(",", "."); // commas can and will break the valueOf method
            repsi = repsi.replace(",", ".");

            try {
                Double currentWeight = Double.valueOf(weightsi);
                Double currentReps = Double.valueOf(repsi);

                if (type.equals("maxWeight")) {
                    if (currentWeight > computedValue) {
                        computedValue = currentWeight;
                    }
                } else if (type.equals("totalWeight")) {
                    computedValue += currentWeight * currentReps;
                } else if (type.equals("averageWeightPerRep")) {
                    computedValue += currentWeight * currentReps;
                    totalReps += currentReps;
                }
            } catch (NumberFormatException e) {
                Log.e("WPA", "NumberFormatException occurred: " + e.getMessage());
            }
        }

        if (type.equals("averageWeightPerRep") && totalReps != 0) {
            return computedValue / totalReps;
        }
        return computedValue;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }
}

