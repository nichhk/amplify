package com.amplify.util;

/**
 * Created by StuartWyrough on 9/26/15.
 */
public class Group {

    private String name;
    private String id;
    private String curSong;

    public Group(String name, String id, String curSong) {
        this.name = name;
        this.id = id;
        this.curSong = curSong;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getCurSong() {
        return curSong;
    }



}
