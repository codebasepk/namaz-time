package com.byteshaft.namaztime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Helpers {

    Context mContext;

    Calendar c = Calendar.getInstance();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d");
    String dDate = df.format(c.getTime());
    String Fajr;
    String Dhuhr;
    String Asar;
    String Maghrib;
    String Isha;
    JSONObject jsonObject;


    public Helpers(Context context) {
        mContext = context;
    }

    public void setTimesFromDatabase() throws InterruptedException, IOException, JSONException {

        String output = getPrayerTimesForDate(dDate);
        jsonObject = new JSONObject(output);
        MainActivity.sDATE = jsonObject.get("date_for").toString();
        Fajr = getPrayerTime(jsonObject, "fajr");
        Dhuhr = getPrayerTime(jsonObject, "dhuhr");
        Asar = getPrayerTime(jsonObject, "asr");
        Maghrib = getPrayerTime(jsonObject, "maghrib");
        Isha = getPrayerTime(jsonObject, "isha");

        displayData();
    }

    private void displayData() {
        MainActivity.sTextView.setText(MainActivity.sDATE + "\n"
                + "Fajr :" + Fajr + "\n"
                + "Dhuhr :" + Dhuhr + "\n"
                + "Asar :" + Asar + "\n"
                + "Maghrib :" + Maghrib + "\n"
                + "Isha :" + Isha);
    }

    public NetworkInfo checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }

    private String getDataFromFileAsString() throws IOException {
        FileInputStream fileInputStream = mContext.openFileInput(MainActivity.sFileName);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        StringBuilder stringBuilder = new StringBuilder();
        while (bufferedInputStream.available() != 0) {
            char characters = (char) bufferedInputStream.read();
            stringBuilder.append(characters);
        }
        bufferedInputStream.close();
        fileInputStream.close();
        return stringBuilder.toString();
    }

    private String getPrayerTimesForDate(String request) throws IOException, JSONException {
        String _data = null;
        String data = getDataFromFileAsString();
        JSONArray readingData = new JSONArray(data);
        for (int i = 0; i < readingData.length(); i++) {
            _data = readingData.getJSONObject(i).toString();
            if (_data.contains(request)) {
                break;
            }
        }

        return _data;
    }

    private String getPrayerTime(JSONObject jsonObject, String namaz) throws JSONException {
        return jsonObject.get(namaz).toString();
    }

    public void refreshDialoge(final Activity context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("No Internet");
        alert.setMessage("Connect to Internet & Press Ok");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (checkNetworkStatus() != null) {
                    new SystemManagement(context).execute();
                } else {
                    context.finish();
                }
            }
        });

        alert.show();
    }

}
