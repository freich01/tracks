package com.example.myapplicationsddddddddddddddd;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EditExerciseActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.example.myapplicationsddddddddddddddd.REPLY";
    public static final String EXTRA_DELETE = "com.example.myapplicationsddddddddddddddd.DELETE";
    public static final String EXTRA_EXERCISE = "com.example.myapplicationsddddddddddddddd.EXERCISE";

    private Exercise exercise;
    private EditText exerciseNameEditText;
    private EditText notesEditText;
    private LinearLayout setsContainer;
    private SharedPreferences sharedPreferences;

    private String workoutPlanId;

    SimpleDateFormat sdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_exercise);

        sharedPreferences = getSharedPreferences("DateLastTrained", Context.MODE_PRIVATE);
        exercise = getIntent().getParcelableExtra(EXTRA_EXERCISE);
        workoutPlanId = getIntent().getStringExtra("workoutPlanId"); // Changed to getStringExtra
        exerciseNameEditText = findViewById(R.id.edit_exercise_name);
        notesEditText = findViewById(R.id.edit_notes);
        setsContainer = findViewById(R.id.sets_container);
        exerciseNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        notesEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        if (exercise != null) {
            exerciseNameEditText.setText(exercise.getName());
            List<String> weights = exercise.getWeights();
            List<String> reps = exercise.getReps();

            for (int i = 0; i < Math.max(weights.size(), reps.size()); i++) {
                String weight = i < weights.size() ? weights.get(i) : null;
                String rep = i < reps.size() ? reps.get(i) : null;
                addSetFields(weight, rep);
            }
            String notesString = exercise.getNotes();
            notesEditText.setText(notesString.substring(7));
        }

        // focus the first weight field and show the keyboard
        if (setsContainer.getChildCount() > 0) {
            View firstSetView = setsContainer.getChildAt(0);
            EditText firstWeightEditText = firstSetView.findViewById(R.id.edit_weight);
            new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(firstWeightEditText), 350);
        }

        Date dateLastTrained = exercise.getDateLastTrained();
        sdf = new SimpleDateFormat("EEEE, dd/MM/yy", Locale.ENGLISH);
        String formattedDate = sdf.format(dateLastTrained);
        TextView textViewDLT = findViewById(R.id.textViewDateLastTrained);
        textViewDLT.setText("Date last trained: " + formattedDate);

        // move cursor to end of notesEditText when it gains focus for ease of use
        notesEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                notesEditText.setSelection(notesEditText.getText().length());
            }
        });

        notesEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveExercise();
                return true;
            }
            return false;
        });

        final Button saveButton = findViewById(R.id.button_save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveExercise();
            }
        });

        final Button deleteButton = findViewById(R.id.button_delete);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new AlertDialog.Builder(EditExerciseActivity.this)
                        .setTitle("Delete Exercise")
                        .setMessage("Are you sure you want to delete this exercise and all related statistical entries?")
                        .setPositiveButton("Yes", (dialog, which) -> deleteExercise())
                        .setNegativeButton("No", null)
                        .show();
            }
        });

        exerciseNameEditText.requestFocus();
        new Handler(Looper.getMainLooper()).postDelayed(() -> showKeyboard(exerciseNameEditText), 300);
    }

    private void addSetFields(String weight, String reps) {
        View setView = getLayoutInflater().inflate(R.layout.item_set_edit_exercise, null);
        EditText weightEditText = setView.findViewById(R.id.edit_weight);
        EditText repsEditText = setView.findViewById(R.id.edit_reps);

        // allow input of commas and points for weights and reps
        weightEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
        weightEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        repsEditText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_CLASS_TEXT);
        repsEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);

        if (weight != null) weightEditText.setText(weight);
        if (reps != null) repsEditText.setText(reps);

        setFocusAndClearText(weightEditText, weight);
        setFocusAndClearText(repsEditText, reps);

        weightEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (weightEditText.getText().length() == 0) {
                    repsEditText.requestFocus();
                    return true;
                }
            }
            return false;
        });

        repsEditText.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (repsEditText.getText().length() == 0) {
                    weightEditText.requestFocus();
                    return true;
                }
            }
            return false;
        });

        setsContainer.addView(setView);
    }

    private void setFocusAndClearText(EditText editText, String originalValue) {
        // just for looks
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                editText.setHint(editText.getText().toString());
                editText.setText("");
            } else {
                if (TextUtils.isEmpty(editText.getText())) {
                    editText.setText(originalValue);
                }
            }
        });
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

                weights.add(weightStr.isEmpty() ? exercise.getWeights().get(i) : weightStr);
                reps.add(repsStr.isEmpty() ? exercise.getReps().get(i) : repsStr);
            }

            String notes = "Notes: " + notesEditText.getText().toString(); // 'Notes:' added so it gets displayed in the RecyclerView
            exercise.setId(exercise.getId());
            exercise.setName(exerciseName);
            exercise.setWeights(weights);
            exercise.setReps(reps);
            exercise.setNotes(notes);
            replyIntent.putExtra(EXTRA_REPLY, exercise);
            setResult(RESULT_OK, replyIntent);

            Date currentDate = new Date();
            exercise.setDateLastTrained(currentDate);

            // update Date last trained in Main
            sdf = new SimpleDateFormat("EEEE, dd/MM/yy", Locale.ENGLISH);
            String formattedDate = sdf.format(currentDate);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            // store the 'old date' in order to enable coloring in ExerciseListAdapter
            editor.putString(exercise.getWorkoutPlanId() + "before", sharedPreferences.getString(exercise.getWorkoutPlanId(), ""));
            // save new date
            editor.putString(exercise.getWorkoutPlanId(), "Date last trained: " + formattedDate);
            editor.apply();
        }
        finish();
    }

    private void deleteExercise() {
        Intent replyIntent = new Intent();
        replyIntent.putExtra(EXTRA_DELETE, exercise);
        setResult(RESULT_OK, replyIntent);
        finish();
    }

    private void showKeyboard(View view) {
        if (view.requestFocus()) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }
}






