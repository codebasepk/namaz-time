package com.byteshaft.namaztime;


import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends ActionBarActivity {

    final static String sFileName = "NAMAZ_TIMES";
    private static MainActivity sActivityInstance = null;
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
        setActivityInstance(this);
        mHelpers = new Helpers(this);
        String location = getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + sFileName;
        file = new File(location);
        if (!file.exists() && mHelpers.isNetworkAvailable()) {
            new NamazTimesDownloadTask(MainActivity.this).execute();
        } else if (!mHelpers.isNetworkAvailable() && !file.exists()) {
            mHelpers.showInternetNotAvailableDialog();
        } else {
            mHelpers.setTimesFromDatabase(true);
        }
        if (Helpers.setData && !ChangeCity.downloadRun) {
            Intent alarmIntent = new Intent("com.byteshaft.Setalarm");
            sendBroadcast(alarmIntent);
        }else {
            Intent alarmIntent = new Intent("com.byteshaft.Setalarm");
            sendBroadcast(alarmIntent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mHelpers.mPresentDate.equals(mHelpers.getDate())) {
            Log.i("refreshNamazTime", "working");
            mHelpers.setTimesFromDatabase(true);
        }
        if (!file.exists()) {
            new NamazTimesDownloadTask(MainActivity.this).execute();
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
                if (mHelpers.isNetworkAvailable()) {
                    changeCity();
                    return true;
                } else {
                    Toast.makeText(this, "Network isn't available", Toast.LENGTH_SHORT).show();
                    return false;
                }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void changeCity() {
        Intent intent = new Intent(this, ChangeCity.class);
        startActivity(intent);
    }
}
