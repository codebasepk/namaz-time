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
    static String mFajr;
    static String mDhuhr;
    static String mAsar;
    static String mMaghrib;
    static String mIsha;
    JSONObject jsonObject;


    public static Calendar getCalenderInstance(){
        return Calendar.getInstance();
    }
    private SimpleDateFormat getDateFormate(){
        return new  SimpleDateFormat("yyyy-M-d");
    }
    private String getDate(){
        return getDateFormate().format(getCalenderInstance().getTime());
    }
    public static  String getAmPm(){
        return getTimeFormate().format(getCalenderInstance().getTime());
    }
    public static SimpleDateFormat getTimeFormate(){
        return new  SimpleDateFormat("h:m");
    }
    public Helpers(Context context) {
        mContext = context;
    }

    public void setTimesFromDatabase() throws InterruptedException, IOException, JSONException {

        String output = getPrayerTimesForDate(getDate());
        jsonObject = new JSONObject(output);
        MainActivity.sDATE = jsonObject.get("date_for").toString();
        setPrayerTime(jsonObject);
    }

    private void setPrayerTime(JSONObject day) throws JSONException {
        mFajr = getPrayerTime(day, "fajr");
        mDhuhr = getPrayerTime(day, "dhuhr");
        mAsar = getPrayerTime(day, "asr");
        mMaghrib = getPrayerTime(day, "maghrib");
        mIsha = getPrayerTime(day, "isha");

        displayData();
    }

    private String getPrayerTime(JSONObject jsonObject, String namaz) throws JSONException {
        return jsonObject.get(namaz).toString();
    }

    private void displayData() {
        MainActivity.sTextView.setText(MainActivity.sDATE + "\n"
                + "Fajr :" + mFajr + "\n"
                + "Dhuhr :" + mDhuhr + "\n"
                + "Asar :" + mAsar + "\n"
                + "Maghrib :" + mMaghrib + "\n"
                + "Isha :" + mIsha);

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
    public NetworkInfo checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
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

