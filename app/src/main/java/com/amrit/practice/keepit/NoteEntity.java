package com.amrit.practice.keepit;

public class NoteEntity {

    String id;
    String head;
    String body;
    long date;

    public NoteEntity(String id, String head, String body, long date) {
        this.id = id;
        this.head = head;
        this.body = body;
        this.date = date;
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

}
