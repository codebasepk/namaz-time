package com.byteshaft.namaztime;


import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;


public class MainActivity extends Activity {

    final static String fileName = "namaztimes.txt";
    static TextView textView;
    static String DATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        Helpers helpers = new Helpers(this);

        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + fileName;
        File file = new File(location);
        if (!file.exists()) {
            if (helpers.checkNetworkStatus() != null) {
                new SystemManagement(this).execute();
            } else {
                Toast.makeText(this, "No internet Connection", Toast.LENGTH_LONG).show();
            }
        } else {
            try {
                helpers.setTimesFromDatabase();
            } catch (InterruptedException | IOException | JSONException e) {
                e.printStackTrace();
            }
        }
    }


}