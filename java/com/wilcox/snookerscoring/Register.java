package com.wilcox.snookerscoring;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Register extends AppCompatActivity {

    private boolean showPassword = false;
    private EditText mFirstName, mLastName, mEmail, mPassword, mConPassword;
    private ImageView eye;
    private Button registerButton;
    private FirebaseAuth auth;
    private ProgressDialog progress;

    @Override
    protected void onStart() {
        super.onStart();

        // If user is registered, send to dashboard
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            sendToDash();
        }
    }

    private void sendToDash() {
        Intent dashIntent = new Intent(Register.this, Dashboard.class);
        startActivity(dashIntent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        auth = FirebaseAuth.getInstance();

        progress = new ProgressDialog(this);

        // Initialise link to objects from xml file
        mEmail = findViewById(R.id.registerEmail);
        mPassword = findViewById(R.id.registerPassword);
        mConPassword = findViewById(R.id.registerConfirmPassword);
        registerButton = findViewById(R.id.registerButtonRegister);
        eye = findViewById(R.id.passwordEyeRegister);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        // Handles show password toggle
        eye.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!showPassword) {
                    mPassword.setTransformationMethod(null);
                    showPassword = true;
                } else {
                    mPassword.setTransformationMethod(new PasswordTransformationMethod());
                    showPassword = false;
                }
            }
        });

    }

    // This method is called when the user taps the register button
    private void register() {

        // Retrieve details that the user entered
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();
        String conPassword = mConPassword.getText().toString().trim();

        // This checks to make sure all fields are filled in
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(conPassword)) {
            if (password.equals(conPassword)) {
                progress.setMessage("Signing Up");
                progress.show();
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progress.dismiss();
                            Intent setupIntent = new Intent(Register.this, Account.class);
                            startActivity(setupIntent);
                            finish();
                        } else {
                            progress.dismiss();
                            Toast.makeText(Register.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            } else {
                Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(Register.this, "Fill in all fields", Toast.LENGTH_LONG).show();
        }
    }
}