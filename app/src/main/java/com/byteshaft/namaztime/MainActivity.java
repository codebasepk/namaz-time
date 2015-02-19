package com.byteshaft.namaztime;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class MainActivity extends Activity {

    TextView textView, textViewTwo;
    int  launchCount = 0;
    private static boolean valueOfLaunchCountModified = false;
    NetworkInfo info;
    final String fileName = "namazTime";
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
    SharedPreferences preferences;

    private static final String TAG = "Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);
        textViewTwo = (TextView) findViewById(R.id.textViewTwo);

        try {
            gettingDataFromDb();
            Log.d(TAG, "reading data is not working");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (DATE == null && checkNetworkStatus() != null) {
            SystemManagement systemManagement = new SystemManagement(this);
            systemManagement.execute();
        } else {


            if (checkNetworkStatus() == null && DATE == null) {
                Toast.makeText(this, "No internet Connection", Toast.LENGTH_LONG).show();

            } else {

                try {
                    gettingDataFromDb();
                    Log.d(TAG, "reading data is not working");
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
            DbData = combineNew.append(loop + "\n").toString();
            Log.d(TAG, "error with append");

        }
    }

    private void displayData() {
        textView.setText(DATE + "\n"+"Fajr :" + FAJR + "\n" + "Dhuhr :" + DHUHR + "\n" + "Asar :" + ASAR + "\n" + "Maghrib :"
                + MAGHRIB + "\n" + "Isha :" + ISHA);
    }
    ///module for checking internet access
    public NetworkInfo  checkNetworkStatus(){

        final ConnectivityManager connMgr = (ConnectivityManager)
                this.getSystemService(Context.CONNECTIVITY_SERVICE);
        info = connMgr.getActiveNetworkInfo();
        return info;
    }
}