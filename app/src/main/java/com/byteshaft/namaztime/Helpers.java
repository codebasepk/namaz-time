package com.byteshaft.namaztime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Helpers extends ContextWrapper {

    private static String mFajr = null;
    private static String mDhuhr = null;
    private static String mAsar = null;
    private static String mMaghrib = null;
    private static String mIsha = null;
    private StringBuilder stringBuilder = null;
    private String _data = null;
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
                MainActivity.getInstance().finish();
            }
        });
        alert.show();
    }

    private Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-M-d");
    }

    public String getDate() {
        return getDateFormat().format(getCalenderInstance().getTime());
    }

    public String getAmPm() {
        return getTimeFormat().format(getCalenderInstance().getTime());
    }

    public SimpleDateFormat getTimeFormat() {
        return new SimpleDateFormat("h:mm aa");
    }

    public void setTimesFromDatabase(boolean runningFromActivity) {
        String date = getDate();
        String output = getPrayerTimesForDate(date, runningFromActivity);
        try {
            JSONObject jsonObject = new JSONObject(output);
            setPrayerTime(jsonObject, runningFromActivity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPrayerTime(JSONObject day, boolean runningFromActivity) throws JSONException {
        mFajr = getPrayerTime(day, "fajr");
        mDhuhr = getPrayerTime(day, "dhuhr");
        mAsar = getPrayerTime(day, "asr");
        mMaghrib = getPrayerTime(day, "maghrib");
        mIsha = getPrayerTime(day, "isha");
        if (runningFromActivity) {
            displayData();
        }
    }

    private String getPrayerTime(JSONObject jsonObject, String namaz) throws JSONException {
        return jsonObject.get(namaz).toString();
    }

    public void displayData() {
        UiUpdateHelpers uiUpdateHelpers = new UiUpdateHelpers(MainActivity.getInstance());
        uiUpdateHelpers.setDate(getDate());
        uiUpdateHelpers.setNamazNames("Fajr" + "\n" + "\n"
                + "Dhuhr" + "\n" + "\n" + "Asar"
                + "\n" + "\n" + "Maghrib" + "\n" + "\n"
                + "Isha");
        uiUpdateHelpers.setNamazTimesLabel(mFajr + "\n" + "\n" +
                mDhuhr + "\n" + "\n" + mAsar
                + "\n" + "\n" + mMaghrib + "\n" + "\n"
                + mIsha);
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

    private String getPrayerTimesForDate(String request, boolean runningFromActivity) {
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
        if (runningFromActivity) {
            if (!_data.contains(request) && isNetworkAvailable()) {
                new NamazTimesDownloadTask(this).execute();
            } else if (isNetworkAvailable() && !_data.contains(request)) {
                showInternetNotAvailableDialog();
            }
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

    public void saveSelectedCity(String cityName, int positionInSpinner) {
        SharedPreferences preferences = getPreferenceManager();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(SELECTED_CITY_NAME, cityName);
        editor.putInt(SELECTED_CITY_POSITION, positionInSpinner);
        editor.apply();
    }

    public void writeDataToFile(String file, String data) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = openFileOutput(file, Context.MODE_PRIVATE);
            fileOutputStream.write(data.getBytes());
            fileOutputStream.close();
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public String[] getNamazTimesArray() {
        return new String[]{mFajr, mDhuhr, mAsar, mMaghrib, mIsha};
    }
}

