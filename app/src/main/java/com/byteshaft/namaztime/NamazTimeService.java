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

    private final String CONSTANT_TIME_LEFT = "0:-10";
    private String diff = null;
    private String mFajr = null;
    private String mDhuhr = null;
    private String mAsar = null;
    private String mMaghrib = null;
    private String mIsha = null;
    private StringBuilder stringBuilder = null;
    private String _data = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final NamazNotification namazNotification = new NamazNotification(this);
        setTimesFromDatabase();
        Timer updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            public void run() {
                try {
                    String namazTimeArr[] = {mFajr, mDhuhr, mAsar, mMaghrib, mIsha};

                    for (String i : namazTimeArr) {
                        Date date1 = getTimeFormat().parse(getAmPm());
                        Date date2 = getTimeFormat().parse(i);
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
                                namazNotification.NamazNotificationStart(i);
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

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Calendar getCalenderInstance() {
        return Calendar.getInstance();
    }

    private String getAmPm() {
        return getTimeFormat().format(getCalenderInstance().getTime());
    }

    private SimpleDateFormat getTimeFormat() {
        return new SimpleDateFormat("h:mm aa");
    }

    private SimpleDateFormat getDateFormat() {
        return new SimpleDateFormat("yyyy-M-d");
    }

    private String getDate() {
        return getDateFormat().format(getCalenderInstance().getTime());
    }

    private void setTimesFromDatabase() {
        String output;
        output = getPrayerTimesForDate(getDate());
        try {
            JSONObject jsonObject = new JSONObject(output);
            setPrayerTime(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setPrayerTime(JSONObject day) {
        mFajr = getPrayerTime(day, "fajr");
        mDhuhr = getPrayerTime(day, "dhuhr");
        mAsar = getPrayerTime(day, "asr");
        mMaghrib = getPrayerTime(day, "maghrib");
        mIsha = getPrayerTime(day, "isha");
    }

    private String getPrayerTime(JSONObject jsonObject, String namaz) {
        try {
            return jsonObject.get(namaz).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getDataFromFileAsString() {
        FileInputStream fileInputStream;
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


