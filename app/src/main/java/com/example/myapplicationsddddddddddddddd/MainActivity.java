package com.example.myapplicationsddddddddddddddd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button addWorkoutPlanButton, homeBottomButton, statisticsBottombutton;
    private RecyclerView workoutPlansRecyclerView;
    private WorkoutPlanAdapter workoutPlanAdapter;
    private WorkoutPlanViewModel workoutPlanViewModel;
    private SharedPreferences sharedPreferences;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private NavigationView navigationView;
    private androidx.appcompat.app.ActionBar actionBar;
    private FirebaseAuth auth;
    private String userId;


    private static final int NEW_WORKOUT_PLAN_ACTIVITY_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        auth = FirebaseAuth.getInstance();

        sharedPreferences = getSharedPreferences("DateLastTrained", Context.MODE_PRIVATE);
        workoutPlanViewModel = new ViewModelProvider(this).get(WorkoutPlanViewModel.class);
        addWorkoutPlanButton = findViewById(R.id.add_workout_plan_button);
        workoutPlansRecyclerView = findViewById(R.id.workout_plans_recyclerview);
        workoutPlansRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        homeBottomButton = findViewById(R.id.buttonHome);
        statisticsBottombutton = findViewById(R.id.buttonStatistics);

        // handle wop delete
        WorkoutPlanAdapter.OnWorkoutPlanDeleteListener deleteListener = new WorkoutPlanAdapter.OnWorkoutPlanDeleteListener() {
            @Override
            public void onWorkoutPlanDelete(WorkoutPlan workoutPlan) {
                workoutPlanViewModel.deleteWorkoutPlan(workoutPlan);
            }
        };

        // handle wop update
        WorkoutPlanAdapter.OnWorkoutPlanUpdateListener updateListener = new WorkoutPlanAdapter.OnWorkoutPlanUpdateListener() {
            @Override
            public void onWorkoutPlanUpdate(WorkoutPlan workoutPlan) {
                workoutPlanViewModel.updateWorkoutPlan(workoutPlan);
            }
        };

        workoutPlanAdapter = new WorkoutPlanAdapter(deleteListener, updateListener);
        workoutPlansRecyclerView.setAdapter(workoutPlanAdapter);

        workoutPlanAdapter.setOnWorkoutPlanClickListener(workoutPlan -> {
            Intent intent = new Intent(MainActivity.this, WorkoutPlanActivity.class);
            intent.putExtra("workoutPlanId", workoutPlan.getId());
            intent.putExtra("workoutPlanName", workoutPlan.getName());
            Log.d("MainActivity", "workoutPlanId sent: " + workoutPlan.getId());
            Log.d("MainActivity", "workoutPlanName sent: " + workoutPlan.getName());
            startActivity(intent);
        });

        // buttons
        addWorkoutPlanButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddWorkoutPlanActivity.class);
            startActivityForResult(intent, NEW_WORKOUT_PLAN_ACTIVITY_REQUEST_CODE);
        });

        homeBottomButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        statisticsBottombutton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
            finish();
        });

        workoutPlanViewModel.getAllWorkoutPlans().observe(this, new Observer<List<WorkoutPlan>>() {
            @Override
            public void onChanged(@Nullable final List<WorkoutPlan> workoutPlans) {
                Log.d("MainActivity", "Workout plans updated: " + workoutPlans);
                workoutPlanAdapter.setWorkoutPlans(workoutPlans);
            }
        });

        // side layout (Settings, About)
        drawerLayout = findViewById(R.id.my_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close);


        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        // permanently put the icon on the ActionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();

            if (id == R.id.nav_settings) {
                // settings click
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_about) {
                //  about click
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // title (at the top)
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Home");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_WORKOUT_PLAN_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            String workoutPlanName = data.getStringExtra(AddWorkoutPlanActivity.EXTRA_REPLY);
            // get the userId of the user currently logged in
            FirebaseUser user = auth.getCurrentUser();
            if (user != null) {
                userId = user.getUid();
                Log.d("NewExerciseActivity", "User ID: " + userId);
            }
            WorkoutPlan workoutPlan = new WorkoutPlan(userId, workoutPlanName);
            workoutPlanViewModel.insert(workoutPlan);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workoutPlanViewModel.removeListener();
    }
}



