package com.wilcox.snookerscoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerManagement extends AppCompatActivity {

    private String id;
    private Toolbar mainToolbar;
    private Button addPlayer;
    private ArrayList<Pair> nameList = new ArrayList<>();
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player_management);

        mainToolbar = findViewById(R.id.accountToolbar);
        addPlayer = findViewById(R.id.addPlayerButton);

        setSupportActionBar(mainToolbar);
        getSupportActionBar().setTitle("Player Management");

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        id = firebaseAuth.getCurrentUser().getUid();

        // Gets current list of players and stores them in recycler view
        DocumentReference docRef = firestore.collection("Users").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map tempMap = document.getData();
                        Map newMap = (Map) tempMap.get("players");
                        if (newMap == null)
                            newMap = new HashMap();
                        for (Object obj : newMap.values()) {
                            Map map = (Map) obj;
                            nameList.add(new Pair((String) map.get("name"), map.get("Wins") + "/" + map.get("Losses")));
                            initRecyclerView();
                        }
                    } else {
                    }
                } else {
                    Toast.makeText(PlayerManagement.this, "Failed to connect to database", Toast.LENGTH_LONG).show();
                }
            }
        });

        addPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddPlayer(PlayerManagement.this);
            }
        });
    }

    // Shows dialog box allowing user to add a player
    private void showAddPlayer(Context c) {
        final EditText temp = new EditText(c);
        AlertDialog dialog = new AlertDialog.Builder(c)
                .setTitle("New Player")
                .setMessage("Enter players name:")
                .setView(temp)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newPlayerName = String.valueOf(temp.getText());
                        if (!TextUtils.isEmpty(newPlayerName)) {
                            nameList.add(new Pair(newPlayerName, "0/0"));
                            Map<String, Object> map = new HashMap<>();
                            map.put("name", newPlayerName);
                            map.put("Wins", 0);
                            map.put("Losses", 0);
                            firestore.collection("Users").document(id)
                                    .update(
                                            "players." + newPlayerName, map
                                    );
                            initRecyclerView();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    // Refreshes recycler view allowing it to update
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.playerList);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(nameList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}