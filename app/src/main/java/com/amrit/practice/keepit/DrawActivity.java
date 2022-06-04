package com.amrit.practice.keepit;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.ViewTreeObserver;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

//        rangeSlider = findViewById(R.id.rangebar);

        mDefaultColor = ContextCompat.getColor(DrawActivity.this, android.R.color.holo_red_dark);
//        ImageButton undo = findViewById(R.id.btn_undo);
//        ImageButton save = findViewById(R.id.btn_save);
//        ImageButton color = findViewById(R.id.btn_color);
//        ImageButton stroke = findViewById(R.id.btn_stroke);
//
//        undo.setOnClickListener(view -> paint.undo());
//
//        save.setOnClickListener(view -> {
//
//            Bitmap bmp = paint.save();
//
//            OutputStream imageOutStream;
//            ContentValues cv = new ContentValues();
//
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");
//            LocalDateTime now = LocalDateTime.now();
//            String time = dtf.format(now);
//            cv.put(MediaStore.Images.Media.DISPLAY_NAME, "drawing" + time + ".png");
//            cv.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
//            cv.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
//
//            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv);
//            try {
//                imageOutStream = getContentResolver().openOutputStream(uri);
//                bmp.compress(Bitmap.CompressFormat.PNG, 100, imageOutStream);
//                imageOutStream.close();
//                Toast.makeText(this, "Image Saved successfully!!", Toast.LENGTH_SHORT).show();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
//
//        color.setOnClickListener(view -> {
//
//            AmbilWarnaDialog colorPick = new AmbilWarnaDialog(this, mDefaultColor, new AmbilWarnaDialog.OnAmbilWarnaListener() {
//                @Override
//                public void onCancel(AmbilWarnaDialog dialog) {
//                }
//
//                @Override
//                public void onOk(AmbilWarnaDialog dialog, int color) {
//                    mDefaultColor = color;
//                    paint.setColor(color);
//                }
//            });
//            colorPick.show();
//
//        });
//
//        stroke.setOnClickListener(view -> {
//            if (rangeSlider.getVisibility() == View.VISIBLE) rangeSlider.setVisibility(View.GONE);
//            else rangeSlider.setVisibility(View.VISIBLE);
//        });
//
//        rangeSlider.setValueFrom(0.0f);
//        rangeSlider.setValueTo(100.0f);
//        rangeSlider.addOnChangeListener((slider, value, fromUser) -> paint.setStrokeWidth((int) value));

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

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == android.R.id.home) {
            returnData();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
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
