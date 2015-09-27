package com.amplify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewGroupActivity extends AppCompatActivity {

    private final String url = "https://shrouded-tundra-5129.herokuapp.com/group/join/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_group);
        TextView title = (TextView) findViewById(R.id.groupTitle);
        Intent intent = getIntent();
        title.setText(intent.getStringExtra(MainActivity.GROUP_NAME_MESSAGE));
        Map<String, String> params = new HashMap<String, String>();
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
        params.put("oauth", builder.toString());
        params.put("groupId", intent.getStringExtra(MainActivity.GROUP_ID_MESSAGE));
        sendGroupToService(params, url);
    }

    private void sendGroupToService(Map<String, String> params, final String path) {
        final JSONObject json = new JSONObject(params);
        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, path, json,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("APIConnector", "Successfully posted " + json.toString() + " to " + path);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
