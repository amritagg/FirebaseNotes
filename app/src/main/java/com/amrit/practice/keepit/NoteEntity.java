package com.amrit.practice.keepit;

import java.util.ArrayList;

public class NoteEntity {

    String id;
    String head;
    String body;
    long date;
    ArrayList<String> images;
    ArrayList<String> keys;

    public NoteEntity(String id, String head, String body, long date, ArrayList<String> images, ArrayList<String> keys) {
        this.id = id;
        this.head = head;
        this.body = body;
        this.date = date;
        this.images = images;
        this.keys = keys;
    }

    public String getId() {
        return id;
    }

    public String getHead() {
        return head;
    }

    public String getBody() {
        return body;
    }

    public long getDate() {
        return date;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public ArrayList<String> getKeys() {
        return keys;
    }
}
