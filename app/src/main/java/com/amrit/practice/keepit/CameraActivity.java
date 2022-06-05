package com.amrit.practice.keepit;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.amrit.practice.keepit.databinding.ActivityCameraBinding;
import com.bumptech.glide.Glide;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private static final String LOG_TAG = CameraActivity.class.getSimpleName();
    private final String[] REQUIRED_PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
    private final int REQUEST_CODE_PERMISSIONS = 10;
    private ImageCapture imageCapture = null;
    private ExecutorService cameraExecutor;
    private ActivityCameraBinding binding;
    private String uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_camera);

        hideUi();
        permissionSetup();
        startCamera();

        binding.button.setOnClickListener(view -> takePhoto());
        binding.image.setVisibility(View.GONE);
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    private void takePhoto() {
        String FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS";
        String name = new SimpleDateFormat(FILENAME_FORMAT, Locale.ENGLISH).format(System.currentTimeMillis());
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraApp-Image");
        }

        ContentResolver contentResolver = getContentResolver();
        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
                .build();

        imageCapture.takePicture(outputFileOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        uri = Objects.requireNonNull(outputFileResults.getSavedUri()).toString();
                        showImage(uri);
                        Toast.makeText(getApplicationContext(), "Image saved", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException error) {
                        Toast.makeText(getApplicationContext(), "Error!!", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void showImage(String uri) {
        binding.view169.setVisibility(View.GONE);
        binding.image.setVisibility(View.VISIBLE);
        binding.button.setVisibility(View.GONE);
        Glide.with(this).load(uri).into(binding.image);
        recognizeText(uri);
    }

    private void recognizeText(String uri) {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        try {
            InputImage inputImage = InputImage.fromFilePath(this, Uri.parse(uri));

            recognizer.process(inputImage)
                    .addOnSuccessListener(visionText -> {
                        Intent returnIntent = new Intent();
                        Bundle bundle = new Bundle();
                        bundle.putString(Constants.INTENT_MEDIA_URI, visionText.getText());
                        returnIntent.putExtras(bundle);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_SHORT).show());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview;
                PreviewView previewView;
                preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                previewView = findViewById(R.id.view_16_9);


                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                cameraProvider.unbindAll();

                imageCapture = new ImageCapture.Builder()
                        .setJpegQuality(100)
                        .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                        .build();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (ExecutionException | InterruptedException e) {
                Log.e(LOG_TAG, "Error " + e);
            }
        }, ContextCompat.getMainExecutor(this));

    }

    private void hideUi() {
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.hide();

        getWindow().setStatusBarColor(Color.TRANSPARENT);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    private void permissionSetup() {
        if (cameraPermissionsGranted()) startCamera();
        else
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (cameraPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted!!", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean cameraPermissionsGranted() {
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

}
