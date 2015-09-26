package com.amplify;
import com.google.gson.Gson;
/**
 * Created by lukesamora on 9/26/15.
 */
public class APIConnector {

    private final String songURI;
    private final int groupID;
    private final long time;

    public APIConnector(String id, int group, long time){
        this.songURI = id;
        this.groupID = group;
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public int getGroupID() {
        return groupID;
    }

    public String getSongURI() {

        return songURI;
    }

    public String getJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);

    }


}
