package com.amplify;

import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.amplify.util.Group;


public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    public static final String GROUP_ID_MESSAGE = "com.amplify.groupId";

    public static final String GROUP_NAME_MESSAGE = "com.amplify.name";

    private static final String CLIENT_ID = "ed720d10115a4fafabf398bbba3e1551";

    private static final String REDIRECT_URI = "amplify-android-success://callback";

    private static final String GROUP_URL = "https://www.google.com";

    private String FILENAME = "oAuth";

    private Player mPlayer;

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
        final TextView textView = (TextView)findViewById(R.id.song_text);
        textView.setTextSize(40);
        textView.setText("Hey bitch");
        //set all the groups by creating radio buttons and an add group
        registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                // This is sent with all broadcasts, regardless of type. The value is taken from
                // System.currentTimeMillis(), which you can compare to in order to determine how
                // old the event is.
                long timeSentInMs = intent.getLongExtra("timeSent", 0L);

                String action = intent.getAction();
                String trackId = "";
                if (action.equals(BroadcastTypes.METADATA_CHANGED)) {
                    trackId = intent.getStringExtra("id");
                    String artistName = intent.getStringExtra("artist");
                    String albumName = intent.getStringExtra("album");
                    String trackName = intent.getStringExtra("track");
                    int trackLengthInSec = intent.getIntExtra("length", 0);
                    textView.setText(trackId);
                    // Do something with extracted information...
                } else if (action.equals(BroadcastTypes.PLAYBACK_STATE_CHANGED)) {
                    boolean playing = intent.getBooleanExtra("playing", false);
                    int positionInMs = intent.getIntExtra("playbackPosition", 0);
                    // Do something with extracted information
                } else if (action.equals(BroadcastTypes.QUEUE_CHANGED)) {
                    // Sent only as a notification, your app may want to respond accordingly.
                }
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

    private void setAllGroups() {
        final Intent viewGroupIntent = new Intent(this, ViewGroupActivity.class);
        List<Group> allGroups = getAllGroups();
        TableLayout tableLayout = (TableLayout)findViewById(R.id.groupTable);
        //row layout for each individual row (match parent and wrap)
        TableRow.LayoutParams rowLayout = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.MATCH_PARENT);
        ViewGroup.LayoutParams rowItemLayout = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        final RadioGroup radioGroup = new RadioGroup(this);
        int i = 0;
        for (Group group : allGroups) {
            TableRow tableRow = new TableRow(this);
            tableRow.setLayoutParams(rowLayout);
            RadioButton radioButton = new RadioButton(this);
            radioButton.setLayoutParams(rowItemLayout);
            radioButton.setId(i);
            radioButton.setText(group.getName());
            radioButton.setTag(group.getId());
            radioGroup.addView(radioButton);
            tableRow.addView(radioButton);
            tableLayout.addView(tableRow);
        }
        Button button = new Button(this);
        button.setLayoutParams(rowItemLayout);
        button.setText("Join Selected Group!");
        View.OnClickListener clickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int radioButtonID = radioGroup.getCheckedRadioButtonId();
                //do nothing if no group is selected
                if (radioButtonID == -1) {
                    return;
                }
                //otherwise, go to the group with the params
                RadioButton radioButton = (RadioButton)radioGroup.findViewById(radioButtonID);
                String groupId = (String) radioButton.getTag();
                String groupName = (String) radioButton.getText();
                viewGroupIntent.putExtra(GROUP_NAME_MESSAGE, groupName);
                viewGroupIntent.putExtra(GROUP_ID_MESSAGE, groupId);
                startActivity(viewGroupIntent);
            }
        };
        tableLayout.addView(button);
        button.setOnClickListener(clickListener);
    }

    private List<Group> getAllGroups() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        //get all of the groups from the api
        List<Group> groups = new ArrayList<>();
        HttpGet getRequest = new HttpGet(GROUP_URL);
        getRequest.addHeader("accept", "application/json");
        HttpResponse response;
        try {
            response = httpClient.execute(getRequest);
        } catch (Exception e) {
            Log.e("MainActivity", "Could not get the groups from the api due to android issues");
            return groups;
        }
        if (response.getStatusLine().getStatusCode() != 200) {
            Log.e("MainActivity", "Bad response from api");
        }
        //parse the response entity as an array of json objects
        StringBuilder jsonStringBuilder = new StringBuilder();
        JSONArray jsonObject;
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader((response.getEntity().getContent())));
            String output;
            while ((output = br.readLine()) != null) {
                jsonStringBuilder.append(output);
            }
            jsonObject = new JSONArray(jsonStringBuilder.toString());
        } catch (IOException e) {
            Log.e("MainActivity", "Could not read the response from the server");
            return groups;
        } catch (JSONException e) {
            Log.e("MainActivity", "Could not parse JSON list of groups");
            return groups;
        }
        //go through the list of group objects and add them to an array
        for (int i = 0; i < jsonObject.length(); i++) {
            try {
                JSONObject groupJSON = jsonObject.getJSONObject(i);
                Group group = new Group(groupJSON.getString("name"), groupJSON.getString("id"),
                        groupJSON.getString("cur_son"));
                groups.add(group);
            } catch (JSONException e) {
                Log.e("MainActivity", "Could not parse individual json object");
            }
        }
        httpClient.getConnectionManager().shutdown();
        return groups;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
       // setAllGroups();
        // Check if result comes from the correct activity
        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse response = AuthenticationClient.getResponse(resultCode, intent);
            if (response.getType() == AuthenticationResponse.Type.TOKEN) {
                try {
                    //write the oAuthID to an internal file
                    FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                    fos.write(response.getAccessToken().getBytes());
                    fos.close();
                    Log.e("MainActivity", "Succesfully stored udid");
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
        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivity(intent);
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

