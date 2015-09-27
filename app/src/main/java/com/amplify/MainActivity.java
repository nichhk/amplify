package com.amplify;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
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
import java.util.Map;

import com.amplify.util.Group;


public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    public static final String GROUP_ID_MESSAGE = "com.amplify.groupId";

    public static final String GROUP_NAME_MESSAGE = "com.amplify.name";

    private static final String CLIENT_ID = "ed720d10115a4fafabf398bbba3e1551";

    private static final String REDIRECT_URI = "amplify-android-success://callback";

    private static final String GROUP_URL = "https://shrouded-tundra-5129.herokuapp.com/group/list/";

    private String FILENAME = "oAuth";

    private Player mPlayer;

    private APIConnector apiConnector = new APIConnector();

    private static final int REQUEST_CODE = 1337;

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
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                // This is sent with all broadcasts, regardless of type. The value is taken from
                // System.currentTimeMillis(), which you can compare to in order to determine how
                // old the event is.
                long timeSentInMs = intent.getLongExtra("timeSent", 0L);
                int positionInMs = 0;
                String action = intent.getAction();
                String trackId = "";
                if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
                    trackId = intent.getStringExtra("id");
                    String artistName = intent.getStringExtra("artist");
                    String albumName = intent.getStringExtra("album");
                    String trackName = intent.getStringExtra("track");
                    int trackLengthInSec = intent.getIntExtra("length", 0);
                    // Do something with extracted information...
                } else if (action.equals(BroadcastTypes.PLAYBACK_STATE_CHANGED)) {
                    boolean playing = intent.getBooleanExtra("playing", false);
                    positionInMs = intent.getIntExtra("playbackPosition", 0);
                    // Do something with extracted information
                } else if (action.equals(BroadcastTypes.QUEUE_CHANGED)) {
                    // Sent only as a notification, your app may want to respond accordingly.
                }
                // TODO: Change the default from 1
                apiConnector.setSong(getBaseContext(),trackId, textView,1, positionInMs);
                Toast.makeText(context, trackId, Toast.LENGTH_LONG).show();
            }

        }, new IntentFilter(BroadcastTypes.METADATA_CHANGED));

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
        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams rowItemLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        final RadioGroup radioGroup = new RadioGroup(this);
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
                        //mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
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

    private void sendGroupToService(Map<String, String> params, final String path) {
        final JSONObject json = new JSONObject(params);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, path, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("CreateGroupActivity", "Successfully posted " + json.toString() + " to " + path);
                        try {
                            //write the group id, and write true for isMaster
                            String groupId = response.getString("groupId");
                            FileOutputStream fos = openFileOutput("groupId", Context.MODE_PRIVATE);
                            fos.write(groupId.getBytes());
                            fos = openFileOutput("isMaster", Context.MODE_PRIVATE);
                            fos.write("true".getBytes());
                            fos.close();
                            Log.d("CreateGroupActivity", "Created groupId in file and isMaster");
                        } catch (JSONException e) {
                            Log.e("CreateGroupActivity", "Could not parse response json");
                        } catch (IOException e) {
                            Log.e("CreateGroupActivity", "Could not write to groupId file");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("CreateGroupActivity", "Something went wrong");
                    }
                });
        queue.add(request);
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
        Spotify.destroyPlayer(this);
    }
}

