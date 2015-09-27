package com.amplify;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
        params.put("oAuth", oAuth);
        params.put("name", groupName);
        Log.d("CreateGroup oAuth", oAuth);
        Log.d("CreateGroup groupName", groupName);
        sendGroupToService(params, "https://www.google.com");
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    private void sendGroupToService(Map<String, String> params, String path) {
        //instantiates httpclient to make request
        DefaultHttpClient httpclient = new DefaultHttpClient();

        //url with the post data
        HttpPost httpost = new HttpPost(path);

        //convert parameters into JSON object
        JSONObject holder = new JSONObject(params);

        StringEntity se = null;
        try {
            //passes the results to a string builder/entity
            se = new StringEntity(holder.toString());
        } catch (UnsupportedEncodingException e) {
            Log.e("CreateGroupActivity", "Could not create the string entity");
        }

        //sets the post request as the resulting string
        httpost.setEntity(se);
        //sets a request header so the page receving the request
        //will know what to do with it
        httpost.setHeader("Accept", "application/json");
        httpost.setHeader("Content-type", "application/json");

        //Handles what is returned from the page
        ResponseHandler responseHandler = new BasicResponseHandler();
        try {
            CloseableHttpResponse response = (CloseableHttpResponse)httpclient.execute(httpost, responseHandler);
        } catch (Exception e) {
            Log.e("CreateGroupActivity", "Could not executre the http put");
        }

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
