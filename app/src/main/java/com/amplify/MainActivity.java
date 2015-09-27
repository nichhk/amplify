package com.amplify;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.io.FileOutputStream;
import java.io.IOException;


public class MainActivity extends Activity implements
        PlayerNotificationCallback, ConnectionStateCallback {

    // TODO: Replace with your client ID
    private static final String CLIENT_ID = "ed720d10115a4fafabf398bbba3e1551";
    // TODO: Replace with your redirect URI
    private static final String REDIRECT_URI = "amplify-android-success://callback";

    String s;

    private String FILENAME = "oAuth";

    private Player mPlayer;

    private Button testRequest;

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


        

        /*
        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(CLIENT_ID,
                AuthenticationResponse.Type.TOKEN,
                REDIRECT_URI);
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        */
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
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
                        mPlayer.play("spotify:track:2TpxZ7JUBn3uw46aR7qd6V");
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

