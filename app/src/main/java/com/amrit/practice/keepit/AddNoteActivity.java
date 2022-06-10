package com.amrit.practice.keepit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.languageid.LanguageIdentification;
import com.google.mlkit.nl.languageid.LanguageIdentifier;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddNoteActivity extends AppCompatActivity {

    public static final String LOG_TAG = AddNoteActivity.class.getSimpleName();
    public static final int RecordAudioRequestCode = 1;
    private EditText head, body;
    private DatabaseReference mDb;
    private String prevBody;
    private String prevHead;
    private String prevId;
    private boolean update;
    private Toast mToast;
    private Uri uri;
    private String userId;
    private ArrayList<String> localImages;
    private final ActivityResultLauncher<Intent> startDrawActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    String uriString = data.getStringExtra(Constants.INTENT_MEDIA_URI);
                    uri = Uri.parse(uriString);
                    localImages.add(uriString);
                    Log.e(LOG_TAG, uriString);
                    detectText();
                }
            }
    );
    private final ActivityResultLauncher<Intent> startCameraActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    Bundle bundle = data.getExtras();
                    String uriString = bundle.getString(Constants.INTENT_MEDIA_URI);
                    localImages.add(uriString);
                    uri = Uri.parse(uriString);
                    detectText();
                }
            }
    );
    private final ActivityResultLauncher<Intent> startImagePickerActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    assert data != null;
                    uri = data.getData();
                    localImages.add(uri.toString());
                    detectText();
                }
            }
    );
    private ArrayList<String> fireImages;
    private ImageView showImage;
    private ArrayList<String> keys;
    private SpeechRecognizer speechRecognizer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        head = findViewById(R.id.head);
        body = findViewById(R.id.body);
        localImages = new ArrayList<>();

        Intent intent = getIntent();
        if (intent.hasExtra(Constants.INTENT_OPEN_NEXT)) {
            String val = intent.getStringExtra(Constants.INTENT_OPEN_NEXT);
            switch (val) {
                case Constants.OPEN_CAMERA: {
                    Intent intent1 = new Intent(this, CameraActivity.class);
                    startCameraActivity.launch(intent1);
                    break;
                }
                case Constants.OPEN_IMAGE:
                    Intent intent3 = new Intent(Intent.ACTION_PICK);
                    intent3.setType("image/*");
                    startImagePickerActivity.launch(intent3);
                    break;
                case Constants.OPEN_DRAWING: {
                    Intent intent1 = new Intent(this, DrawActivity.class);
                    startDrawActivity.launch(intent1);
                    break;
                }
            }
        }

        if (intent.hasExtra(Constants.INTENT_BUNDLE)) {
            Bundle bundle = intent.getBundleExtra(Constants.INTENT_BUNDLE);
            prevBody = bundle.getString(Constants.INTENT_BODY);
            prevHead = bundle.getString(Constants.INTENT_HEAD);
            prevId = bundle.getString(Constants.INTENT_ID);
            fireImages = bundle.getStringArrayList(Constants.INTENT_IMAGES);
            keys = bundle.getStringArrayList(Constants.INTENT_IMAGE_KEYS);

            head.setText(prevHead);
            body.setText(prevBody);
            update = true;
        } else {
            prevBody = null;
            prevHead = null;
            prevId = null;
            update = false;
            fireImages = new ArrayList<>();
            keys = new ArrayList<>();
        }

        userId = FirebaseAuth.getInstance().getUid();
        if (userId != null)
            mDb = FirebaseDatabase.getInstance().getReference().child(Constants.FIRE_NOTE).child(userId);

        showImage = findViewById(R.id.view_pager);
        if (fireImages.size() == 0) showImage.setVisibility(View.GONE);
        else {
            showImage.setVisibility(View.VISIBLE);
            Glide.with(this).load(fireImages.get(0)).into(showImage);
        }

        showImage.setOnClickListener(view -> {
            Intent pagerIntent = new Intent(AddNoteActivity.this, ImagePagerActivity.class);
            ArrayList<String> temp = new ArrayList<>();
            temp.addAll(fireImages);
            temp.addAll(localImages);
            pagerIntent.putStringArrayListExtra(Constants.INTENT_PAGER_URIS, temp);
            startActivity(pagerIntent);
        });
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onResume() {
        super.onResume();
        Log.e(LOG_TAG, "on start works");
        if (fireImages.size() == 0 && localImages.size() > 0) {
            showImage.setVisibility(View.VISIBLE);
            Log.e(LOG_TAG, "on start works");
            Glide.with(this).load(localImages.get(0)).into(showImage);
        }
    }

    private void saveNote() {
        if (localImages.size() == 0 && fireImages.size() == 0) saveText();
        else saveImage();
    }

    private void saveText() {
        Log.e(LOG_TAG, "crate message is called");
        String headText = head.getText().toString();
        String bodyText = body.getText().toString();
        Map<String, Object> newMessageMap = new HashMap<>();

        String id;
        long date;
        if (update) {
            Log.e(LOG_TAG, prevId);
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
        newMessageMap.put(Constants.FIRE_NOTE_HEAD, headText.trim());
        newMessageMap.put(Constants.FIRE_NOTE_BODY, bodyText.trim());
        newMessageMap.put(Constants.FIRE_NOTE_LAST_UPDATE, date);
        messageDb.updateChildren(newMessageMap);
        Log.e(LOG_TAG, "Messaged pushed");
    }

    private void saveImage() {
        Log.e(LOG_TAG, "crate message is called");
        String headText = head.getText().toString();
        String bodyText = body.getText().toString();
        Map<String, Object> newMessageMap = new HashMap<>();

        String id;
        long date;
        if (update) {
            Log.e(LOG_TAG, prevId);
            id = prevId;
            if (prevHead.equals(headText) && prevBody.equals(bodyText) && localImages.size() == 0)
                return;
            if (headText.isEmpty() && bodyText.isEmpty() && fireImages.size() == 0) {
                mDb.child(id).removeValue();
                showToast("Note deleted!");
                return;
            }
        } else {
            if (headText.isEmpty() && bodyText.isEmpty() && localImages.size() == 0 && fireImages.size() == 0) {
                showToast("Note discarded!");
                return;
            }
            id = mDb.push().getKey();
        }
        date = Calendar.getInstance().getTime().getTime();
        assert id != null;

        Log.e(LOG_TAG, headText + " " + bodyText);
        DatabaseReference messageDb = mDb.child(id);
        newMessageMap.put(Constants.FIRE_NOTE_HEAD, headText.trim());
        newMessageMap.put(Constants.FIRE_NOTE_BODY, bodyText.trim());
        newMessageMap.put(Constants.FIRE_NOTE_LAST_UPDATE, date);

        for (int k = 0; k < fireImages.size(); k++) {
            newMessageMap.put("/image/" + keys.get(k) + "/", fireImages.get(k));
        }

        if (localImages.size() == 0) {
            messageDb.updateChildren(newMessageMap);
            return;
        }
        for (int i = 0; i < localImages.size(); i++) {
            String mediaId = mDb.child(Constants.FIRE_IMAGE).push().getKey();
            assert mediaId != null;
            StorageReference filePath = FirebaseStorage.getInstance().getReference().child("images").child(userId).child(id).child(mediaId);
            UploadTask uploadTask = filePath.putFile(Uri.parse(localImages.get(i)));
            int finalI = i;
            uploadTask.addOnSuccessListener(taskSnapshot -> filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                newMessageMap.put("/image/" + mediaId + "/", uri.toString());
                if (finalI == localImages.size() - 1) {
                    messageDb.updateChildren(newMessageMap);
                    Log.e(LOG_TAG, "Images pushed");
                }
            }));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        int id = menuItem.getItemId();
        switch (id) {
            case android.R.id.home:
                saveNote();
                finish();
                break;
            case R.id.add_image:
                Intent intent3 = new Intent(Intent.ACTION_PICK);
                intent3.setType("image/*");
                startImagePickerActivity.launch(intent3);
                break;
            case R.id.add_drawing:
                Intent intent = new Intent(this, DrawActivity.class);
                startDrawActivity.launch(intent);
                break;
            case R.id.open_camera:
                Intent intent1 = new Intent(this, CameraActivity.class);
                startCameraActivity.launch(intent1);
                break;
            case R.id.speech_to_text:
                checkAudioPermission();
                break;
            case R.id.detect_language:
                detectLanguage();
                break;
            case R.id.translate_language:
                translateFromEnglish();
                break;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void detectText() {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        try {
            InputImage inputImage = InputImage.fromFilePath(this, uri);

            recognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        body.append("\nAfter detection: " + visionText.getText().trim());
                        body.setText(body.getText().toString().trim());
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void translateFromEnglish() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build();

        final Translator translator = Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder().requireWifi().build();
        translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        unused -> {
                            String text = body.getText().toString();
                            if (text.isEmpty()) {
                                showToast("Add some text to detect");
                                return;
                            }
                            translator.translate(text)
                                    .addOnSuccessListener(s -> body.append("\nIn Hindi: " + s))
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
                    body.append(data.get(0).trim());
                else body.append(" " + data.get(0).trim());
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

    private void checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, RecordAudioRequestCode);
        else speechText();
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