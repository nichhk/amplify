package com.amplify;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CreateGroupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_create_group, menu);
        return true;
    }

    public void sendGroup(View view) {
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
        String oAuth = builder.toString();
        Map<String, String> params = new HashMap<>();
        params.put("oauth", oAuth);
        params.put("name", groupName);
        Log.d("CreateGroupActivity", "oauth " + oAuth);
        Log.d("CreateGroupActivity", "groupname " + groupName);
        sendGroupToService(params, "https://shrouded-tundra-5129.herokuapp.com/group/create/");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
                            String groupId = response.getString("groupId");
                            FileOutputStream fos = openFileOutput("groupId", Context.MODE_PRIVATE);
                            fos.write(groupId.getBytes());
                            fos.close();
                            Log.d("CreateGroupActivity", "Created groupId in file");
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
