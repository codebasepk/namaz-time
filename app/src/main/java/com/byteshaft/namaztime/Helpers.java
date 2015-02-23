package com.byteshaft.namaztime;

import android.content.Context;
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
    String FAJR;
    String DHUHR;
    String ASAR;
    String MAGHRIB;
    String ISHA;


    public Helpers(Context context) {
        mContext = context;
    }

    public void setTimesFromDatabase() throws InterruptedException, IOException, JSONException {

        String output = getPrayerTimesForDate(dDate);
        JSONObject o = new JSONObject(output);

        MainActivity.DATE = o.get("date_for").toString();
        FAJR = getPrayerTime(o, "fajr");
        DHUHR = getPrayerTime(o, "dhuhr");
        ASAR = getPrayerTime(o, "asr");
        MAGHRIB = getPrayerTime(o, "maghrib");
        ISHA = getPrayerTime(o, "isha");

        displayData();
    }

    private void displayData() {
        MainActivity.textView.setText(MainActivity.DATE + "\n"
                + "Fajr :" + FAJR + "\n"
                + "Dhuhr :" + DHUHR + "\n"
                + "Asar :" + ASAR + "\n"
                + "Maghrib :" + MAGHRIB + "\n"
                + "Isha :" + ISHA);
    }

    public NetworkInfo checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }

    private String getDataFromFileAsString() throws IOException {
        FileInputStream fileInputStream = mContext.openFileInput(MainActivity.fileName);
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
        for(int i = 0; i < readingData.length(); i++) {
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
}
