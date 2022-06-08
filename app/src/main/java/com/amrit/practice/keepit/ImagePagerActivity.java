package com.amrit.practice.keepit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;

public class ImagePagerActivity extends AppCompatActivity {

    public static final String LOG_TAG = ImagePagerActivity.class.getSimpleName();
    ArrayList<String> uris;
    ViewPagerAdapter adapter;
    ViewPager2 viewPager2;
    private int cur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_pager);

        Intent intent = getIntent();
        uris = intent.getStringArrayListExtra(Constants.INTENT_PAGER_URIS);

        cur = 0;
        Log.e(LOG_TAG, uris.toString());
        setTitle(1 + " of " + uris.size());
        CompositePageTransformer transformer = new CompositePageTransformer();
        transformer.addTransformer(new MarginPageTransformer(100));
        adapter = new ViewPagerAdapter(this, uris);
        viewPager2 = findViewById(R.id.view_pager);

        viewPager2.setClipToPadding(false);
        viewPager2.setClipChildren(false);
        viewPager2.setOffscreenPageLimit(3);
        viewPager2.getChildAt(0).setOverScrollMode(View.OVER_SCROLL_NEVER);
        viewPager2.setAdapter(adapter);
        viewPager2.setCurrentItem(0);

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                cur = position;
                setTitle((position + 1) + " of " + uris.size());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_pager_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.paint_cur) {
            openPaint();
            return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    private void openPaint() {
        Intent intent = new Intent(this, DrawActivity.class);
        Glide.with(this).asBitmap().load(uris.get(cur)).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                MyCache.getInstance().saveBitmapToCache(uris.get(cur), resource);
                intent.putExtra(Constants.INTENT_IMAGE_URI, uris.get(cur));
                startActivity(intent);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }

}