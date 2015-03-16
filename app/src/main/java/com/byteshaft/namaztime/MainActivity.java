package com.byteshaft.namaztime;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.io.File;
import java.util.ArrayList;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    public final static String sFileName = "NAMAZ_TIMES";
    private static MainActivity sActivityInstance = null;
    private File mFile = null;
    private Helpers mHelpers = null;

    public static MainActivity getInstance() {
        return sActivityInstance;
    }

    private void setActivityInstance(MainActivity mainActivity) {
        sActivityInstance = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setActivityInstance(this);
        mHelpers = new Helpers(this);
        setupCitiesSelectionSpinner();
        String location = mHelpers.getDiskLocationForFile(sFileName);
        mFile = new File(location);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (mHelpers.isNetworkAvailable()) {
            if (mHelpers.getPreviouslySelectedCityIndex() != position) {
                String city = parent.getItemAtPosition(position).toString().toLowerCase();
                mHelpers.saveSelectedCity(city, position);
                new NamazTimesDownloadTask(MainActivity.this).execute();
            }
            if (mFile.exists()) {
                mHelpers.setTimesFromDatabase();
            } else {
                new NamazTimesDownloadTask(MainActivity.this).execute();
            }
        } else if (mFile.exists()) {
            mHelpers.setTimesFromDatabase();
        } else {
            mHelpers.showInternetNotAvailableDialog();
        }
        startService(new Intent(this, NamazTimeService.class));
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Intentionally left blank.
    }

    private void setupCitiesSelectionSpinner() {
        Spinner spinner = (Spinner) findViewById(R.id.FirstSpinner);
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
        spinner.setAdapter(adapter);
        spinner.setSelection(previouslySelectedCityIndex);
        spinner.setOnItemSelectedListener(this);
    }
}
