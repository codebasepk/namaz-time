package com.byteshaft.namaztime;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    final static String sFileName = "namaztimes.txt";
    public static int CITY_NAME;
    static TextView sTextView;
    static String sDATE;
    private final String SELECTED_CITY = "city";
    private Spinner mSpinner;
    private SharedPreferences setting;
    private String FILE_NAME = "cities";
    private SharedPreferences.OnSharedPreferenceChangeListener listen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializationOfXmlReferences();
        Helpers helpers = new Helpers(this);
        mSpinner.setOnItemSelectedListener(this);
        citiesSpinner();
        refreshOnChangeSharedPrefrence(this);
        setting.registerOnSharedPreferenceChangeListener(listen);


        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + sFileName;
        File file = new File(location);
        if (!file.exists()) {
            if (helpers.checkNetworkStatus() != null) {
                new SystemManagement(this).execute();
            } else {
                helpers.refreshDialoge(this);
            }
        } else {
            try {
                helpers.setTimesFromDatabase();
            } catch (InterruptedException | IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        startService(new Intent(getBaseContext(), NamazTimeService.class));
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
        setSharedPrefrenceForCities(mSpinner.getSelectedItemPosition());
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
                new SystemManagement(context).execute();
            }
        };

    }

    private void initializationOfXmlReferences() {
        sTextView = (TextView) findViewById(R.id.textView);
        mSpinner = (Spinner) findViewById(R.id.FirstSpinner);

    }
}
