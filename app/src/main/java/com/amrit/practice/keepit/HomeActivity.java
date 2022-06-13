package com.amrit.practice.keepit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class HomeActivity extends AppCompatActivity {

    public static String LOG_TAG = HomeActivity.class.getSimpleName();

    String userId;
    DatabaseReference mDb;
    TextView textView;
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
        textView.setText("");

        initialiseRecyclerView();

        findViewById(R.id.fab).setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        getNotes();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getNotes() {
        noteList.clear();
        noteId.clear();
        noteAdapter.notifyDataSetChanged();
        mDb.addValueEventListener(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snap) {
                if (snap.exists()) {
                    for (DataSnapshot snapshot : snap.getChildren()) {
                        String body_text = "";
                        String head_text = "";
                        String note_id = snapshot.getKey();
                        long time = 0L;
                        ArrayList<String> images = new ArrayList<>();
                        ArrayList<String> keys = new ArrayList<>();

                        if (snapshot.child(Constants.FIRE_NOTE_HEAD).getValue() != null)
                            head_text = Objects.requireNonNull(snapshot.child(Constants.FIRE_NOTE_HEAD).getValue()).toString();

                        if (snapshot.child(Constants.FIRE_NOTE_BODY).getValue() != null)
                            body_text = Objects.requireNonNull(snapshot.child(Constants.FIRE_NOTE_BODY).getValue()).toString();

                        if (snapshot.child(Constants.FIRE_NOTE_LAST_UPDATE).getValue() != null)
                            time = Long.parseLong(Objects.requireNonNull(snapshot.child(Constants.FIRE_NOTE_LAST_UPDATE).getValue()).toString());

                        if (snapshot.child(Constants.FIRE_IMAGE).getValue() != null) {
                            for (DataSnapshot mediaSnapShot : snapshot.child(Constants.FIRE_IMAGE).getChildren()) {
                                keys.add(mediaSnapShot.getKey());
                                images.add(Objects.requireNonNull(mediaSnapShot.getValue()).toString());
                            }
                        }

                        if (!noteId.contains(note_id)) {
                            NoteEntity noteEntity = new NoteEntity(note_id, head_text, body_text, time, images, keys);
                            noteList.add(noteEntity);
                            noteId.add(note_id);
                        }
                    }
                    if (snap.getChildrenCount() == 0) {
                        textView.setVisibility(View.VISIBLE);
                        textView.setText("No Notes yet");
                    } else {
                        textView.setVisibility(View.GONE);
                        noteList.sort(Comparator.comparing(NoteEntity::getDate));
                        Collections.reverse(noteList);
                    }
                    noteAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                } else {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("No Notes yet");
                    progressBar.setVisibility(View.GONE);
                }
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
        noteAdapter = new HomeScreenAdapter(noteList);
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        menu.findItem(R.id.detect_language).setVisible(false);
        menu.findItem(R.id.speech_to_text).setVisible(false);
        menu.findItem(R.id.translate_language).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.add_image) {
            Intent intent = new Intent(this, AddNoteActivity.class);
            intent.putExtra(Constants.INTENT_OPEN_NEXT, Constants.OPEN_IMAGE);
            startActivity(intent);
            return true;
        } else if (menuItem.getItemId() == R.id.open_camera) {
            Intent intent = new Intent(this, AddNoteActivity.class);
            intent.putExtra(Constants.INTENT_OPEN_NEXT, Constants.OPEN_CAMERA);
            startActivity(intent);
            return true;
        } else if (menuItem.getItemId() == R.id.add_drawing) {
            Intent intent = new Intent(this, AddNoteActivity.class);
            intent.putExtra(Constants.INTENT_OPEN_NEXT, Constants.OPEN_DRAWING);
            startActivity(intent);
            return true;
        } else if (menuItem.getItemId() == R.id.logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getApplicationContext(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

}