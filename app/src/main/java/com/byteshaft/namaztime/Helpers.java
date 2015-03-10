package com.byteshaft.namaztime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
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

    public static String mPresentDate;
    private String mFajr;
    private String mDhuhr;
    private String mAsar;
    private String mMaghrib;
    private String mIsha;
    private String sDATE;
    private static Context mContext;
    JSONObject jsonObject;
    StringBuilder stringBuilder;
    String _data;
    String output = null;

    public Helpers(Context context) {
        mContext = context;
    }

    public  Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    public static NetworkInfo checkNetworkStatus() {
        ConnectivityManager connMgr = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connMgr.getActiveNetworkInfo();
    }

    public static void refreshDialoge(final Activity context) {
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

    private SimpleDateFormat getDateFormate() {
        return new SimpleDateFormat("yyyy-M-d");
    }

    private void getDate() {
        mPresentDate = getDateFormate().format(getCalenderInstance().getTime());
    }

    public void setTimesFromDatabase() {
        getDate();
        output = getPrayerTimesForDate(mPresentDate);
        try {
            jsonObject = new JSONObject(output);
            sDATE = jsonObject.get("date_for").toString();
            setPrayerTime(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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

    public void displayData() {
        MainActivity.textTime.setText(sDATE);
        MainActivity.textTime.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        MainActivity.textTime.setTextSize(20);
        MainActivity.text.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        MainActivity.text.setTextSize(20);
        MainActivity.textView.setTextSize(20);
        MainActivity.textView.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        MainActivity.textView.setText("Fajr" + "\n" + "\n"
                + "Dhuhr" + "\n" + "\n" + "Asar"
                + "\n" + "\n"+ "Maghrib" + "\n" + "\n"
                + "Isha");
        MainActivity.textView.setTextColor(Color.parseColor("#FFFFFF"));
        MainActivity.text.setText(mFajr + "\n" + "\n" +
                mDhuhr + "\n" + "\n"+ mAsar
                + "\n" + "\n"+ mMaghrib + "\n" + "\n"
                + mIsha);
        MainActivity.text.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private String getDataFromFileAsString() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = mContext.openFileInput(MainActivity.sFileName);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            stringBuilder = new StringBuilder();
            while (bufferedInputStream.available() != 0) {
                char characters = (char) bufferedInputStream.read();
                stringBuilder.append(characters);
            }
            bufferedInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private String getPrayerTimesForDate(String request) {
        try {
            _data = null;
            String data = getDataFromFileAsString();
            JSONArray readingData = new JSONArray(data);
            for (int i = 0; i < readingData.length(); i++) {
                _data = readingData.getJSONObject(i).toString();
                if (_data.contains(request)) {
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!_data.contains(request)) {
            new SystemManagement(mContext).execute();
        }
        return _data;
    }
}

