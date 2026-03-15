package com.procollegia;

public class TimetableEvent {
    public static final int TYPE_CLASS = 0;
    public static final int TYPE_BREAK = 1;

    public int type;
    public String time;
    public String title;
    // Only for TYPE_CLASS
    public String room;
    public String professor;
    public String colorHex;
    
    // Constructor for Class
    public TimetableEvent(String time, String title, String room, String professor, String colorHex) {
        this.type = TYPE_CLASS;
        this.time = time;
        this.title = title;
        this.room = room;
        this.professor = professor;
        this.colorHex = colorHex;
    }

    // Constructor for Break
    public TimetableEvent(String time, String title) {
        this.type = TYPE_BREAK;
        this.time = time;
        this.title = title;
    }
}
