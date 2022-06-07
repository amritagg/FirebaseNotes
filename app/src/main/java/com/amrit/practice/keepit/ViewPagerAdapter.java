package com.amrit.practice.keepit;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class ViewPagerAdapter extends RecyclerView.Adapter<ViewPagerAdapter.ViewPagerViewHolder> {

    private final Context context;
    private final ArrayList<String> images;
    private static final String LOG_TAG = ViewPagerAdapter.class.getSimpleName();

    public ViewPagerAdapter(Context context, ArrayList<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewPagerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_pager_image, parent, false);
        return new ViewPagerViewHolder(view);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onBindViewHolder(@NonNull ViewPagerViewHolder holder, int position) {
        Log.e(LOG_TAG, images.get(position));
        Glide.with(context).load(images.get(position)).load(holder.image);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ViewPagerViewHolder extends RecyclerView.ViewHolder {

        ImageView image;

        public ViewPagerViewHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.view_page_image);
        }
    }
}
