package com.amrit.practice.keepit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class HomeScreenAdapter extends RecyclerView.Adapter<HomeScreenViewHolder> {

    private final List<NoteEntity> noteList;
    private final Context context;

    public HomeScreenAdapter(List<NoteEntity> noteList, Context context) {
        this.noteList = noteList;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeScreenViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        @SuppressLint("InflateParams")
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_home, null, false);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutView.setLayoutParams(lp);

        return new HomeScreenViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeScreenViewHolder holder, int position) {
        NoteEntity note = noteList.get(position);
        if (note.getBody().equals("")) holder.body.setVisibility(View.GONE);
        else {
            holder.body.setVisibility(View.VISIBLE);
            holder.body.setText(note.getBody());
        }
        if(note.getHead().equals("")) holder.head.setVisibility(View.GONE);
        else{
            holder.head.setVisibility(View.VISIBLE);
            holder.head.setText(note.getHead());
        }
        @SuppressLint("SimpleDateFormat")
        String date = new SimpleDateFormat("dd-MM-yyyy HH:mm").format(new Date(note.getDate()));
        holder.time.setText(date);

        holder.layout.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), AddNoteActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(Constants.INTENT_HEAD, note.getHead());
            bundle.putString(Constants.INTENT_BODY, note.getBody());
            bundle.putLong(Constants.INTENT_DATE, note.getDate());
            bundle.putString(Constants.INTENT_ID, note.getId());
            intent.putExtra(Constants.INTENT_BUNDLE, bundle);
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
