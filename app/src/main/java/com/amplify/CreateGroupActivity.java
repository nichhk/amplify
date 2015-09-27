package com.amplify;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import org.apache.http.client.*;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

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
        String s;
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

    }

    private void sendGroupToService(String groupName, String oAuth) {

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
