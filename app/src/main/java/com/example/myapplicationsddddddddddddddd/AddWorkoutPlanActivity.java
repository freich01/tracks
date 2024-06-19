package com.example.myapplicationsddddddddddddddd;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class AddWorkoutPlanActivity extends AppCompatActivity {

    public static final String EXTRA_REPLY = "com.example.myapplicationsddddddddddddddd.REPLY";

    private EditText editWorkoutPlanName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_workout_plan);

        editWorkoutPlanName = findViewById(R.id.edit_workout_plan_name);
        editWorkoutPlanName.setInputType(InputType.TYPE_CLASS_TEXT);

        // focus on text field and show keyboard
        editWorkoutPlanName.requestFocus();
        new Handler().postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(editWorkoutPlanName, InputMethodManager.SHOW_IMPLICIT);
            }
        }, 350); // keyboard won´t open without delay..

        // save new workout plan when enter is clicked
        editWorkoutPlanName.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveExercise();
                return true;
            }
            return false;
        });

        final Button button = findViewById(R.id.button_save);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
            saveExercise();
            }
        });
    }

    public void saveExercise () {
        Intent replyIntent = new Intent();
        if (TextUtils.isEmpty(editWorkoutPlanName.getText())) {
            setResult(RESULT_CANCELED, replyIntent);
        } else {
            String workoutPlanName = editWorkoutPlanName.getText().toString();
            replyIntent.putExtra(EXTRA_REPLY, workoutPlanName);
            setResult(RESULT_OK, replyIntent);
        }
        finish();
    }
}


