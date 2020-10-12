package com.wilcox.snookerscoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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

public class MatchHistoryAdapter extends RecyclerView.Adapter<MatchHistoryAdapter.ViewHolder> {

    private ArrayList<String[]> matchList;
    private ArrayList<Map> matches;
    private Context context;
    private Map<String, String> stats;
    private String name;

    public MatchHistoryAdapter(ArrayList<String[]> nameList, Context context) {
        this.matchList = nameList;
        this.context = context;
    }

    // This method inflates the view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.match_list_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    // This method binds data to the view
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        holder.info.setText(matchList.get(position)[0]);
        String[] score = matchList.get(position)[1].split(":");
        String temp = "";
        if (Integer.parseInt(score[0]) > Integer.parseInt(score[1]))
            temp += "W ";
        else
            temp += "L ";
        temp += score[0] + " - " + score[1];
        holder.score.setText(temp);

        holder.info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieves stats of selected match from database
                DocumentReference docRef = holder.firestore.collection("Users").document(holder.id);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                java.util.Map tempMap = document.getData();
                                matches = (ArrayList) tempMap.get("MatchHistory");
                                name = ((String) tempMap.get("name")).split(" ")[0];
                                stats = matches.get(position);
                                showStats();
                            } else {
                            }
                        } else {
                            Toast.makeText(context, "Failed to connect to database", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });

        // When delete is pressed, remove player from database and then RecyclerView
        holder.delete.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                DocumentReference docRef = holder.firestore.collection("Users").document(holder.id);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                java.util.Map tempMap = document.getData();
                                matches = (ArrayList) tempMap.get("MatchHistory");
                                matches.remove(position);
                                Map temp = new HashMap();
                                temp.put("MatchHistory", matches);
                                docRef.update(temp);
                            } else {}
                        } else {
                            Toast.makeText(context, "Failed to connect to database", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                matchList.remove(position);
                notifyItemRemoved(position);
                notifyDataSetChanged();
            }
        });
    }

    // Show statistics of selected match in popup dialog
    public void showStats() {
        List<String> list = new ArrayList();
        for (int i = 1; i < 31; i++) {
            switch (i) {
                case 4:
                    list.add(name);
                    break;
                case 6:
                    list.add(String.valueOf(stats.get("name")));
                    break;
                case 7:
                    list.add(String.valueOf(stats.get("score").split(":")[0]));
                    break;
                case 8:
                    list.add("Score");
                    break;
                case 9:
                    list.add(String.valueOf(stats.get("score").split(":")[1]));
                    break;
                case 10:
                    list.add(String.valueOf(stats.get("break").split(":")[0]));
                    break;
                case 11:
                    list.add("Max Break");
                    break;
                case 12:
                    list.add(String.valueOf(stats.get("break").split(":")[1]));
                    break;
                case 13:
                    list.add(String.valueOf(stats.get("fouls").split(":")[0]));
                    break;
                case 14:
                    list.add("Fouls");
                    break;
                case 15:
                    list.add(String.valueOf(stats.get("fouls").split(":")[1]));
                    break;
                case 16:
                    list.add(String.valueOf(stats.get("misses").split(":")[0]));
                    break;
                case 17:
                    list.add("Misses");
                    break;
                case 18:
                    list.add(String.valueOf(stats.get("misses").split(":")[1]));
                    break;
                case 19:
                    list.add(String.valueOf(stats.get("pots").split(":")[0]));
                    break;
                case 20:
                    list.add("Pots");
                    break;
                case 21:
                    list.add(String.valueOf(stats.get("pots").split(":")[1]));
                    break;
                case 22:
                    list.add(String.valueOf(stats.get("shots").split(":")[0]));
                    break;
                case 23:
                    list.add("Shots");
                    break;
                case 24:
                    list.add(String.valueOf(stats.get("shots").split(":")[1]));
                    break;
                case 25:
                    list.add(String.format("%.1f", Double.parseDouble(stats.get("potPercentage").split(":")[0])) + "%");
                    break;
                case 26:
                    list.add("Pot Percentage");
                    break;
                case 27:
                    list.add(String.format("%.1f", Double.parseDouble(stats.get("potPercentage").split(":")[1])) + "%");
                    break;
                case 28:
                    list.add(String.format("%.1f", Double.parseDouble(stats.get("safetyPercentage").split(":")[0])) + "%");
                    break;
                case 29:
                    list.add("Safety Percentage");
                    break;
                case 30:
                    list.add(String.format("%.1f", Double.parseDouble(stats.get("safetyPercentage").split(":")[1])) + "%");
                    break;
                default:
                    list.add("");
            }
        }

        GridView grid = new GridView(context);
        grid.setAdapter(new ArrayAdapter(context, android.R.layout.simple_list_item_1, list));
        grid.setNumColumns(3);

        new AlertDialog.Builder(context)
                .setNegativeButton("Back", null)
                .setTitle("Match Statistics")
                .setView(grid)
                .show();
    }

    // Returns how many players are saved to database/RecyclerView
    @Override
    public int getItemCount() {
        return matchList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView info, score;
        RelativeLayout layout;
        Button delete;
        FirebaseFirestore firestore;
        FirebaseAuth firebaseAuth;
        String id;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link to xml file
            info = itemView.findViewById((R.id.matchInfo));
            score = itemView.findViewById(R.id.matchScore);
            layout = itemView.findViewById(R.id.relativeLayout);
            delete = itemView.findViewById(R.id.deleteMatchButton);

            firestore = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            id = firebaseAuth.getCurrentUser().getUid();
        }
    }
}
