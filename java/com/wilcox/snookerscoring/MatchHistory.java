package com.wilcox.snookerscoring;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MatchHistory extends AppCompatActivity {

    private Toolbar toolbar;
    private String id;
    private ArrayList<String[]> matchList = new ArrayList<>();

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.match_history);

        toolbar = findViewById(R.id.matchHistoryToolbar);

        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        id = firebaseAuth.getCurrentUser().getUid();

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Match History");

        // Gets current list of matches and stores them in recycler view
        DocumentReference docRef = firestore.collection("Users").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        java.util.Map tempMap = document.getData();
                        ArrayList<Map> savedMatches = (ArrayList) tempMap.get("MatchHistory");
                        if (savedMatches == null)
                            savedMatches = new ArrayList();
                        for (Map map : savedMatches) {
                            String[] tempArray = {map.get("name").toString(), map.get("score").toString()};
                            matchList.add(tempArray);
                            initRecyclerView();
                        }
                    } else {}
                } else {
                    Toast.makeText(MatchHistory.this, "Failed to retrieve data", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    // Refreshes recycler view allowing it to update
    private void initRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.matchList);
        MatchHistoryAdapter adapter = new MatchHistoryAdapter(matchList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}
