package com.wilcox.snookerscoring;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import static android.text.TextUtils.isEmpty;

public class ForgotPassword extends AppCompatActivity {

    private TextView mEmail;
    private Button resetButton;
    private ProgressBar progress;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);

        auth = FirebaseAuth.getInstance();

        mEmail = findViewById(R.id.emailForgotPassword);
        resetButton = findViewById(R.id.resetPasswordButton);
        progress = findViewById(R.id.progressBarForgotPassword);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString();
                if (isEmpty(email)) {
                    Toast.makeText(ForgotPassword.this, "Enter valid email", Toast.LENGTH_LONG).show();
                } else {
                    progress.setVisibility(View.VISIBLE);

                    // Firebase method that send reset email
                    auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPassword.this, "Reset password email sent", Toast.LENGTH_LONG).show();
                                progress.setVisibility(View.INVISIBLE);
                                startActivity(new Intent(ForgotPassword.this, Login.class));
                            } else {
                                progress.setVisibility(View.INVISIBLE);
                                Toast.makeText(ForgotPassword.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }
}
