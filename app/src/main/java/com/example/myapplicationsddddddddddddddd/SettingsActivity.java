package com.example.myapplicationsddddddddddddddd;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingsActivity extends AppCompatActivity {

    androidx.appcompat.app.ActionBar actionBar;
    FirebaseAuth auth;
    Button logoutButton, changeEmailAdressButton, changePasswordButton, deleteAccountButton, resendVerificationEmailButton;
    FirebaseUser user;
    TextView TextViewUserEmail;
    String changeMailinput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initialization..
        auth = FirebaseAuth.getInstance();
        logoutButton = findViewById(R.id.btn_logout);
        user = auth.getCurrentUser();
        TextViewUserEmail = findViewById(R.id.TextViewUserEmail);
        changeEmailAdressButton = findViewById(R.id.btn_changeEmail);
        changePasswordButton = findViewById(R.id.btn_changePassword);
        deleteAccountButton = findViewById(R.id.btn_deleteAccount);
        resendVerificationEmailButton = findViewById(R.id.btn_resendVerificationEmail);

        if (user == null) {
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            TextViewUserEmail.setText("Associated email address: " + user.getEmail());
        }

        // click listeners for all the buttons
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        resendVerificationEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendVerificationEmail();
            }
        });

        changeEmailAdressButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptForEmailChange();
            }
        });

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });

        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
            }
        });

        // back button in the top bar
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("Home");
        }
    }

    private void resendVerificationEmail() {
        // the Dialog is basically a popup window that prompts the user for some action here
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Resend verification email");
        builder.setMessage("Are you sure you want to resend your verification email?");
        builder.setPositiveButton("Yes", (dialog, which) -> resendVerificationEmailVerified());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void resendVerificationEmailVerified() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Email sent.");
                            Toast.makeText(SettingsActivity.this, "Email sent.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void deleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete account");
        builder.setMessage("Are you sure you want to delete your account? This will also delete all of your workout plans, exercises and statistics!");
        builder.setPositiveButton("Yes, delete account", (dialog, which) -> deleteAccountFinal());
        builder.setNegativeButton("No", null);
        builder.show();
    }

    private void deleteAccountFinal() {
        // user has already confirmed that he wants to delete his account in deleteAccount
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
    }

    private void promptForEmailChange() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter new E-mail address");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        // buttons
        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeMailinput = input.getText().toString();
                promptForPassword(changeMailinput);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void promptForPassword(final String newEmail) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your password");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Reauthenticate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                reauthenticateAndChangeEmail(newEmail, password);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void reauthenticateAndChangeEmail(final String newEmail, String password) {
        // reauthentification required by Firebase for this operation
        if (user == null) return;

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User reauthenticated.");
                            Log.d(TAG, String.valueOf(newEmail));
                            sendVerificationEmail(newEmail);
                        } else {
                            Log.e(TAG, "Reauthentication failed.", task.getException());
                            Toast.makeText(SettingsActivity.this, "Reauthentication failed. Please try again. Maybe you did not confirm your email address?", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationEmail(final String newEmail) {
        if (user == null) return;

        ActionCodeSettings actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("https://noreply@login-registration-tracks.firebaseapp.com/__/auth/action?mode=verifyEmail&uid=" + user.getUid())
                .setHandleCodeInApp(true)
                .setAndroidPackageName(
                        getPackageName(),
                        true,
                        "12"    )
                .build();

        user.verifyBeforeUpdateEmail(newEmail, actionCodeSettings)

                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Verification email sent to new email address.");
                            Toast.makeText(SettingsActivity.this, "Verification email sent to new email address. Please verify to complete the process.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "Failed to send verification email.", task.getException());
                            Toast.makeText(SettingsActivity.this, "Failed to send verification email. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void changePassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm changing the password");

        // buttons
        builder.setPositiveButton("Send change email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                // send email where the user can change his password
                FirebaseAuth auth = FirebaseAuth.getInstance();
                String emailAddress = user.getEmail();;

                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    FirebaseAuth.getInstance().signOut();
                                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }
                            }
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }
}


