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


import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.byteshaft.namaztime.geofencing.GeofenceTransitionService;
import com.byteshaft.namaztime.geofencing.SimpleGeofence;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.util.Set;

public class MainActivity extends ActionBarActivity {

    public String sFileName = null;
    static ProgressBar sProgressBar;
    private static MainActivity sActivityInstance = null;
    private Notifications notifications;
    private Helpers mHelpers = null;
    private File mFile;
    public static MainActivity getInstance() {
        return sActivityInstance;
    }
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private NotificationManager notificationManager;

    private void setActivityInstance(MainActivity mainActivity) {
        sActivityInstance = mainActivity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        sProgressBar.setVisibility(View.INVISIBLE);
        notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        setActivityInstance(this);
        Set<String> hashSet = AppGlobals.getHashSet();
        if (hashSet.size() < 1) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("New Feature");
            dialog.setMessage("You can now add your mosque to the app to silent your mobile when you are inside mosque.");
            dialog.setPositiveButton("Add now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    //get gps
                }
            });
            dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
        }
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
            if (!ChangeCityActivity.sCityChanged && !NotificationReceiver.sNotificationDisplayed) {
                Intent alarmIntent = new Intent("com.byteshaft.setalarm");
                sendBroadcast(alarmIntent);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        changeCityInDisplay();
        if (ChangeCityActivity.sCityChanged) {
            Log.i("NAMAZ_TIME" ,  "City Changed");
            notifications.removeNotification();
            Intent alarmIntent = new Intent("com.byteshaft.setalarm");
            sendBroadcast(alarmIntent);
            ChangeCityActivity.sCityChanged = false;
        }

        if (GeofenceTransitionService.getInstance() != null) {
            if (Helpers.isMyServiceRunning(GeofenceTransitionService.getInstance().getClass())) {
                GeofenceTransitionService.getInstance().stopSelf();
            }
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

        } else {
            if (Helpers.locationEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        && !notificationManager.isNotificationPolicyAccessGranted()) {
                    Intent intent = new Intent(
                            android.provider.Settings
                                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                    startActivity(intent);
                } else {
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Set<String> latLngSet = AppGlobals.getHashSet();
                            if (latLngSet.size() > 0) {
                                SimpleGeofence simpleGeofence = new SimpleGeofence();
                                int counter = 0;
                                for (String location : latLngSet) {
                                    Log.i("TAG", "adding fence" + counter);
                                    String[] locations = location.split(",");
                                    LatLng latLng = new LatLng(Double.parseDouble(locations[0]),
                                            Double.parseDouble(locations[1]));
                                    Log.i("TAG", "Fence :Lat " + latLng.latitude + " Lng "+ latLng.longitude);
                                    simpleGeofence.createGeofences(String.valueOf(counter), latLng.latitude, latLng.longitude);
                                    counter++;
                                }
                            }
                        }
                    }, 2000);
                }
            }
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
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_city:
                changeCity();
                return true;
            case R.id.action_add_location:
                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            MY_PERMISSIONS_REQUEST_LOCATION);

                } else {
                    if (Helpers.locationEnabled()) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                                && !notificationManager.isNotificationPolicyAccessGranted()) {
                            Intent intent = new Intent(
                                    android.provider.Settings
                                            .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                            startActivity(intent);
                        } else {
                            startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                        }
                    } else {
                        Helpers.dialogForLocationEnableManually(this);
                    }
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AppGlobals.LOCATION_ENABLE:
                if (Helpers.locationEnabled()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            && !notificationManager.isNotificationPolicyAccessGranted()) {
                        Intent intent = new Intent(
                                android.provider.Settings
                                        .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivity(intent);
                    } else {
                        startActivity(new Intent(getApplicationContext(), MapsActivity.class));
                    }
                }
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(findViewById(android.R.id.content), "permission granted",
                            Snackbar.LENGTH_SHORT).show();
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {
                    Snackbar.make(findViewById(android.R.id.content), "permission denied!",
                            Snackbar.LENGTH_SHORT).show();

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void changeCity() {
        Intent intent = new Intent(this, ChangeCityActivity.class);
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
