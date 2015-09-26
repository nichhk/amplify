package com.amplify;
import android.app.DownloadManager;
import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.net.*;
import java.util.Map;

import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.json.*;

import javax.json.Json;

/**
 * Created by lukesamora on 9/26/15.
 */
public class APIConnector {

    //endpoint subject to change
    private final static String url = "www.amplify.com/play";
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

    public int getGroupID() {return groupID;}

    public String getSongURI() {return songURI;}


    public String getJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);

    }

    /**
     *Posts the JSON data to the server from the spotify side.
     * @param context The Current Activity that is posting the data
     */
    public void postData(Context context){
        try {
            JSONObject json = new JSONObject(this.getJSON());
            RequestQueue queue = Volley.newRequestQueue(context);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d("APIConnector", "Something went wrong");
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.d("APIConnector", "Something went wrong");
                }
            });
            queue.add(request);

        }
        catch(JSONException e){
            Log.d("APIConnector", "Could not parse JSON from string");
        }

    }




}
