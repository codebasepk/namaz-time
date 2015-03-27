package com.byteshaft.namaztime;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;


public class ChangeCity extends ActionBarActivity {
    public final static String sFileName = "NAMAZ_TIMES";
    LinearLayout linearLayout;
    Helpers mHelpers;
    public static boolean downloadRun = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mHelpers = new Helpers(this);
        int mPreviousCity = mHelpers.getPreviouslySelectedCityIndex();
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        mHelpers = new Helpers(this);
        String location = mHelpers.getDiskLocationForFile(sFileName);
        File mFile = new File(location);
        ListView modeList = new ListView(this);
        String[] stringArray = new String[]{"Karachi", "Lahore", "Multan"
                , "Islamabad", "Peshawar", "Azad-Kashmir", "Faisalabad", "Bahawalpur", "Rawalpindi", "Hyderabad", "Quetta"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stringArray);
        modeList.setAdapter(modeAdapter);
        modeList.setItemChecked(mPreviousCity, true);
        linearLayout.addView(modeList);
        modeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                new NamazTimesDownloadTask(ChangeCity.this).execute();
                parent.getItemAtPosition(position);
                parent.setSelection(position);
                String city = parent.getItemAtPosition(position).toString().toLowerCase();
                mHelpers.saveSelectedCity(city, position);
                downloadRun = true;

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }
}
