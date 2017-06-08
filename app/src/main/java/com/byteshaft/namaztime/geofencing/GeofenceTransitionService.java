package com.byteshaft.namaztime.geofencing;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.byteshaft.namaztime.MainActivity;
import com.byteshaft.namaztime.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionService extends Service {

    private static final String TAG = "GeofenceTransitions";
    private static GeofenceTransitionService geofenceTransitionsIntentService;

    public GeofenceTransitionService() {
        super();
    }

    public static GeofenceTransitionService getInstance() {
        return geofenceTransitionsIntentService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onHandleIntent");
        geofenceTransitionsIntentService = this;
        Log.i("TAG", intent.getStringExtra("id"));
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Goefencing Error " + geofencingEvent.getErrorCode());
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        Log.i(TAG, "geofenceTransition = " + geofenceTransition + " Enter : " + Geofence.GEOFENCE_TRANSITION_ENTER + "Exit : " + Geofence.GEOFENCE_TRANSITION_EXIT);
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL){
            Log.i(TAG, "ENTER Showing Notification...");

        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, " Exited Showing Notification...");
        } else {
            // Log the error.
//            showNotification("Error", "Error");
            Log.e(TAG, "Error ");
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("Mobile silenter")
                .setContentTitle("Namaz time")
                .setContentText("Silent your mobile inside mosque")
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);


//        notificationManager.notify(1000, notificationBuilder.build());
        startForeground(101, notificationBuilder.build());
        return START_STICKY;
    }

}
