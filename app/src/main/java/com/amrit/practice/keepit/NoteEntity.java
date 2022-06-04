package com.amrit.practice.keepit;

import java.util.ArrayList;

public class NoteEntity {

    String id;
    String head;
    String body;
    long date;
    ArrayList<Stroke> stroke;

    public NoteEntity(String id, String head, String body, long date) {
        this.id = id;
        this.head = head;
        this.body = body;
        this.date = date;
    }

    public NoteEntity(String id, String head, String body, long date, ArrayList<Stroke> stroke) {
        this.id = id;
        this.head = head;
        this.body = body;
        this.date = date;
        this.stroke = stroke;
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

    public ArrayList<Stroke> getStroke() {
        return stroke;
    }
}
