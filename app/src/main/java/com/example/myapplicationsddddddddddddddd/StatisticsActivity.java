package com.example.myapplicationsddddddddddddddd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class StatisticsActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {
    private static final String TAG = "StatisticsActivity";
    private Button homeBottomButton, statisticsBottomButton;
    private TrainingSessionViewModel trainingSessionViewModel;
    private TrainingSessionListAdapter adapter;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private Spinner nameSpinner, timeFrameSpinner, typeSpinner;
    private String[] timeFrameValues = {"All", "Last 7 days", "Last 30 days", "Last year"};
    private String[] typeValues = {"Total weight", "Max weight", "Avg weight per rep"};
    private String userId;
    private boolean initialLoad = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        homeBottomButton = findViewById(R.id.buttonHome);
        statisticsBottomButton = findViewById(R.id.buttonStatistics);
        trainingSessionViewModel = new ViewModelProvider(this).get(TrainingSessionViewModel.class);

        setTitle("Statistics");

        homeBottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        statisticsBottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(StatisticsActivity.this, StatisticsActivity.class);
            startActivity(intent);
            finish();
        });

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        adapter = new TrainingSessionListAdapter(new TrainingSessionListAdapter.TrainingSessionDiff(), this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));


        // get the userId of the user currently logged in
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            userId = user.getUid();
            Log.d("StatisticsActivity", "User ID: " + userId);
        }

        // set up the name spinner
        fetchWorkoutPlans(userId);

        // set up the timeframe spinner
        timeFrameSpinner = findViewById(R.id.spinnerTimeframe);
        ArrayAdapter<String> adapterTimeframe = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, timeFrameValues);
        timeFrameSpinner.setAdapter(adapterTimeframe);
        timeFrameSpinner.setSelection(1); // default: "Last week"
        timeFrameSpinner.setOnItemSelectedListener(this);

        // set up the type spinner
        typeSpinner = findViewById(R.id.spinnerType);
        ArrayAdapter<String> adapterType = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, typeValues);
        typeSpinner.setAdapter(adapterType);
        typeSpinner.setSelection(1); // default: "Max weight"
        typeSpinner.setOnItemSelectedListener(this);

        // observe the LiveData from ViewModel, update the adapter when data changes
        trainingSessionViewModel.getWorkoutPlanTrainingSessions().observe(this, trainingSessions -> {
            if (trainingSessions != null) {
                Log.d(TAG, "Training sessions updated: " + trainingSessions.size());
                adapter.submitList(trainingSessions);
                Log.d("StatisticsActivity", "Submitted to adapter: " + trainingSessions);
            } else {
                adapter.submitList(new ArrayList<>());
                Log.d(TAG, "Training sessions is null");
            }
        });
    }

    private void fetchWorkoutPlans(String userId) {
        db.collection("workout_plans")
                .whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> workoutPlanNames = new ArrayList<>();
                        workoutPlanNames.add("All"); // has to be added manually because it is not an added workoutplan
                        for (DocumentSnapshot document : task.getResult()) {
                            WorkoutPlan workoutPlan = document.toObject(WorkoutPlan.class);
                            if (workoutPlan != null && workoutPlan.getName() != null) {
                                workoutPlanNames.add(workoutPlan.getName());
                            }
                        }

                        String[] namesValues = workoutPlanNames.toArray(new String[0]);
                        Log.d(TAG, "Workout plan names loaded: " + workoutPlanNames);

                        // spinner for workout plan names
                        nameSpinner = findViewById(R.id.spinnerNames);
                        ArrayAdapter<String> adapterNames = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, namesValues);
                        nameSpinner.setAdapter(adapterNames);
                        nameSpinner.setSelection(0); // default: "All"
                        nameSpinner.setOnItemSelectedListener(this);

                        if (initialLoad) {
                            trainingSessionViewModel.loadWorkoutPlan("All");
                            initialLoad = false;
                        }

                    } else {
                        Log.w(TAG, "Error getting workout plans.", task.getException());
                    }
                });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (nameSpinner == null || timeFrameSpinner == null || typeSpinner == null) {
            return;
        }

        if (parent.getId() == R.id.spinnerNames) {
            String selectedWorkoutPlan = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "Selected workout plan: " + selectedWorkoutPlan);
            trainingSessionViewModel.loadWorkoutPlan(selectedWorkoutPlan);

        } else if (parent.getId() == R.id.spinnerTimeframe) {
            String selectedTimeframe = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "Selected timeframe: " + selectedTimeframe);
            // load plans based on the selected plan and timeframe
            String nameCurrent = String.valueOf(nameSpinner.getSelectedItem());
            trainingSessionViewModel.getWorkoutPlanTimeframe(nameCurrent, selectedTimeframe);

        } else if (parent.getId() == R.id.spinnerType) {
            String selectedType = parent.getItemAtPosition(position).toString();
            Log.d(TAG, "Selected type: " + selectedType);
            if (adapter != null) {
                adapter.setSelectedType(selectedType);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // has to be implemented for proper functionality
    }

    @Override
    public void onClick(View v) {
        // has to be implemented for proper functionality
    }
}




