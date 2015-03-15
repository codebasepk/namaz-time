package com.byteshaft.namaztime;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    final static String sFileName = "namaztimes.txt";
    public static int CITY_NAME;
    public static TextView textView, text, textTime;
    public static Spinner mSpinner;
    static LinearLayout layout;
    static LinearLayout linearLayout;
    static MainActivity instance = null;
    private final String SELECTED_CITY = "city";
    private SharedPreferences setting;
    private String FILE_NAME = "cities";
    private File file;
    private SharedPreferences.OnSharedPreferenceChangeListener listen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        instance = this;
        initializationOfXmlReferences();
        Helpers helpers = new Helpers(this);
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + sFileName;
        file = new File(location);
        if (!file.exists()) {
            if (Helpers.checkNetworkStatus() != null) {
                new NamazTimesDownloadTask(this).execute();
            } else {
                Helpers.showInternetNotAvailableDialog(this);
            }
        } else {
            helpers.setTimesFromDatabase();
            startService(new Intent(this, NamazTimeService.class));
        }
    }

    private void citiesSpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Karachi");
        categories.add("Lahore");
        categories.add("Multan");
        categories.add("Islamabad");
        categories.add("Peshawar");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        setting = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        int previousPosition = setting.getInt(SELECTED_CITY, 0);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(previousPosition);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (Helpers.checkNetworkStatus() != null) {
            setSharedPrefrenceForCities(mSpinner.getSelectedItemPosition());
            setSharedPrefrenceForCities(mSpinner.getSelectedItemPosition());
        } else if (Helpers.checkNetworkStatus() == null && file.exists() && Helpers.sDATE == null) {
            Toast.makeText(this, "Connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void setSharedPrefrenceForCities(int value) {
        setting = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor;
        editor = setting.edit();
        editor.putInt(SELECTED_CITY, value);
        editor.apply();
        CITY_NAME = setting.getInt(SELECTED_CITY, 0);
    }

    private void refreshOnChangeSharedPrefrence(final Activity context) {
        listen = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if (Helpers.checkNetworkStatus() == null) {
                    Helpers.showInternetNotAvailableDialog(context);
                } else {
                    new NamazTimesDownloadTask(context).execute();
                }
            }
        };
    }

    private void initializationOfXmlReferences() {
        mSpinner = (Spinner) findViewById(R.id.FirstSpinner);
        layout = (LinearLayout) findViewById(R.id.layout);
        mSpinner.setOnItemSelectedListener(this);
        citiesSpinner();
        refreshOnChangeSharedPrefrence(this);
        setting.registerOnSharedPreferenceChangeListener(listen);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        linearLayout.setBackgroundResource(R.drawable.back);
        textView = (TextView) findViewById(R.id.textView);
        text = (TextView) findViewById(R.id.text);
        textTime = (TextView) findViewById(R.id.textTime);
    }
}
