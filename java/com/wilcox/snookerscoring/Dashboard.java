package com.wilcox.snookerscoring;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Dashboard extends AppCompatActivity {

    private Toolbar mainToolbar;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener authListener;
    private Button newMatch, playerManagement, map, matchHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        auth = FirebaseAuth.getInstance();
        String id = auth.getCurrentUser().getUid();

        // This makes sure the user has registered a name
        // e.g. if the user closes the app while setting a name/profile pic they are unable to use dashboard until they fill it out
        FirebaseFirestore ref = FirebaseFirestore.getInstance();
        DocumentReference doc = ref.collection("Users").document(id);
        doc.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (!document.exists())
                        startActivity(new Intent(Dashboard.this, Account.class));
                } else {
                    Toast.makeText(Dashboard.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG);
                }
            }
        });

        // Initialise link to objects from xml file
        mainToolbar = findViewById(R.id.accountToolbar);
        newMatch = findViewById(R.id.newMatchButtonDashboard);
        playerManagement = findViewById(R.id.playerManagementButton);
        map = findViewById(R.id.mapButton);
        matchHistory = findViewById(R.id.matchHistoryButton);

        // Creates app bar
        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Dashboard");

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    sendToLogin();
                }
            }
        };

        newMatch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, Match.class));
            }
        });

        playerManagement.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, PlayerManagement.class));
            }
        });

        map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Search for clubs nearby
                Uri mapURI = Uri.parse("geo:0,0?q=snooker%20club");
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, mapURI);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        matchHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Dashboard.this, MatchHistory.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        auth.addAuthStateListener(authListener);
    }

    // This is called when menu icon is selected
    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder m = (MenuBuilder) menu;
            m.setOptionalIconsVisible(true);
        }
        return true;
    }

    // This is called when a menu item is selected
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogout:
                logOut();
                return true;
            case R.id.menuAccountSettings:
                Intent accountIntent = new Intent(Dashboard.this, Account.class);
                startActivity(accountIntent);
                return true;
            default:
                return false;
        }
    }

    private void logOut() {
        auth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(Dashboard.this, Login.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(loginIntent);
    }
}
