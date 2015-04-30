/*
 *
 *  * (C) Copyright 2015 byteShaft Inc.
 *  *
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser General Public License
 *  * (LGPL) version 2.1 which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl-2.1.html
 *  
 */

package com.byteshaft.namaztime;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;


public class ChangeCity extends ActionBarActivity implements ListView.OnItemClickListener {

    RelativeLayout mRelativeLayout;
    Helpers mHelpers;
    AlarmHelpers mAlarmHelpers;
    File mFile;
    ChangeCityHelpers mChangeCityHelpers;
    static ProgressBar sProgressBar;
    Notifications notifications;
    static boolean sCityChanged = false;
    boolean sActivityPaused = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changecitylayout);
        sProgressBar = (ProgressBar) findViewById(R.id.mprogressBar);
        sProgressBar.setVisibility(View.INVISIBLE);
        mHelpers = new Helpers(this);
        mAlarmHelpers = new AlarmHelpers(this);
        mChangeCityHelpers = new ChangeCityHelpers(this);
        notifications = new Notifications(this);
        int mPreviousCity = mHelpers.getPreviouslySelectedCityIndex();
        mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        ListView list = getListView(mPreviousCity);
        list.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mAlarmHelpers.removePreviousAlarams();
        sCityChanged = true;
        String city = parent.getItemAtPosition(position).toString();
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + city;
        mFile = new File(location);
//        MainActivity.sFileName = city;
        if (mFile.exists()) {
            mChangeCityHelpers.fileExists(parent, position);
        } else {
            if (mHelpers.isNetworkAvailable()) {
                mChangeCityHelpers.fileNotExists(parent, position);
            } else {
                Toast.makeText(this, "Network isn't available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    ListView getListView(int mPreviousCity) {
        ListView list = new ListView(this);
        String[] cityList = new String[]{"Karachi", "Lahore", "Multan"
                , "Islamabad", "Peshawar", "azadkashmir", "Faisalabad", "Bahawalpur", "Rawalpindi", "Hyderabad", "Quetta"};
        ArrayAdapter<String> modeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cityList);
        list.setAdapter(modeAdapter);
        list.setItemChecked(mPreviousCity, true);
        mRelativeLayout.addView(list);
        return list;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        Intent intent = new Intent(this , MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sActivityPaused = true;
        if (sProgressBar.isShown()) {
            this.finish();
        }
        finish();
    }

}
