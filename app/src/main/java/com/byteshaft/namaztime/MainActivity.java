package com.byteshaft.namaztime;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    public final static String sFileName = "NAMAZ_TIMES";
    public static TextView textView, text, textTime;
    private Spinner mSpinner;
    private static MainActivity activityInstance = null;
    private File file;
    private Helpers mHelpers = null;

    public static MainActivity getInstance() {
        return activityInstance;
    }

    private void setActivityInstance(MainActivity mainActivity) {
        activityInstance = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActivityInstance(this);
        initializeXmlReferences();
        mHelpers = new Helpers(this);
        SharedPreferences preferences = mHelpers.getPreferenceManager();
        preferences.registerOnSharedPreferenceChangeListener(this);
        setupCitiesSelectionSpinner();
        String location = mHelpers.getDiskLocationForFile(sFileName);
        file = new File(location);
        if (!file.exists()) {
            if (mHelpers.isNetworkAvailable()) {
                new NamazTimesDownloadTask(this).execute();
            } else {
                mHelpers.showInternetNotAvailableDialog();
            }
        } else {
            mHelpers.setTimesFromDatabase();
            startService(new Intent(this, NamazTimeService.class));
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mHelpers.isNetworkAvailable()) {
            String city = parent.getItemAtPosition(position).toString().toLowerCase();
            mHelpers.saveSelectedCityName(city);
            mHelpers.setPreferenceForCityByIndex(position);
        } else if (file.exists() && Helpers.sDATE == null) {
            Toast.makeText(getApplicationContext(),
                    "Connect to internet", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (!mHelpers.isNetworkAvailable()) {
            mHelpers.showInternetNotAvailableDialog();
        } else {
            new NamazTimesDownloadTask(MainActivity.this).execute();
        }
    }

    private void setupCitiesSelectionSpinner() {
        int previouslySelectedCityIndex = mHelpers.getPreviouslySelectedCityIndex();
        ArrayList<String> citiesList = new ArrayList<>();
        citiesList.add("Karachi");
        citiesList.add("Lahore");
        citiesList.add("Multan");
        citiesList.add("Islamabad");
        citiesList.add("Peshawar");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, citiesList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
        mSpinner.setSelection(previouslySelectedCityIndex);
        mSpinner.setOnItemSelectedListener(this);
    }

    private void initializeXmlReferences() {
        mSpinner = (Spinner) findViewById(R.id.FirstSpinner);
        textView = (TextView) findViewById(R.id.textView);
        text = (TextView) findViewById(R.id.text);
        textTime = (TextView) findViewById(R.id.textTime);
    }
}
