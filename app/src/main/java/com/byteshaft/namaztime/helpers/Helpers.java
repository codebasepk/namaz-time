/*
 *
 *  * (C) Copyright 2015 byteShaft Inc.
 *  *
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser General Public License
 *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl-2.1.html
 *  
 */

package com.byteshaft.namaztime.helpers;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.byteshaft.namaztime.AppGlobals;
import com.byteshaft.namaztime.NamazTimesDownloadTask;
import com.byteshaft.namaztime.fragments.Home;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Helpers extends ContextWrapper {

    private final String SELECTED_CITY_POSITION = "cityPosition";
    private final String SELECTED_CITY_NAME = "cityName";
    private StringBuilder mStringBuilder = null;
    private String mData = null;

    public Helpers(Context context) {
        super(context);
    }

    Helpers(Activity activityContext) {
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

            }
        });
        alert.show();
    }

    private Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("dd MMM YYYY");
    }

    public String getDate() {
        return getDateFormat().format(getCalenderInstance().getTime());
    }

    String getAmPm() {
        return getTimeFormat().format(getCalenderInstance().getTime());
    }

    SimpleDateFormat getTimeFormat() {
        return new SimpleDateFormat("hh:mm aa");
    }

    public void setTimesFromDatabase(boolean runningFromActivity, String fileName) {
        String date = getDate();
        String output = getPrayerTimesForDate(date, runningFromActivity, fileName);
        Log.i("TAG", "output " + output);
        try {
            JSONObject jsonObject = new JSONObject(output);
            setPrayerTime(jsonObject, runningFromActivity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPrayerTime(JSONObject day, boolean runningFromActivity) throws JSONException {
        saveTimeForNamaz("Fajr", getPrayerTime(day, "Fajr"));
        saveTimeForNamaz("Dhuhr", getPrayerTime(day, "Dhuhr"));
        saveTimeForNamaz("Asr", getPrayerTime(day, "Asr"));
        saveTimeForNamaz("Maghrib", getPrayerTime(day, "Maghrib"));
        saveTimeForNamaz("Isha", getPrayerTime(day, "Isha"));
        if (runningFromActivity) {
            displayData();
        }
    }

    private String getPrayerTime(JSONObject jsonObject, String namaz) throws JSONException {
        return jsonObject.get(namaz).toString();
    }

    void displayData() {
        String currentCity = getPreviouslySelectedCityName();
        UiUpdateHelpers uiUpdateHelpers = new UiUpdateHelpers(Home.getInstance().getActivity());
        uiUpdateHelpers.setDate(getDate());
        uiUpdateHelpers.setCurrentCity(toTheUpperCaseSingle(currentCity));
        uiUpdateHelpers.displayDate(getAmPm());
        uiUpdateHelpers.setNamazNames("Fajr" + "\n" + "\n"
                + "Dhuhr" + "\n" + "\n" + "Asar"
                + "\n" + "\n" + "Maghrib" + "\n" + "\n"
                + "Isha");
        uiUpdateHelpers.setNamazTimesLabel(
                retrieveTimeForNamazAndTime("Fajr") + "\n" + "\n" +
                        retrieveTimeForNamazAndTime("Dhuhr") + "\n" + "\n" +
                        retrieveTimeForNamazAndTime("Asr") + "\n" + "\n" +
                        retrieveTimeForNamazAndTime("Maghrib") + "\n" + "\n" +
                        retrieveTimeForNamazAndTime("Isha"));
    }

    private String getDataFromFileAsString(String fileName) {
        FileInputStream fileInputStream;
        try {
            fileInputStream = openFileInput(fileName);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            mStringBuilder = new StringBuilder();
            while (bufferedInputStream.available() != 0) {
                char characters = (char) bufferedInputStream.read();
                mStringBuilder.append(characters);
            }
            bufferedInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return mStringBuilder.toString();
    }

    private String getPrayerTimesForDate(String request, boolean runningFromActivity, String fileName) {
        try {
            mData = null;
            String data = getDataFromFileAsString(fileName);
            JSONArray readingData = new JSONArray(data);
            for (int i = 0; i < readingData.length(); i++) {
                JSONObject jsonObject = readingData.getJSONObject(i);
                String dateFromData = jsonObject.getJSONObject("date").getString("readable");
                SimpleDateFormat simpleDateFormat =  new SimpleDateFormat("dd MMM YYYY");
                Date fromData = simpleDateFormat.parse(dateFromData);
                Date todaysDate = simpleDateFormat.parse(request);
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTime(fromData);
                cal2.setTime(todaysDate);
                boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
                if (sameDay) {
                    mData = jsonObject.getJSONObject("timings").toString();
                    return mData;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (runningFromActivity) {
            if (!mData.contains(request) && isNetworkAvailable() || mData.length() == 0) {
                NamazTimesDownloadTask namazTimesDownloadTask = new NamazTimesDownloadTask(this);
                namazTimesDownloadTask.downloadNamazTime();
            } else if (isNetworkAvailable() && !mData.contains(request)) {
                showInternetNotAvailableDialog();
            }
        }
        return mData;
    }

    private SharedPreferences getPreferenceManager() {
        return PreferenceManager.getDefaultSharedPreferences(this);
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

    String[] getNamazTimesArray() {
        return new String[]{
                retrieveTimeForNamazAndTime("Fajr"),
                retrieveTimeForNamazAndTime("Dhuhr"),
                retrieveTimeForNamazAndTime("Asr"),
                retrieveTimeForNamazAndTime("Maghrib"),
                retrieveTimeForNamazAndTime("Isha")
        };
    }

    String toTheUpperCaseSingle(String givenString) {
        String example = givenString;

        example = example.substring(0, 1).toUpperCase()
                + example.substring(1, example.length());
        return example;
    }

    private void saveTimeForNamaz(String namaz, String time) {
        SharedPreferences preference = getPreferenceManager();
        preference.edit().putString(namaz, time).apply();
        preference.edit().putString("date", getDate()).apply();
    }

    public String retrieveTimeForNamazAndTime(String namaz) {
        SharedPreferences preference = getPreferenceManager();
        String time =  preference.getString(namaz, null);
        if (!time.contains(getDate())) {
            SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
            SimpleDateFormat sdfs = new SimpleDateFormat("hh:mm aa");
            Date dt;
            String namazTime = null;
            Log.i("Helpers", "time " + time);
            Log.i("Helpers", "time " + time.split(" ")[0]);
            try {
                dt = sdf.parse(time.split(" ")[0]);
                namazTime = sdfs.format(dt);
                System.out.println("Time Display: " + sdfs.format(dt)); // <-- I got result here
            } catch (ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return namazTime;
        } else {
            return time;
        }
    }

    public static boolean locationEnabled() {
        LocationManager lm = (LocationManager) AppGlobals.getContext()
                .getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        return gps_enabled || network_enabled;
    }

    public static void dialogForLocationEnableManually(final Activity activity) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setMessage("Location is not enabled");
        dialog.setPositiveButton("Turn on", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivityForResult(myIntent, AppGlobals.LOCATION_ENABLE);
                //get gps
            }
        });
        dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // TODO Auto-generated method stub

            }
        });
        dialog.show();
    }

    public static boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) AppGlobals.getContext().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

}

