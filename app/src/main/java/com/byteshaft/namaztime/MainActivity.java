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
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    public String sFileName = null;
    static ProgressBar sProgressBar;
    private static MainActivity sActivityInstance = null;
    private Notifications notifications;
    private Helpers mHelpers = null;
    File mFile;

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
        sProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        sProgressBar.setVisibility(View.INVISIBLE);
        setActivityInstance(this);
        mHelpers = new Helpers(this);
        notifications = new Notifications(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        sFileName = mHelpers.getPreviouslySelectedCityName();
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + sFileName;
        mFile = new File(location);
        if (!mFile.exists() && mHelpers.isNetworkAvailable()) {
            sProgressBar.setVisibility(View.VISIBLE);
            NamazTimesDownloadTask namazTimesDownloadTask = new NamazTimesDownloadTask(this);
            namazTimesDownloadTask.downloadNamazTime();
        } else if (!mHelpers.isNetworkAvailable() && !mFile.exists()) {
            mHelpers.showInternetNotAvailableDialog();
        } else if (mFile.exists()) {
            mHelpers.setTimesFromDatabase(true, mHelpers.getPreviouslySelectedCityName());
            if (!ChangeCity.sCityChanged && !NotificationReceiver.sNotificationDisplayed) {
                Intent alarmIntent = new Intent("com.byteshaft.setalarm");
                sendBroadcast(alarmIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeCityInDisplay();
        if (ChangeCity.sCityChanged) {
            Log.i("NAMAZ_TIME" ,  "City Changed");
            notifications.removeNotification();
            Intent alarmIntent = new Intent("com.byteshaft.setalarm");
            sendBroadcast(alarmIntent);
            ChangeCity.sCityChanged = false;
        }
    }

    private void changeCityInDisplay() {
        if (mFile.exists() && !mHelpers.retrieveTimeForNamazAndTime("date").equals(mHelpers.getDate())) {
            mHelpers.setTimesFromDatabase(true, sFileName);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
        startActivity(startMain);
        MainActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                changeCity();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeCity() {
        Intent intent = new Intent(this, ChangeCity.class);
        finish();
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (sProgressBar.isShown()) {
            sProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            openOptionsMenu();
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
}
