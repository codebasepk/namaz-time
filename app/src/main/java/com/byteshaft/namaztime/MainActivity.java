package com.byteshaft.namaztime;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity {

    private static final String TAG = "Activity";
    final String fileName = "namazTime";
    TextView textView, textViewTwo;
    String DbData;
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-M-dd");
    String dDate = df.format(c.getTime());
    String matchedObj;
    String FAJR;
    String DHUHR;
    String ASAR;
    String MAGHRIB;
    String ISHA;
    String DATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textViewTwo = (TextView) findViewById(R.id.textViewTwo);

        try {
            gettingDataFromDb();
            Log.d(TAG, "reading data is not working");
        } catch (IOException | JSONException | InterruptedException e) {
            e.printStackTrace();
        }
        if (DATE == null && checkNetworkStatus() != null) {
            new SystemManagement(this).execute();
        } else if (checkNetworkStatus() == null && DATE == null) {
            Toast.makeText(this, "No internet Connection", Toast.LENGTH_LONG).show();
        } else {
            displayData();
        }
    }

    public void gettingDataFromDb() throws InterruptedException, IOException, JSONException {

        FileInputStream readFile = openFileInput(fileName);
        BufferedInputStream bis = new BufferedInputStream(readFile);
        StringBuilder stringBuilder = new StringBuilder();
        while (bis.available() != 0) {
            char characters = (char) bis.read();
            stringBuilder.append(characters);
            Log.d(TAG, "error in while loop");
        }
        bis.close();
        readFile.close();
        // deserializing the content...
        JSONArray readingData = new JSONArray(stringBuilder.toString());
        StringBuilder combineNew = new StringBuilder();

        for (int i = 0; i < readingData.length(); i++) {
            String loop = readingData.getJSONObject(i).get("date_for").toString();
            Log.e(TAG, "todays date not found");
            if (loop.matches(dDate)) {
                matchedObj = readingData.getJSONObject(i).toString().trim();
                if (matchedObj.contains(dDate)) {
                    Log.d(TAG, "cannot find specific namaz");
                    DATE = readingData.getJSONObject(i).get("date_for").toString();
                    FAJR = readingData.getJSONObject(i).get("fajr").toString();
                    DHUHR = readingData.getJSONObject(i).get("dhuhr").toString();
                    ASAR = readingData.getJSONObject(i).get("asr").toString();
                    MAGHRIB = readingData.getJSONObject(i).get("maghrib").toString();
                    ISHA = readingData.getJSONObject(i).get("isha").toString();
                }
            }
            DbData = combineNew.append(loop).append("\n").toString();
            Log.d(TAG, "error with append");

        }
    }

    private void displayData() {
        textView.setText(DATE + "\n"
                + "Fajr :"+ FAJR+ "\n"
                + "Dhuhr :" + DHUHR + "\n"
                + "Asar :" + ASAR + "\n"
                + "Maghrib :"+ MAGHRIB + "\n"
                + "Isha :" + ISHA);
    }

    public NetworkInfo checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }
}