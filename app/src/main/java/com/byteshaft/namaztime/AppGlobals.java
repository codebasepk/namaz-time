package com.byteshaft.namaztime;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by s9iper1 on 6/8/17.
 */

public class AppGlobals extends Application {

    private static Context sContext;
    private static final String MaSJIDLOCATIONS = "masjid_locations";
    public static final int LOCATION_ENABLE = 10;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }

    public static SharedPreferences getPreferenceManager() {
        return getContext().getSharedPreferences("shared_prefs", MODE_PRIVATE);
    }

    public static void saveHashSet(Set<String> value) {
        SharedPreferences sharedPreferences = getPreferenceManager();
        sharedPreferences.edit().putStringSet(MaSJIDLOCATIONS, value).apply();
    }

    public static Set<String> getHashSet() {
        SharedPreferences sharedPreferences = getPreferenceManager();
        Set<String> strings = new HashSet<>();
        return sharedPreferences.getStringSet(MaSJIDLOCATIONS, strings);
    }
}
