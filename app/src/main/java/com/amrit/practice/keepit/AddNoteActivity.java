package com.amrit.practice.keepit;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    public static final String LOG_TAG = AddNoteActivity.class.getSimpleName();
    public static final Integer RecordAudioRequestCode = 1;
    DatabaseReference mDb;
    EditText head, body;
    String headText, bodyText;
    long date;
    String userId;
    String prevBody, prevHead;
    long prevDate;
    String prevId;
    boolean update;
    ArrayList<Stroke> list;
    ActivityResultLauncher<Intent> startDrawActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    Bundle bundle = data.getExtras();
                    list = bundle.getParcelableArrayList(Constants.INTENT_DRAWING_STROKES);
                    Log.e(LOG_TAG, list.toString());
                }
            }
    );
    Toast mToast;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        head = findViewById(R.id.head);
        body = findViewById(R.id.body);

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_BUNDLE)) {
            Bundle bundle = intent.getBundleExtra(Constants.INTENT_BUNDLE);
            prevBody = bundle.getString(Constants.INTENT_BODY);
            prevHead = bundle.getString(Constants.INTENT_HEAD);
            prevId = bundle.getString(Constants.INTENT_ID);
            prevDate = bundle.getLong(Constants.INTENT_DATE, 0);
            list = bundle.getParcelableArrayList(Constants.INTENT_DRAW_STROKES_FROM_FIREBASE);

            head.setText(prevHead);
            body.setText(prevBody);
            update = true;
        } else {
            prevDate = -1;
            prevBody = null;
            prevHead = null;
            prevId = null;
            update = false;
        }

        userId = FirebaseAuth.getInstance().getUid();
        mDb = FirebaseDatabase.getInstance().getReference().child(Constants.FIRE_NOTE).child(userId);
    }

    private void saveNote() {
        Log.e(LOG_TAG, "crate message is called");
        headText = head.getText().toString();
        bodyText = body.getText().toString();
        Map<String, Object> newMessageMap = new HashMap<>();

        String id;
        if (update) {
            date = prevDate;
            id = prevId;
            if (prevHead.equals(headText) && prevBody.equals(bodyText)) return;
            if (headText.isEmpty() && bodyText.isEmpty()) {
                mDb.child(id).removeValue();
                showToast("Note deleted!");
                return;
            }
        } else {
            if (headText.isEmpty() && bodyText.isEmpty()) {
                showToast("Note discarded!");
                return;
            }
            id = mDb.push().getKey();
        }
        date = Calendar.getInstance().getTime().getTime();
        assert id != null;

        DatabaseReference messageDb = mDb.child(id);
        newMessageMap.put(Constants.FIRE_NOTE_HEAD, headText);
        newMessageMap.put(Constants.FIRE_NOTE_BODY, bodyText);
        newMessageMap.put(Constants.FIRE_NOTE_LAST_UPDATE, date);
//        if (list != null) newMessageMap.put(Constants.FIRE_NOTE_DRAW_STROKE, list);
        messageDb.updateChildren(newMessageMap);
        Log.e(LOG_TAG, "Messaged pushed");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            saveNote();
            finish();
            return true;
        } else if (menuItem.getItemId() == R.id.add_drawing) {
            Intent intent = new Intent(this, DrawActivity.class);
            if (update) {
                if (list != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Constants.INTENT_DRAW_STROKES, list);
                    intent.putExtra(Constants.ADD_DRAW_BUNDLE, bundle);
                }
            }
            startDrawActivity.launch(intent);
            return true;
        } else if (menuItem.getItemId() == R.id.open_camera) {
            Intent intent = new Intent(this, CameraActivity.class);
            startActivity(intent);
            return true;
        } else if (menuItem.getItemId() == R.id.speech_to_text) {
            checkAudioPermission();
            return true;
        } else if (menuItem.getItemId() == R.id.detect_language) {
            detectLanguage();
            return true;
        } else if (menuItem.getItemId() == R.id.translate_language) {
            translateToEnglish();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void translateToEnglish() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build();
        final Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        unused -> {
                            String text = body.getText().toString();
                            if (text.isEmpty()) {
                                showToast("Add some text to detect");
                                return;
                            }
                            translator.translate(text)
                                    .addOnSuccessListener(this::showToast)
                                    .addOnFailureListener(e -> showToast(e.toString()));
                        })
                .addOnFailureListener(
                        e -> showToast(e.toString()));
    }

    private void detectLanguage() {
        LanguageIdentifier identifier = LanguageIdentification.getClient();
        getLifecycle().addObserver(identifier);

        String text = body.getText().toString();
        if (text.isEmpty()) {
            showToast("Add some text to detect");
            return;
        }
        identifier.identifyLanguage(text)
                .addOnSuccessListener(this::showToast)
                .addOnFailureListener(e -> showToast(e.toString()));
    }

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        else speechText();
    }

    private void speechText() {

        Log.e(LOG_TAG, "function is started");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);

        final Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        Log.e(LOG_TAG, "Working till now");
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
                showToast("Listening...");
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {
                showToast("Stopped!!!");
                speechRecognizer.stopListening();
            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (body.getText().toString().equals(""))
                    body.append(data.get(0));
                else body.append(" " + data.get(0));
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        speechRecognizer.startListening(speechRecognizerIntent);
    }

    @Override
    public void onBackPressed() {
        saveNote();
        finish();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognizer != null) speechRecognizer.destroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == RecordAudioRequestCode && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showToast("Permission Granted");
                speechText();
            } else showToast("Permission not granted");
        }
    }

    private void showToast(String msg) {
        if (mToast != null) mToast.cancel();
        mToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

}