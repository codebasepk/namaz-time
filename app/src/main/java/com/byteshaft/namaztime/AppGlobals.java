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
}
