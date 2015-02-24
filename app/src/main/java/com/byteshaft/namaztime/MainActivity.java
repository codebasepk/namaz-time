package com.byteshaft.namaztime;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends Activity implements AdapterView.OnItemSelectedListener {

    final static String sFileName = "namaztimes.txt";
    static TextView sTextView;
    static String sDATE;
    public static int CITY_NAME;
    Spinner spinner;
    SharedPreferences setting;
    final String SELECTED_CITY = "city";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sTextView = (TextView) findViewById(R.id.textView);
        Helpers helpers = new Helpers(this);
        spinner = (Spinner) findViewById(R.id.FirstSpinner);
        spinner.setOnItemSelectedListener(this);
        citiesSpinner();

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
    }

    private void citiesSpinner() {
        ArrayList<String> categories = new ArrayList<>();
        categories.add("Karachi");
        categories.add("Lahore");
        categories.add("Multan");
        categories.add("Islamabad");
        categories.add("Peshawar");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
//        int previousPosition = setting.getInt(SELECTED_CITY , 0);
//        System.out.println(previousPosition);
//        spinner.setSelection(previousPosition);
        }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        setSharedPrefrenceForCities(spinner.getSelectedItemPosition());

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        spinner.setSelection(setting.getInt(SELECTED_CITY, 0));

    }

private void setSharedPrefrenceForCities(int value){
    String FILE_NAME = "cities";
    setting = getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    SharedPreferences.Editor  editor;
    editor = setting.edit();
    editor.putInt(SELECTED_CITY, value);
    editor.apply();
    CITY_NAME = setting.getInt(SELECTED_CITY, 0);
    System.out.println(CITY_NAME);
}



}
