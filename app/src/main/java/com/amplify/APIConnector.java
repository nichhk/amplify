package com.amplify;
import android.app.DownloadManager;
import android.content.Context;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.Map;

import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lukesamora on 9/26/15.
 */
public class APIConnector {

    //endpoint subject to change
    private final static String baseUrl = "https://shrouded-tundra-5129.herokuapp.com/";
    private final static String setSongUrl = baseUrl + "group/set-song/";
    private final static String getUserInfoUrl = baseUrl  + "user/detail/";


    public APIConnector(){

    }

    public String getJSON(){
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public JsonObject setSong(Context context, String songId,  int groupId, int playbackPosition){
        Gson gson = new Gson();
        JsonObject json = new JsonObject();
        json.addProperty("song", songId);
        json.addProperty("group", groupId);
        json.addProperty("playbackPosition",playbackPosition);
        String jsonString = gson.toJson(json);
        Log.d("APIConnector", "Hello");


        try {
            postData(context, setSongUrl, new JSONObject(jsonString));
        }catch (Exception e){

        }
        return new JsonObject();
    }

    public void getUserInfo(Context context, CallBack callBack){
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        try {
            getData(context, getUserInfoUrl + uuid, new JSONObject(), callBack);
        }catch (Exception e){
        }

    }



    /**
     *Posts the JSON data to the server from the spotify side.
     * @param context The Current Activity that is posting the data
     */
    // TODO: Add in timer to see lag for media playback
    public void postData(Context context, String url, JSONObject json){
        RequestQueue queue = Volley.newRequestQueue(context);
        Log.d("APIConnector", "Am here");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("APIConnector", "It went right");
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("APIConnector", "Something went wrong");
                    }
                });
        queue.add(request);
    }


    /**
     *Posts the JSON data to the server from the spotify side.
     * @param context The Current Activity that is posting the data
     */
    // TODO: Add in timer to see lag for media playback
    public void getData(Context context, String url, JSONObject json, final CallBack callBack){
        RequestQueue queue = Volley.newRequestQueue(context);
        final JSONObject[] js = new JSONObject[1];
        Log.d("APIConnector", "Am here");
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("APIConnect", "went well");
                callBack.callback(response);
            }
        },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("APIConnector", "Something went wrong");
                    }
                });
        queue.add(request);
    }

}