package com.byteshaft.namaztime;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
public class Helpers {

    static String mFajr;
    static String mDhuhr;
    static String mAsar;
    static String mMaghrib;
    static String mIsha;
    static String sDATE;
    private static Context mContext;
    JSONObject jsonObject;
    StringBuilder stringBuilder;
    String _data;


    public Helpers(Context context) {
        mContext = context;
    }

    public static Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    private SimpleDateFormat getDateFormate() {
        return new SimpleDateFormat("yyyy-M-d");
    }

    private String getDate() {
        return getDateFormate().format(getCalenderInstance().getTime());
    }

    public void setTimesFromDatabase() {

        String output = null;
            output = getPrayerTimesForDate(getDate());
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
        MainActivity.textView.setText("Fajr"+"\n"+"\n"+"Dhuhr"+"\n"+"\n"+"Asar"+"\n"+"\n"
                +"Maghrib"+"\n"+"\n"+"Isha");
        MainActivity.text.setText(mFajr + "\n"+"\n"+mDhuhr + "\n"+"\n"+mAsar
                + "\n"+"\n"+mMaghrib + "\n"+"\n"+mIsha);

    }

    private String getDataFromFileAsString()  {
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

    private String getPrayerTimesForDate(String request)  {
        try{
        _data = null;
        String data = getDataFromFileAsString();
        JSONArray readingData = new JSONArray(data);
        for (int i = 0; i < readingData.length(); i++) {
            _data = readingData.getJSONObject(i).toString();
            if (_data.contains(request)) {
                break;
            }

        }
        }catch(JSONException e){
            e.printStackTrace();

        }

        return _data;
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

}

