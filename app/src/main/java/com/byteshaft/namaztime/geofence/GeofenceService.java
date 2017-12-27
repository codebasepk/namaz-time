package com.byteshaft.namaztime.geofence;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.byteshaft.namaztime.AppGlobals;
import com.byteshaft.namaztime.MainActivity;
import com.byteshaft.namaztime.R;
import com.byteshaft.namaztime.serializers.MasjidDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class GeofenceService extends Service implements
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, ResultCallback<Status>,
        LocationListener {

    protected ArrayList<Geofence> mGeofenceList;
    GoogleApiClient mGoogleApiClient;
    private PendingIntent mGeofencePendingIntent;
    private LocationRequest mLocationRequest;
    private DatabaseReference ref;
    private boolean isServiceAlreadyRunning = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mGeofenceList = new ArrayList<>();
        mGeofencePendingIntent = null;
        buildGoogleApiClient();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getActivityRequests(AppGlobals.getPersonCountry(), AppGlobals.getPersonCity());
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Thank you for respecting other's and their prayer")
                .setContentIntent(pendingIntent).build();
        startForeground(100221, notification);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, "GoogleApiClient Not Connected", Toast.LENGTH_SHORT).show();
        } else {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        }
        mGoogleApiClient.disconnect();
        ref.removeEventListener(valueEventListener);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i("API", "Connected");
        long INTERVAL = 0;
        long FASTEST_INTERVAL = 0;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        if (mGeofenceList.size() > 0) {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }
        isServiceAlreadyRunning = true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onResult(Status status) {
        Log.i("GeoFence", "onResult status" + status);
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }


    private void getActivityRequests(String countryName, String cityName) {
        ref = FirebaseDatabase.getInstance().
                getReference()
                .child("Database").child("locations").child(countryName).child(cityName);
        ref.addValueEventListener(valueEventListener);
    }

    ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot snapshot) {
            for (DataSnapshot ds : snapshot.getChildren()) {
                if (isServiceAlreadyRunning) {
                    onDestroy();
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            isServiceAlreadyRunning = false;
                            startService(new Intent(getApplicationContext(), GeofenceService.class));
                        }
                    }, 1000);
                    return;
                }
                Log.i("TAG SERVICE", "request" + ds.getKey());
                Log.i("TAG SERVICE", "value " + ds.getValue(MasjidDetails.class).getCity());
                MasjidDetails masjidDetails = ds.getValue(MasjidDetails.class);
                mGeofenceList.add(new Geofence.Builder()
                        .setRequestId(masjidDetails.getMasjidName())
                        .setCircularRegion(
                                masjidDetails.getLat(),
                                masjidDetails.getLng(), 30

                        )
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER
                                | Geofence.GEOFENCE_TRANSITION_EXIT)
                        .build());
            }
            mGoogleApiClient.connect();
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("TAG", databaseError.getMessage());

        }
    };

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onLocationChanged(Location location) {
    }
}