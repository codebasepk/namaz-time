package com.byteshaft.namaztime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by s9iper1 on 6/8/17.
 */

public class AppGlobals extends Application {

    private static Context sContext;
    private static final String MASJIDLOCATIONS = "masjid_locations";
    private static final String AudioMode = "audio_mode";
    public static final int LOCATION_ENABLE = 10;
    private static final String MY_CITY = "my_city";
    private static final String MY_COUNTRY = "my_country";
    private static final String OPENED_MAPS_ONCE = "open_maps_once";

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
        FirebaseApp.initializeApp(getApplicationContext());
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    public static Context getContext() {
        return sContext;
    }

    public static SharedPreferences getPreferenceManager() {
        return getContext().getSharedPreferences("shared_prefs", MODE_PRIVATE);
    }

    public static void saveHashSet(Set<String> value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().remove(MASJIDLOCATIONS).apply();
        sharedPreferences.edit().putStringSet(MASJIDLOCATIONS, value).apply();
    }

    public static Set<String> getHashSet() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        Set<String> strings = new HashSet<>();
        return sharedPreferences.getStringSet(MASJIDLOCATIONS, strings);
    }

    public static void saveAudioManagerMode(int value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putInt(AudioMode, value).apply();
    }

    public static int getAudioMOde() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getInt(AudioMode, -1);
    }

    public static void savePersonCity(String  city) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(MY_CITY, city).apply();
    }

    public static String getPersonCity() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(MY_CITY, "");
    }

    public static void savePersonCountry(String country) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putString(MY_COUNTRY, country).apply();
    }

    public static String getPersonCountry() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getString(MY_COUNTRY, "");
    }

    public static void anyLocationSaved(boolean mapsOpened) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putBoolean(OPENED_MAPS_ONCE, mapsOpened).apply();
    }

    public static boolean isLocationSaved() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        return sharedPreferences.getBoolean(OPENED_MAPS_ONCE, false);
    }
}

