package com.byteshaft.namaztime;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class NamazTimeService extends Service {
    private String diff;
    private final String CONSTANT_TIME_LEFT = "0:-10";
    private String mFajr;
    private String mDhuhr;
    private String mAsar;
    private String mMaghrib;
    private String mIsha;
    String sDATE;
    JSONObject jsonObject;
    StringBuilder stringBuilder;
    String _data;
    NamazNotification namazNotification = new NamazNotification(this);
    Timer updateTimer;

    public static Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    public static String getAmPm() {
        return getTimeFormate().format(getCalenderInstance().getTime());
    }

    public static SimpleDateFormat getTimeFormate() {
        return new SimpleDateFormat("h:mm aa");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setTimesFromDatabase();
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    String namazTimeArr[] = {mFajr, mDhuhr, mAsar,mMaghrib, mIsha};

                    for (String i : namazTimeArr) {
                        Date date1 = getTimeFormate().parse(getAmPm());
                        Date date2 = getTimeFormate().parse(i);
                        if (date1.before(date2)) {
                            long mills = date1.getTime() - date2.getTime();
                            Log.v("Data1", "" + date1.getTime());
                            Log.v("Data2", "" + date2.getTime());
                            int Hours = (int) (mills / (1000 * 60 * 60));
                            int Mins = (int) (mills / (1000 * 60)) % 60;
                            diff = Hours + ":" + Mins; // updated value every1 second
                            Log.v("TIME:", diff);
                            if (diff.equals(CONSTANT_TIME_LEFT)) {
                                Log.v("condition match", "" + diff);
                                namazNotification.startNamazNotification(i);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }, 0, 30000);
        return flags;
    }

    private SimpleDateFormat getDateFormate() {
        return new SimpleDateFormat("yyyy-M-d");
    }

    private String getDate() {
        return getDateFormate().format(getCalenderInstance().getTime());
    }

    private void setTimesFromDatabase() {
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
    }

    private String getPrayerTime(JSONObject jsonObject, String namaz) throws JSONException {
        return jsonObject.get(namaz).toString();
    }

    private String getDataFromFileAsString() {
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = this.openFileInput("namaztimes.txt");
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
        return _data;
    }
}


