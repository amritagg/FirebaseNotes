package com.amrit.practice.keepit;

import android.graphics.Path;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class Stroke implements Parcelable, Serializable {

    public int color;
    public int strokeWidth;
    public Path path;

    public Stroke(int color, int strokeWidth, Path path) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.path = path;
    }

    protected Stroke(Parcel in) {
        color = in.readInt();
        strokeWidth = in.readInt();
    }

    public static final Creator<Stroke> CREATOR = new Creator<Stroke>() {
        @Override
        public Stroke createFromParcel(Parcel in) {
            return new Stroke(in);
        }

        @Override
        public Stroke[] newArray(int size) {
            return new Stroke[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(color);
        parcel.writeInt(strokeWidth);
    }

}
