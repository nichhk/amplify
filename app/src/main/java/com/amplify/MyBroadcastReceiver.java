package com.amplify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

    private APIConnector apiConnector = new APIConnector();

    public Context context;

    public MyBroadcastReceiver(Context context) {
        super();
        this.context = context;
    }

    static final class BroadcastTypes {
        static final String SPOTIFY_PACKAGE = "com.spotify.music";
        static final String PLAYBACK_STATE_CHANGED = SPOTIFY_PACKAGE + ".playbackstatechanged";
        static final String QUEUE_CHANGED = SPOTIFY_PACKAGE + ".queuechanged";
        static final String METADATA_CHANGED = SPOTIFY_PACKAGE + ".metadatachanged";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // This is sent with all broadcasts, regardless of type. The value is taken from
        // System.currentTimeMillis(), which you can compare to in order to determine how
        // old the event is.
        Log.d("MyBroadcastReceiver", "here");
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
        Log.d("MainActivity", "got a new song playback");
        apiConnector.setSong(context, trackId, 1, positionInMs);
        Toast.makeText(context, trackId, Toast.LENGTH_LONG).show();
    }
}