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

import com.byteshaft.namaztime.geofence.GeofenceService;
import com.google.android.gms.maps.model.LatLng;

import java.util.Set;

/**
 * Created by s9iper1 on 11/4/16.
 */

public class BootListener extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
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
                           context.startService(new Intent(context, GeofenceService.class));
                        }
                    }, 2000);
                }
            }
        }

    }
}
