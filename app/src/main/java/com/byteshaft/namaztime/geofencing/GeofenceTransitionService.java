package com.byteshaft.namaztime.geofencing;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.byteshaft.namaztime.AppGlobals;
import com.byteshaft.namaztime.MainActivity;
import com.byteshaft.namaztime.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

public class GeofenceTransitionService extends IntentService {

    private static final String TAG = "GeofenceTransitions";
    private static GeofenceTransitionService geofenceTransitionsIntentService;
    private AudioManager am;

    public GeofenceTransitionService() {
        super("GeofenceTransitions");
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
    protected void onHandleIntent(@Nullable Intent intent) {
        Log.i(TAG, "onHandleIntent");
        am = (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
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
            showNotification("Inside Mosque");
            AppGlobals.saveAudioManagerMode(am.getRingerMode());
            if (am.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                am.setRingerMode(0);
            }

        }
        else if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            Log.i(TAG, " Exited Showing Notification...");
            showNotification("Exit From Mosque");
            am.setRingerMode(AppGlobals.getAudioMOde());
        } else {
            Log.e(TAG, "Error ");
        }

    }

    private void showNotification(String message){
        NotificationManager notificationManager =
                (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setLargeIcon(bm)
                .setSmallIcon(R.drawable.mosque)
                .setTicker("Mobile silenter")
                .setContentTitle("Namaz time")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(null)
                .setContentIntent(pendingIntent);


//        notificationManager.notify(1000, notificationBuilder.build());
        notificationManager.notify(10001, notificationBuilder.build());
    }

}
