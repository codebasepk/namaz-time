package com.byteshaft.namaztime.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.byteshaft.namaztime.AppGlobals;
import com.byteshaft.namaztime.Helpers;
import com.byteshaft.namaztime.NamazTimesDownloadTask;
import com.byteshaft.namaztime.NotificationReceiver;
import com.byteshaft.namaztime.Notifications;
import com.byteshaft.namaztime.R;

import java.io.File;
import java.util.Set;

/**
 * Created by s9iper1 on 12/24/17.
 */

public class Home extends Fragment {

    private View mBaseView;
    public String sFileName = null;
    public static ProgressBar sProgressBar;
    private Notifications notifications;
    private Helpers mHelpers = null;
    private File mFile;
    private static Home instance;

    public static Home getInstance() {
        return instance;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.home, container, false);
        instance = this;
        FloatingActionButton fab =  mBaseView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadFragment(new Maps());
            }
        });
        sProgressBar = mBaseView.findViewById(R.id.progressBar);
        sProgressBar.setVisibility(View.INVISIBLE);
        Set<String> hashSet = AppGlobals.getHashSet();
        if (hashSet.size() < 1) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("New Feature");
            dialog.setMessage("You can now add your mosque to the app to silent your mobile when you are inside mosque.");
            dialog.setPositiveButton("Add now", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    startActivity(new Intent(getActivity(), Maps.class));
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
        mHelpers = new Helpers(getContext());
        notifications = new Notifications(getContext());
        return mBaseView;
    }

    public void loadFragment(Fragment fragment) {
        String backStateName = fragment.getClass().getSimpleName();
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);
        fragmentTransaction.replace(R.id.container, fragment, backStateName);
        fragmentTransaction.commit();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sFileName = mHelpers.getPreviouslySelectedCityName();
        String location = getActivity().getFilesDir().getAbsoluteFile().getAbsolutePath() + "/" + sFileName;
        mFile = new File(location);
        if (!mFile.exists() && mHelpers.isNetworkAvailable()) {
            sProgressBar.setVisibility(View.VISIBLE);
            NamazTimesDownloadTask namazTimesDownloadTask = new NamazTimesDownloadTask(getContext());
            namazTimesDownloadTask.downloadNamazTime();
        } else if (!mHelpers.isNetworkAvailable() && !mFile.exists()) {
            mHelpers.showInternetNotAvailableDialog();
        } else if (mFile.exists()) {
            mHelpers.setTimesFromDatabase(true, mHelpers.getPreviouslySelectedCityName());
            if (!ChangeCity.sCityChanged && !NotificationReceiver.sNotificationDisplayed) {
                Intent alarmIntent = new Intent("com.byteshaft.setalarm");
                getActivity().sendBroadcast(alarmIntent);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        changeCityInDisplay();
        if (ChangeCity.sCityChanged) {
            Log.i("NAMAZ_TIME" ,  "City Changed");
            notifications.removeNotification();
            Intent alarmIntent = new Intent("com.byteshaft.setalarm");
            getActivity().sendBroadcast(alarmIntent);
            ChangeCity.sCityChanged = false;
        }

//        if (GeofenceTransitionService.getInstance() != null) {
//            if (Helpers.isMyServiceRunning(GeofenceTransitionService.getInstance().getClass())) {
//                GeofenceTransitionService.getInstance().stopSelf();
//            }
//        }
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this,
//                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                    MY_PERMISSIONS_REQUEST_LOCATION);
//
//        } else {
//            if (Helpers.locationEnabled()) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
//                        && !notificationManager.isNotificationPolicyAccessGranted()) {
//                    Intent intent = new Intent(
//                            android.provider.Settings
//                                    .ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
//                    startActivity(intent);
//                } else {
//                    new android.os.Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            Set<String> latLngSet = AppGlobals.getHashSet();
//                            if (latLngSet.size() > 0) {
//                                SimpleGeofence simpleGeofence = new SimpleGeofence();
//                                int counter = 0;
//                                for (String location : latLngSet) {
//                                    Log.i("TAG", "adding fence" + counter);
//                                    String[] locations = location.split(",");
//                                    LatLng latLng = new LatLng(Double.parseDouble(locations[0]),
//                                            Double.parseDouble(locations[1]));
//                                    Log.i("TAG", "Fence :Lat " + latLng.latitude + " Lng "+ latLng.longitude);
//                                    simpleGeofence.createGeofences(String.valueOf(counter), latLng.latitude, latLng.longitude);
//                                    counter++;
//                                }
//                            }
//                        }
//                    }, 2000);
//                }
//            }
//        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sProgressBar.isShown()) {
            sProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void changeCityInDisplay() {
        if (mFile.exists() && !mHelpers.retrieveTimeForNamazAndTime("date").equals(mHelpers.getDate())) {
            mHelpers.setTimesFromDatabase(true, sFileName);
        }
    }
}
