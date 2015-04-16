package com.byteshaft.namaztime;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.File;


public class ChangeCity extends ActionBarActivity implements ListView.OnItemClickListener {
    static boolean downloadRun = false;
    RelativeLayout linearLayout;
    Helpers mHelpers;
    AlarmHelpers alarmHelpers;
    File file;
    ChangeCityHelpers mChangeCityHelpers;
    static ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changecitylayout);
        mProgressBar = (ProgressBar) findViewById(R.id.mprogressBar);
        mProgressBar.setVisibility(View.INVISIBLE);
        mHelpers = new Helpers(this);
        alarmHelpers = new AlarmHelpers(this);
        mChangeCityHelpers = new ChangeCityHelpers(this);
        int mPreviousCity = mHelpers.getPreviouslySelectedCityIndex();
        linearLayout = (RelativeLayout) findViewById(R.id.linearLayout);
        ListView list = getListView(mPreviousCity);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        AlarmHelpers.removePreviousAlarams();
        Log.i("NAMAZ_TIME" , String.valueOf(AlarmHelpers.pendingIntent == null));
        Log.i("NAMAZ_TIME" , String.valueOf(AlarmHelpers.pIntent == null));
        Notifications notifications = new Notifications(this);
        notifications.removeNotification();
        String city = parent.getItemAtPosition(position).toString();
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + city;
        file = new File(location);
        MainActivity.sFileName = city;
        if (file.exists()) {
            mChangeCityHelpers.fileExists(parent, position);
        } else {
            mChangeCityHelpers.fileNotExists(parent, position);
            downloadRun = true;
        }

    }
    ListView getListView(int mPreviousCity) {
        ListView list = new ListView(this);
        String[] cityList = new String[]{"Karachi", "Lahore", "Multan"
                , "Islamabad", "Peshawar", "Azad Kashmir", "Faisalabad", "Bahawalpur", "Rawalpindi", "Hyderabad", "Quetta"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cityList);
        list.setAdapter(modeAdapter);
        list.setItemChecked(mPreviousCity, true);
        linearLayout.addView(list);
        return list;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mProgressBar.isShown()) {
            this.finish();
        }
    }

}
