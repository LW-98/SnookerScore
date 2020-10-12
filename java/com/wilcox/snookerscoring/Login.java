package com.wilcox.snookerscoring;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

public class Login extends AppCompatActivity {

    private boolean showPassword = false;
    private EditText mEmail, mPassword;
    private TextView forgotPass;
    private Button loginButton;
    private ImageView eye;
    private LoginButton facebookLogin;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private ProgressBar progress;

    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        auth = FirebaseAuth.getInstance();

        // Initialise link to objects from XML file
        mEmail = findViewById(R.id.emailLogin);
        mPassword = findViewById(R.id.passwordLogin);
        loginButton = findViewById(R.id.loginButton);
        facebookLogin = findViewById(R.id.facebookSignIn);
        progress = findViewById(R.id.progressBarLogin);
        eye = findViewById(R.id.passwordEye);
        forgotPass = findViewById(R.id.forgotPassword);

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, ForgotPassword.class));
            }
        });

        // Create a callbackManager to handle login responses
        callbackManager = CallbackManager.Factory.create();
        facebookLogin.setPermissions(Arrays.asList("email", "public_profile"));
        facebookLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Toast.makeText(Login.this, "Cancelled Login", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException error) {
                Toast.makeText(Login.this, "Error: " + error, Toast.LENGTH_LONG).show();
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

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() != null) {
                    startActivity(new Intent(Login.this, Dashboard.class));
                }
            }
        };
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    private void signIn() {
        // Retrieves email and password that user enters
        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        // Checks to make sure all fields all filled in
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(Login.this, "Fill in all fields", Toast.LENGTH_LONG).show();
        } else {
            progress.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    // Checks to make sure details match
                    if (!task.isSuccessful()) {
                        Toast.makeText(Login.this, "Incorrect email or password", Toast.LENGTH_LONG).show();
                    }
                    progress.setVisibility(View.INVISIBLE);
                }
            });
        }
    }

    // Method that takes user to register if register button is clicked
    public void goToRegister(View view) {
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = auth.getCurrentUser();
                            startActivity(new Intent(Login.this, Dashboard.class));
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(Login.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
