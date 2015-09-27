package com.amplify;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import com.android.volley.toolbox.Volley;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.PlayConfig;
import com.spotify.sdk.android.player.Spotify;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


import com.amplify.util.Group;


public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    public static final String GROUP_ID_MESSAGE = "com.amplify.groupId";

    public static final int radioId = 18;

    public static final String GROUP_NAME_MESSAGE = "com.amplify.name";

    private static final String CLIENT_ID = "ed720d10115a4fafabf398bbba3e1551";

    private static final String REDIRECT_URI = "amplify-android-success://callback";

    private static final String GROUP_URL = "https://shrouded-tundra-5129.herokuapp.com/group/list/";

    private String FILENAME = "oAuth";

    private String curSong;

    private Random rand = new Random();

    private Player mPlayer;

    BroadcastReceiver receiver;

    private APIConnector apiConnector = new APIConnector();

    private static final int REQUEST_CODE = 1337;

    private Timer timer = new Timer();

    private  TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.MATCH_PARENT);

    private ViewGroup.LayoutParams rowItemLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);


    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        receiver = new MyBroadcastReceiver(getBaseContext());
        registerReceiver(receiver, new IntentFilter(BroadcastTypes.METADATA_CHANGED));
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);

    }

    private void setAllGroups(List<Group> allGroups) {
        final Intent viewGroupIntent = new Intent(this, ViewGroupActivity.class);
        TableLayout tableLayout = (TableLayout)findViewById(R.id.groupTable);
        //row layout for each individual row (match parent and wrap)
        final RadioGroup radioGroup;
        radioGroup = new RadioGroup(this);
        radioGroup.setId(radioId);

        Log.d("MainActivity", Integer.toString(allGroups.size()));
        //add all the radio button groups
        for (int i = 0; i < allGroups.size(); i++) {
            Group group = allGroups.get(i);
            RadioButton radioButton = new RadioButton(this);
            radioButton.setLayoutParams(rowItemLayout);
            radioButton.setId(i);
            radioButton.setText(group.getName());
            radioButton.setTag(group.getId());
            radioGroup.addView(radioButton);
        }
        //add the radio group to the table layout
        tableLayout.addView(radioGroup);
        //only add the join group button if there is some group to join
        if (allGroups.size() > 0) {
            Button button = new Button(this);
            button.setLayoutParams(rowItemLayout);
            button.setText("Join Selected Group!");
            //create the listener to join the selected group
            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int radioButtonID = radioGroup.getCheckedRadioButtonId();
                    //do nothing if no group is selected
                    if (radioButtonID == -1) {
                        return;
                    }
                    //otherwise, go to the group with the params
                    RadioButton radioButton = (RadioButton) radioGroup.findViewById(radioButtonID);
                    String groupId = (String) radioButton.getTag();
                    String groupName = (String) radioButton.getText();
                    viewGroupIntent.putExtra(GROUP_NAME_MESSAGE, groupName);
                    viewGroupIntent.putExtra(GROUP_ID_MESSAGE, groupId);
                    //
                    try {
                        FileOutputStream fos = openFileOutput("isMaster", Context.MODE_PRIVATE);
                        fos.write("false".getBytes());
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //clear all of the radio buttons
                    startActivity(viewGroupIntent);
                }
            };
            tableLayout.addView(button);
            button.setOnClickListener(clickListener);
        }
    }

    private List<Group> getAllGroups() {
        final List<Group> groups = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, GROUP_URL,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("MainActivity", "got all the groups");
                        //go through the list of group objects and add them to an array
                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject groupJSON = response.getJSONObject(i);
                                Group group = new Group(groupJSON.getString("name"), groupJSON.getString("id"),
                                        groupJSON.getString("song"));
                                groups.add(group);
                            } catch (JSONException e) {
                                Log.e("MainActivity", "Could not parse individual json object");
                            }
                        }
                        setAllGroups(groups);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("APIConnector", "Something went wrong");
                    }
                });
        queue.add(request);

        Log.d("MainActivity", "returning the groups");
        return groups;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        getAllGroups();
        // Check if result comes from the correct activity

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                try {
                    //write the oAuthID to an internal file
                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(response.getAccessToken().getBytes());
                    fos.close();
                    Log.d("MainActivity", "Succesfully stored udid");
                } catch (IOException e) {
                    Log.e("MainActivity","Could not write udid to file " + e.getMessage());
                }
                Config playerConfig = new Config(this, response.getAccessToken(), CLIENT_ID);
                mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                    @Override
                    public void onInitialized(Player player) {
                        mPlayer.addConnectionStateCallback(MainActivity.this);
                        mPlayer.addPlayerNotificationCallback(MainActivity.this);
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                poll();
                            }
                        }, 0, 4000);
                    }
                    @Override
                    public void onError(Throwable throwable) {
                        Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                    }
                });
            }
        }
    }

    public void createGroup(View view) {
        Log.d("CreateGroupAcitivty", "creating a group");
        EditText editText = (EditText) findViewById(R.id.groupName);
        String groupName = editText.getText().toString();
        StringBuilder builder = new StringBuilder();
        try {
            FileInputStream fis = openFileInput("oAuth");
            int ch;
            while((ch = fis.read()) != -1){
                builder.append((char)ch);
            }
        } catch (IOException e) {
            Log.e("CreateGroupActivity", "Could not open oAuth file");
        }
        Map<String, String> params = new HashMap<>();
        params.put("android_id", Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID));
        params.put("name", groupName);
        Log.d("CreateGroupActivity", "groupname " + groupName);
        sendGroupToService(params, "https://shrouded-tundra-5129.herokuapp.com/group/create/");
    }

    private void sendGroupToService(final Map<String, String> params, final String path) {
        final JSONObject json = new JSONObject(params);
        final RadioGroup radioGroup;
        if (findViewById(radioId) == null) {
            radioGroup = new RadioGroup(this);
            radioGroup.setId(radioId);
        } else {
            radioGroup = (RadioGroup)findViewById(radioId);
        }
        final RadioButton radioButton = new RadioButton(this);
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, path, new Response.Listener<String>() {
            public void onResponse(String groupId) {
                Log.d("MainActivity", groupId);
                try {
                    //write the group id, and write true for isMaster
                    FileOutputStream fos = openFileOutput("groupId", Context.MODE_PRIVATE);
                    fos.write(groupId.getBytes());
                    fos = openFileOutput("isMaster", Context.MODE_PRIVATE);
                    fos.write("true".getBytes());
                    fos.close();
                    radioButton.setId(rand.nextInt(10000000) + 101);
                    radioButton.setText(params.get("name"));
                    radioButton.setTag(groupId);
                    radioGroup.addView(radioButton);
                    Log.d("MainActivity", "adding new radio button");
                    Log.d("CreateGroupActivity", "Created groupId in file and isMaster");
                } catch (IOException e) {

                }
            }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            }){
            @Override
            protected Map<String,String> getParams(){
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type","application/x-www-form-urlencoded");
                return params;
            }
        };
        queue.add(request);
    }


    public void poll() {
        apiConnector.getUserInfo(getBaseContext(), new CallBack(){
            public void callback(JSONObject response){
                boolean isMaster = false;
                String group = "-1";
                try {
                    Log.d("MainActivity", "****** " + response.toString());
                    isMaster = response.getBoolean("is_master");
                    group = response.getString("group");
                }catch(Exception e) {
                    Log.e("MainActivity", e.getClass().toString());
                }
                Log.d("MainActivity", Boolean.toString(isMaster));
                if (group != null && !"-1".equals(group) && !isMaster) {
                    RequestQueue queue = Volley.newRequestQueue(MainActivity.this);
                    JsonObjectRequest request = new JsonObjectRequest("https://shrouded-tundra-5129.herokuapp.com/group/get-song?group="+ group,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    try {
                                        final String song = (String) response.get("song");
                                        Log.d("MainActivity", "playing " + song);
                                        if (song != null && !song.equals(curSong)) {
                                            curSong = song;
                                            Log.d("MainActivity", "time = " + Long.toString(Math.round((double) response.get("start")) - System.currentTimeMillis()));
                                            final CountDownTimer songTimer = new CountDownTimer(Math.round((double) response.get("start")) - System.currentTimeMillis(), Long.MAX_VALUE){
                                                @Override
                                                public void onTick(long millisUntilFinished) {

                                                }
                                                @Override
                                                public void onFinish(){
                                                    mPlayer.play(PlayConfig.createFor(song).withInitialPosition(1500));
                                                    cancel();
                                                }
                                            }.start();
                                            Log.d("Main Activity", "Song should be playing!");
                                            Log.d("Main Activity", response.toString());
                                        }

                                    } catch (JSONException e) {
                                        Log.d("Main Activity", "Did not find a song URI!");
                                    }

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Main Activity", "There was an error in the response!!!");
                                }
                            });
                    queue.add(request);
                }
            }
        });

    }


    @Override
    public void onLoggedIn() {
        Log.d("MainActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("MainActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable error) {
        Log.d("MainActivity", "Login failed");
    }

    @Override
    public void onTemporaryError() {
        Log.d("MainActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String message) {
        Log.d("MainActivity", "Received connection message: " + message);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("MainActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String errorDetails) {
        Log.d("MainActivity", "Playback error received: " + errorType.name());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
        Spotify.destroyPlayer(this);
    }
}

