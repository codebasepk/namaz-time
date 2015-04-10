package com.byteshaft.namaztime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.io.File;


public class ChangeCity extends ActionBarActivity implements ListView.OnItemClickListener {
    static boolean downloadRun = false;
    LinearLayout linearLayout;
    Helpers mHelpers;
    AlarmHelpers alarmHelpers;
    File file;
    ChangeCityHelpers mChangeCityHelpers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        mHelpers = new Helpers(this);
        alarmHelpers = new AlarmHelpers(this);
        mChangeCityHelpers = new ChangeCityHelpers(this);
        int mPreviousCity = mHelpers.getPreviouslySelectedCityIndex();
        linearLayout = (LinearLayout) findViewById(R.id.linearLayout);
        ListView list = getListView(mPreviousCity);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        alarmHelpers.removePreviousAlarams();
        Notifications notifications = new Notifications(this);
        notifications.removeNotification();
        String city = parent.getItemAtPosition(position).toString();
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + city;
        file = new File(location);
        MainActivity.sFileName = city;
        if (file.exists()) {
            mChangeCityHelpers.fileExsist(parent, position);
        } else {
            mChangeCityHelpers.fileNotExsist(parent, position);
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
        this.finish();
        if (NamazTimesDownloadTask.dialogShowing) {
            NamazTimesDownloadTask.mProgressDialog.dismiss();
        }
    }

}
