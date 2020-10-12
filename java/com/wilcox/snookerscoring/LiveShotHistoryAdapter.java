package com.wilcox.snookerscoring;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class LiveShotHistoryAdapter extends RecyclerView.Adapter<LiveShotHistoryAdapter.ViewHolder> {

    private ArrayList<Integer> data = new ArrayList();
    private Context context;

    // Constructor that retrieves data from activity
    public LiveShotHistoryAdapter(ArrayList<Integer> data, Context context) {
        this.data = data;
        this.context = context;
    }

    // This method inflates the view
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_shot_history, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    // This method binds data to the view
    // It sets the image to whatever score is in the shot history (data).
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        switch (data.get(position)) {
            case 1:
                holder.image.setImageResource(R.drawable.ball_red);
                break;
            case 2:
                holder.image.setImageResource(R.drawable.ball_yellow);
                break;
            case 3:
                holder.image.setImageResource(R.drawable.ball_green);
                break;
            case 4:
                holder.image.setImageResource(R.drawable.ball_brown);
                break;
            case 5:
                holder.image.setImageResource(R.drawable.ball_blue);
                break;
            case 6:
                holder.image.setImageResource(R.drawable.ball_pink);
                break;
            case 7:
                holder.image.setImageResource(R.drawable.ball_black);
                break;
            case -4:
                holder.image.setImageResource(R.drawable.f4);
                break;
            case -5:
                holder.image.setImageResource(R.drawable.f5);
                break;
            case -6:
                holder.image.setImageResource(R.drawable.f6);
                break;
            case -7:
                holder.image.setImageResource(R.drawable.f7);
                break;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ViewHolder(View itemView) {
            super(itemView);
            // Link to xml file
            image = itemView.findViewById(R.id.live_shot);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
