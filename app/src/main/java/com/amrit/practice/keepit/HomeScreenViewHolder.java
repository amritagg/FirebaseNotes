package com.amrit.practice.keepit;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class HomeScreenViewHolder extends RecyclerView.ViewHolder {

    public TextView head;
    public TextView body;
    public TextView time;
    public ConstraintLayout layout;

    public HomeScreenViewHolder(@NonNull View itemView) {
        super(itemView);
        head = itemView.findViewById(R.id.head_home);
        body = itemView.findViewById(R.id.body_home);
        time = itemView.findViewById(R.id.date_home);
        layout = itemView.findViewById(R.id.card_layout_home);
    }
}
