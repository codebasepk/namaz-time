package com.byteshaft.namaztime;


import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    public static String sFileName;
    static ProgressBar progressBar;
    private static MainActivity sActivityInstance = null;
    Notifications notifications;
    private Helpers mHelpers = null;
    File file;

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
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        setActivityInstance(this);
        mHelpers = new Helpers(this);
        notifications = new Notifications(this);
        sFileName = mHelpers.getPreviouslySelectedCityName();
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + sFileName;
        file = new File(location);
        if (!file.exists() && mHelpers.isNetworkAvailable()) {
            progressBar.setVisibility(View.VISIBLE);
            NamazTimesDownloadTask namazTimesDownloadTask = new NamazTimesDownloadTask(this);
            namazTimesDownloadTask.downloadNamazTime();
        } else if (!mHelpers.isNetworkAvailable() && !file.exists()) {
            mHelpers.showInternetNotAvailableDialog();
        } else if (file.exists()) {
            mHelpers.setTimesFromDatabase(true, sFileName);
            if (!ChangeCity.cityChanged && !NotificationReceiver.sNotificationDisplayed) {
                Intent alarmIntent = new Intent("com.byteshaft.setalarm");
                sendBroadcast(alarmIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentCity = mHelpers.getPreviouslySelectedCityName();
        if (file.exists() && !mHelpers.getPreviouslySelectedCityName().equals(sFileName)) {
            mHelpers.setTimesFromDatabase(true, currentCity);
        }
        changeCityInDisplay();
        if (ChangeCity.cityChanged) {
            System.out.println("city Changed");
            notifications.removeNotification();
            Intent alarmIntent = new Intent("com.byteshaft.setalarm");
            sendBroadcast(alarmIntent);
            ChangeCity.cityChanged = false;
        }
    }

    private void changeCityInDisplay() {
        if (file.exists() && !mHelpers.retrieveTimeForNamazAndTime("date").equals(mHelpers.getDate())) {
            mHelpers.setTimesFromDatabase(true, sFileName);
            System.out.println("called");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
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
        startActivity(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (progressBar.isShown()) {
            progressBar.setVisibility(View.INVISIBLE);
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
