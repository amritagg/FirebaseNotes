package com.amrit.practice.keepit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;

public class DrawActivity extends AppCompatActivity {

    //    private RangeSlider rangeSlider;
    int mDefaultColor;
    private DrawView paint;
    ArrayList<Stroke> list;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        paint = findViewById(R.id.draw_view);
        Intent intent = getIntent();
        if(intent.hasExtra(Constants.ADD_DRAW_BUNDLE)) {
            Bundle bundle = intent.getExtras();
            list = bundle.getParcelableArrayList(Constants.INTENT_DRAW_STROKES);
            paint.setPaths(list);
        }


        mDefaultColor = ContextCompat.getColor(DrawActivity.this, android.R.color.holo_red_dark);

        ViewTreeObserver vto = paint.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                paint.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = paint.getMeasuredWidth();
                int height = paint.getMeasuredHeight();
                paint.init(height, width);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.drawing_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            returnData();
            return true;
        }else if(menuItem.getItemId() == R.id.detect){
            recognizeText();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void recognizeText() {
        Bitmap bitmap = paint.save();
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        InputImage inputImage = InputImage.fromBitmap(bitmap, 0);
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
    }

    @Override
    public void onBackPressed() {
        returnData();
        super.onBackPressed();
    }

    private void returnData() {
        Intent returnIntent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(Constants.INTENT_DRAWING_STROKES, paint.getPaths());
        returnIntent.putExtras(bundle);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

}
