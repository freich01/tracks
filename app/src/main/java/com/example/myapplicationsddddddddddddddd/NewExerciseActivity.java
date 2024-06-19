package com.example.myapplicationsddddddddddddddd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewExerciseActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.example.myapplicationsddddddddddddddd.REPLY";
    private EditText exerciseNameEditText;
    private EditText notesEditText;
    private LinearLayout setsContainer;
    private String workoutPlanId, userId;
    private Button addSetButton;
    private SharedPreferences sharedPreferences;
    private boolean editMultiTrain = false;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_exercise);
        FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance();
        // retrieve the workoutPlanId from the intent
        workoutPlanId = getIntent().getStringExtra("workoutPlanId");
        Log.d("NewExerciseActivity", "workoutPlanId received: " + workoutPlanId);

        sharedPreferences = getSharedPreferences("DateLastTrained", Context.MODE_PRIVATE);
        exerciseNameEditText = findViewById(R.id.edit_exercise_name);
        notesEditText = findViewById(R.id.edit_notes);
        setsContainer = findViewById(R.id.sets_container);
        addSetButton = findViewById(R.id.button_add_set);

        exerciseNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        exerciseNameEditText.setSingleLine(true);
        exerciseNameEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        notesEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        addSetButton.setOnClickListener(v -> addSetFields(null, null));

        final Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(view -> saveExercise());

        // add 4 default sets
        for (int i = 0; i < 4; i++) {
            addSetFields(null, null);
        }

        // focus on exercise name and show keyboard
        exerciseNameEditText.requestFocus();
        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(exerciseNameEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 400);

        // check if MultiTrain was activated
        Switch switchMultiTrain = findViewById(R.id.switchMultiTrain);
        switchMultiTrain.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // switch in 'on'-position
            editMultiTrain = true;
        });

        // save exercise when hitting enter in Notes
        notesEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveExercise();
                return true;
            }
            return false;
        });
    }

    private void addSetFields(String weight, String reps) {
        View setView = getLayoutInflater().inflate(R.layout.item_set, null);
        EditText weightEditText = setView.findViewById(R.id.edit_weight);
        EditText repsEditText = setView.findViewById(R.id.edit_reps);
        ImageButton deleteButton = setView.findViewById(R.id.button_delete_set);

        // allow input of commas and points
        weightEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
        weightEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        repsEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
        repsEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        if (weight != null) weightEditText.setText(weight);
        if (reps != null) repsEditText.setText(reps);

        deleteButton.setOnClickListener(v -> setsContainer.removeView(setView));

        setsContainer.addView(setView);
    }

    private void saveExercise() {
        Intent replyIntent = new Intent();
        if (TextUtils.isEmpty(exerciseNameEditText.getText())) {
            setResult(RESULT_CANCELED, replyIntent);
        } else {
            String exerciseName = exerciseNameEditText.getText().toString();
            List<String> weights = new ArrayList<>();
            List<String> reps = new ArrayList<>();

            for (int i = 0; i < setsContainer.getChildCount(); i++) {
                View setView = setsContainer.getChildAt(i);
                EditText weightEditText = setView.findViewById(R.id.edit_weight);
                EditText repsEditText = setView.findViewById(R.id.edit_reps);

                String weightStr = weightEditText.getText().toString();
                String repsStr = repsEditText.getText().toString();

                // if both fields are empty in a line dont add it, if one is empty set the other to 0 so it does not mess up the statistics menu
                if (!TextUtils.isEmpty(weightStr)) {
                    weights.add(weightStr);
                }
                if (!TextUtils.isEmpty(repsStr)) {
                    reps.add(repsStr);
                }
                if (!TextUtils.isEmpty(weightStr) && TextUtils.isEmpty(repsStr)) {
                    reps.add("0");
                }
                if (TextUtils.isEmpty(weightStr) && !TextUtils.isEmpty(repsStr)) {
                    weights.add("0");
                }
            }

            String notes = "Notes: " + notesEditText.getText().toString();
            Date currentDate = new Date();

            // exerciseId has not been assigned by Firestore yet -> 'none'
            Exercise exercise = new Exercise("none", exerciseName, weights, reps, notes, currentDate, workoutPlanId, editMultiTrain);

            Log.d("NewExerciseActivity", "workoutPlanId used: " + workoutPlanId);
            replyIntent.putExtra(EXTRA_REPLY, exercise);
            setResult(RESULT_OK, replyIntent);

            // update Date last trained in Main
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yy", Locale.ENGLISH);
            String formattedDate = sdf.format(currentDate);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(String.valueOf(workoutPlanId), "Date last trained: " + formattedDate);
            editor.apply();
        }
        finish();
    }
}


