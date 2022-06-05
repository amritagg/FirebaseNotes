package com.amrit.practice.keepit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    public static String LOG_TAG = HomeActivity.class.getSimpleName();

    String userId;
    DatabaseReference mDb;
    TextView textView;
    StringBuilder str;
    ProgressBar progressBar;
    private List<NoteEntity> noteList;
    private HashSet<String> noteId;
    private HomeScreenAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        textView = findViewById(R.id.text);
        progressBar = findViewById(R.id.progress);
        userId = FirebaseAuth.getInstance().getUid();
        mDb = FirebaseDatabase.getInstance().getReference().child(Constants.FIRE_NOTE).child(userId);
        str = new StringBuilder();

        initialiseRecyclerView();

        findViewById(R.id.fab).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        textView.setText("");
        getNotes();
    }

    private void getNotes() {
        mDb.addChildEventListener(new ChildEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String body_text = "";
                    String head_text = "";
                    String note_id = snapshot.getValue().toString();
                    long time = 0L;

                    if (snapshot.child(Constants.FIRE_NOTE_HEAD).getValue() != null)
                        head_text = snapshot.child(Constants.FIRE_NOTE_HEAD).getValue().toString();

                    if (snapshot.child(Constants.FIRE_NOTE_BODY).getValue() != null)
                        body_text = snapshot.child(Constants.FIRE_NOTE_BODY).getValue().toString();

                    if (snapshot.child(Constants.FIRE_NOTE_LAST_UPDATE).getValue() != null)
                        time = Long.parseLong(snapshot.child(Constants.FIRE_NOTE_LAST_UPDATE).getValue().toString());

                    if (!noteId.contains(note_id)) {
                        NoteEntity noteEntity = new NoteEntity(note_id, head_text, body_text, time);
                        noteList.add(noteEntity);
                        noteAdapter.notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                        noteId.add(note_id);
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }

        });
    }

    private void initialiseRecyclerView() {
        progressBar.setVisibility(View.VISIBLE);
        noteList = new ArrayList<>();
        noteId = new HashSet<>();
        RecyclerView recyclerView = findViewById(R.id.recycler_view_home);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager mChatLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mChatLayoutManager);
        noteAdapter = new HomeScreenAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);
    }

}