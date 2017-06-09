package com.byteshaft.namaztime;

import android.Manifest;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.byteshaft.namaztime.geofencing.SimpleGeofence;
import com.google.android.gms.maps.model.LatLng;

import java.util.Set;

/**
 * Created by s9iper1 on 11/4/16.
 */

public class BootListener extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        context.sendBroadcast(new Intent("com.byteshaft.setnextalarm"));
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            if (Helpers.locationEnabled()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                        && notificationManager.isNotificationPolicyAccessGranted()) {
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
                                    Log.i("TAG", "Fence :Lat " + latLng.latitude + " Lng " + latLng.longitude);
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
}
