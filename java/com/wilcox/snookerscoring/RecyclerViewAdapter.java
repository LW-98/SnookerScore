package com.wilcox.snookerscoring;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<Pair> nameList = new ArrayList<>();
    private Context context;

    public RecyclerViewAdapter(ArrayList<Pair> nameList, Context context) {
        this.nameList = nameList;
        this.context = context;
    }

    // This method inflates the view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.player_list_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    // This method binds data to the view
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
         holder.playerName.setText((String) nameList.get(position).first);
         holder.record.setText((String) nameList.get(position).second);

         // When delete is pressed, remove player from database and then RecyclerView
         holder.delete.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 DocumentReference docRef = holder.firestore.collection("Users").document(holder.id);
                 docRef.update("players."+holder.playerName.getText(), FieldValue.delete());

                 nameList.remove(position);
                 notifyItemRemoved(position);
                 notifyDataSetChanged();
             }
         });
    }

    // Returns how many players are saved to database/RecyclerView
    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView playerName, record;
        RelativeLayout layout;
        Button delete;
        FirebaseFirestore firestore;
        FirebaseAuth firebaseAuth;
        String id;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Link to xml file
            playerName = itemView.findViewById(R.id.playerManPlayerName);
            record = itemView.findViewById(R.id.winRecord);
            layout = itemView.findViewById(R.id.relativeLayout);
            delete = itemView.findViewById(R.id.deletePlayerButton);

            firestore = FirebaseFirestore.getInstance();
            firebaseAuth = FirebaseAuth.getInstance();
            id = firebaseAuth.getCurrentUser().getUid();
        }
    }
}
