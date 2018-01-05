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
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.byteshaft.namaztime.fragments.ChangeCity;
import com.byteshaft.namaztime.fragments.Home;
import com.byteshaft.namaztime.fragments.Maps;
import com.byteshaft.namaztime.geofence.GeofenceService;
import com.byteshaft.namaztime.helpers.Helpers;
import com.byteshaft.namaztime.receivers.AlarmNotification;
import com.byteshaft.namaztime.receivers.NextNamazTimeReceiver;
import com.byteshaft.namaztime.receivers.NotificationReceiver;
import com.byteshaft.namaztime.receivers.RingtoneRestoreReceiver;
import com.byteshaft.namaztime.receivers.StandardAlarmReceiver;
import com.byteshaft.namaztime.serializers.MasjidDetails;
import com.byteshaft.requests.HttpRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private NotificationManager notificationManager;
    public static boolean sPermissionNotGranted = false;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        final SwitchCompat serviceSwitch = headerView.findViewById(R.id.service_switch);
        serviceSwitch.setChecked(AppGlobals.isServiceRunning());
        if (AppGlobals.isServiceRunning() && AppGlobals.isLocationSaved() &&
            Helpers.locationEnabled() && ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(getApplicationContext(), GeofenceService.class));
            } else {
                startService(new Intent(getApplicationContext(), GeofenceService.class));
            }
        } else if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

        } else if (!Helpers.locationEnabled()) {
            Helpers.dialogForLocationEnableManually(MainActivity.this);
        }
        serviceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    if (!AppGlobals.isLocationSaved() || checkPermissionAndProceed()) {
                        if (!AppGlobals.isLocationSaved()) {
                            loadFragment(new Maps());
                        }
                        } else {
                            serviceSwitch.setChecked(false);
                        }
                    if (AppGlobals.isLocationSaved() &&
                            Helpers.locationEnabled()) {
                        if (ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED) {
                            AppGlobals.serviceState(true);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(new Intent(getApplicationContext(), GeofenceService.class));
                            } else {
                                startService(new Intent(getApplicationContext(), GeofenceService.class));
                            }                        }
                        else {
                            sPermissionNotGranted = true;
                        }
                    } else {
                        serviceSwitch.setChecked(false);
                    }
                } else {
                    AppGlobals.serviceState(false);
                    AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
                    if (AppGlobals.isModeChangedByUs()) {
                        audioManager.setRingerMode(AppGlobals.getAudioMOde());
                    }
                    NotificationManager notificationManager =
                            (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    notificationManager.cancel(10001);
                    stopService(new Intent(getApplicationContext(), GeofenceService.class));
                }
                DrawerLayout drawer = findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
            }
        });
        navigationView.setNavigationItemSelectedListener(this);
        registerAllReceiver();
        loadFragment(new Home());
    }

    public void loadFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getSimpleName();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.container, fragment, backStateName);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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
                    if (!Helpers.locationEnabled()) {
                        Helpers.dialogForLocationEnableManually(MainActivity.this);
                    }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            loadFragment(new Home());
            // Handle the camera action
        } else if (id == R.id.nav_add_masjid) {
            if (checkPermissionAndProceed()) {
                loadFragment(new Maps());
            }
        } else if (id == R.id.nav_change_city) {
            loadFragment(new ChangeCity());

        } else if (id == R.id.nav_request_addition) {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto","s9iper1@gmail.com", null));
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "New City Request");
            emailIntent.putExtra(Intent.EXTRA_TEXT, ("Add this city "));
            startActivity(Intent.createChooser(emailIntent, "Send Email"));
        } else if (id == R.id.nav_share) {
            shareApp();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    protected void shareApp() {
        String APP_LINK = "https://play.google.com/store/apps/details?id=com.bytesahft.namaztime";

        LayoutInflater li = LayoutInflater.from(MainActivity.this);
        View promptsView = li.inflate(R.layout.edit, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alertdialog builder

        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setView(promptsView);

        final EditText edtText = (EditText) promptsView.findViewById(R.id.edtName);
        String body = getString(R.string.Share_App_Body_top) + " " + getString(R.string.app_name) + " " +
                getString(R.string.Share_App_Body_middle) + " " + APP_LINK + " " +
                getString(R.string.Share_App_Body_bottom);
        edtText.setText(body);

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(getString(R.string.Send),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // get user input and set it to result
                                // edit text
                                String finalString = (edtText.getText().toString());

                                Intent email = new Intent(Intent.ACTION_SEND);
                                email.setType("text/plain");
                                email.putExtra(Intent.EXTRA_EMAIL, "");
                                email.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                email.putExtra(Intent.EXTRA_SUBJECT, " " + getString(R.string.app_name) + " " + getString(R.string.Share_App_Sub));

                                //						String body=getString(R.string.Share_App_Body_top)+" "+getString(R.string.app_name)+" "+
                                //						getString(R.string.Share_App_Body_middle)+ " "+APP_LINK+" "+
                                //						getString(R.string.Share_App_Body_bottom);

                                email.putExtra(Intent.EXTRA_TEXT, finalString);

                                try {
                                    startActivity(Intent.createChooser(email, "Send Message..."));
                                } catch (android.content.ActivityNotFoundException ex) {

                                }
                            }
                        })
                .setNegativeButton("cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private boolean checkPermissionAndProceed() {
        boolean check = true;
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            check = false;
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
                    check = false;
                } else {

                }
            } else {
                check = false;
                Helpers.dialogForLocationEnableManually(this);
            }
        }
        return check;
    }

    private void getLocation() {
        HttpRequest request = new HttpRequest(this);
        request.setOnReadyStateChangeListener(new HttpRequest.OnReadyStateChangeListener() {
            @Override
            public void onReadyStateChange(HttpRequest request, int readyState) {
                switch (readyState) {
                    case HttpRequest.STATE_DONE:
                        switch (request.getStatus()) {
                            case HttpURLConnection.HTTP_OK:
                                Log.i("TAG", "GET LOCATION " + request.getResponseText());
                                break;
                        }
                }

            }
        });
        request.setOnErrorListener(new HttpRequest.OnErrorListener() {
            @Override
            public void onError(HttpRequest request, int readyState, short error, Exception exception) {

            }
        });
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch" +
                "/json?location=" + "30.161858325327056"  + ","+"71.52063466608524"
                + "&radius=50000&types=mosque&name=Multan Pakistan" +
                "&key=AIzaSyAiQD4d4e9WxI0XXQMColWs0SaznwbSYpg";
        request.open("POST", url);
        request.send();

    }

    private void saveMajidManually() {
        MasjidDetails masjidDetails = new MasjidDetails();
        masjidDetails.setMasjidName("Mustafa Jama Masjid");
        masjidDetails.setLat(24.944314);
        masjidDetails.setLng(67.075838);
        masjidDetails.setCity("Karachi");
        masjidDetails.setCountry("Pakistan");
        ref = FirebaseDatabase.getInstance().
                getReference();
        ref.child("Database").child("locations").child("Pakistan").child("Karachi").push()
                .setValue(masjidDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                }
            }

        });
    }

    private void registerAllReceiver() {
        // Notification receiver
        IntentFilter intent = new IntentFilter("com.byteshaft.shownotification");
        NotificationReceiver notificationReceiver = new NotificationReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(notificationReceiver, intent);

        IntentFilter ringtoneRestore = new IntentFilter("com.byteshaft.silent");
        RingtoneRestoreReceiver restoreReceiver = new RingtoneRestoreReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(restoreReceiver, ringtoneRestore);

        IntentFilter alarmNotification = new IntentFilter("com.byteshaft.setalarm");
        AlarmNotification notification = new AlarmNotification();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(notification, alarmNotification);

        IntentFilter next = new IntentFilter("com.byteshaft.setnextalarm");
        NextNamazTimeReceiver nextNamazTimeReceiver = new NextNamazTimeReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(nextNamazTimeReceiver, next);

        IntentFilter standard = new IntentFilter("com.byteShaft.standardalarm");
        StandardAlarmReceiver standardAlarmReceiver = new StandardAlarmReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(standardAlarmReceiver, standard);
    }
}
