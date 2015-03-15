package com.byteshaft.namaztime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

public class Helpers extends ContextWrapper {

    public static String mPresentDate;
    public static String sDATE;
    private String mFajr;
    private String mDhuhr;
    private String mAsar;
    private String mMaghrib;
    private String mIsha;
    private StringBuilder stringBuilder;
    private String _data;
    private final String SELECTED_CITY_POSITION = "cityPosition";
    private final String SELECTED_CITY_NAME = "cityName";

    public Helpers(Context context) {
        super(context);
    }

    public Helpers(Activity activityContext) {
        super(activityContext);
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void showInternetNotAvailableDialog() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("No Internet");
        alert.setMessage("Please connect to the internet and try again");
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (isNetworkAvailable()) {
                    new NamazTimesDownloadTask(getApplicationContext()).execute();
                } else {
                    MainActivity.getInstance().finish();
                }
            }
        });
        alert.show();
    }

    public Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-M-d");
    }

    private void getDate() {
        mPresentDate = getDateFormat().format(getCalenderInstance().getTime());
    }

    public void setTimesFromDatabase() {
        getDate();
        String output = getPrayerTimesForDate(mPresentDate);
        try {
            JSONObject jsonObject = new JSONObject(output);
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
                + "\n" + "\n" + "Maghrib" + "\n" + "\n"
                + "Isha");
        MainActivity.textView.setTextColor(Color.parseColor("#FFFFFF"));
        MainActivity.text.setText(mFajr + "\n" + "\n" +
                mDhuhr + "\n" + "\n" + mAsar
                + "\n" + "\n" + mMaghrib + "\n" + "\n"
                + mIsha);
        MainActivity.text.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private String getDataFromFileAsString() {
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput(MainActivity.sFileName);
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
        if (!_data.contains(request) && isNetworkAvailable()) {

            new NamazTimesDownloadTask(this).execute();
        } else if (isNetworkAvailable() && !_data.contains(request)) {
            showInternetNotAvailableDialog();
        }
        return _data;
    }

    public String getDiskLocationForFile(String file) {
        return getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + file;
    }

    public SharedPreferences getPreferenceManager() {
        return getSharedPreferences("NAMAZ_TIME", Context.MODE_PRIVATE);
    }

    public String getPreviouslySelectedCityName() {
        SharedPreferences preferences = getPreferenceManager();
        return preferences.getString(SELECTED_CITY_NAME, "Karachi");
    }

    public int getPreviouslySelectedCityIndex() {
        SharedPreferences preferences = getPreferenceManager();
        return preferences.getInt(SELECTED_CITY_POSITION, 0);
    }

    public void setPreferenceForCityByIndex(int value) {
        SharedPreferences preferences = getPreferenceManager();
        preferences.edit().putInt(SELECTED_CITY_POSITION, value).apply();
    }

    public void saveSelectedCityName(String city) {
        SharedPreferences preferences = getPreferenceManager();
        preferences.edit().putString(SELECTED_CITY_NAME, city).apply();
    }
}

