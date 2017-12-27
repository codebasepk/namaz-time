
package com.byteshaft.namaztime.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.byteshaft.namaztime.AppGlobals;
import com.byteshaft.namaztime.R;
import com.byteshaft.namaztime.serializers.MasjidDetails;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Maps extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private int locationCounter = 0;
    private Set<String> latLngSet;
    private boolean mapTouch = false;
    private ArrayList<MasjidDetails> alreadyExisting;
    private DatabaseReference ref;
    private View mBaseView;
    private MapView mMapView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mBaseView = inflater.inflate(R.layout.activity_maps, container, false);
        alreadyExisting = new ArrayList<>();
        latLngSet = AppGlobals.getHashSet();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mMapView = (MapView) mBaseView.findViewById(R.id.map);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(this);
        buildGoogleApiClient();
        Snackbar.make(mBaseView, "if possible mark mosque when you are inside it...",
                Snackbar.LENGTH_SHORT).show();
        return mBaseView;
    }


    public void buildGoogleApiClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            mGoogleApiClient.connect();
        }
    }

    public void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        createLocationRequest();
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    private void stopLocationUpdate() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }
    }


    protected void createLocationRequest() {
        long INTERVAL = 0;
        long FASTEST_INTERVAL = 0;
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i("TAG", "Map onPuase");
        stopLocationUpdate();
    }

    @Override
    public void onLocationChanged(Location location) {
        locationCounter++;
        if (locationCounter > 1 && locationCounter < 3) {
            Log.i("TAG", "onLocationChanged called");
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng).zoom(18).build();
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
            Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = null;
            try {
                addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude,
                        1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (addresses != null && addresses.size() > 0) {
                Log.i("TAG", addresses.toString());
                String locality = addresses.get(0).getLocality();
                String countryName = addresses.get(0).getCountryName();
                AppGlobals.savePersonCity(locality);
                AppGlobals.savePersonCountry(countryName);
                getActivityRequests(countryName, locality);
                AppGlobals.anyLocationSaved(true);
            }
        }
    }

    private void getActivityRequests(String countryName, String cityName) {
        ref = FirebaseDatabase.getInstance().
                getReference()
                .child("Database").child("locations").child(countryName).child(cityName);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Log.i("TAG", "request" + ds.getKey());
                    Log.i("TAG", "value " + ds.getValue(MasjidDetails.class).getCity());
                    showExistedMosquesToUser(ds.getValue(MasjidDetails.class));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("TAG", databaseError.getMessage());
            }
        });
    }

    private void showExistedMosquesToUser(MasjidDetails masjidDetails) {
        int strokeColor = Color.parseColor("#4890c8");
        int shadeColor = Color.parseColor("#334890c8");
        LatLng latLng = new LatLng(masjidDetails.getLat(),
                masjidDetails.getLng());
        alreadyExisting.add(masjidDetails);
        BitmapDescriptor bitmap;
        bitmap = BitmapDescriptorFactory.fromResource(R.drawable.mosque);
        mMap.addMarker(new MarkerOptions()
                .position(latLng).title(String.valueOf(masjidDetails.getMasjidName()))
                .icon(bitmap));
        CircleOptions circleOptions = new CircleOptions().center(latLng).radius(30)
                .fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(8);
        Circle mCircle = mMap.addCircle(circleOptions);
        mCircle.setCenter(latLng);


    }

    @Override
    public void onMapLongClick(final LatLng latLng) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Add Mosque");
        String text = "<b>Note:</b> Mobile will go in Silent Mode when you are in the mosque and normal when exit the mosque";
        alertDialogBuilder.setMessage("Do you want to add this mosque ? \n \n " +
                Html.fromHtml(text)).setCancelable(false);
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        input.setHint("please enter mosque name");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(5, 5, 5, 5);
        input.setLayoutParams(lp);
        alertDialogBuilder.setView(input);
        alertDialogBuilder.setPositiveButton("Add", null);
        alertDialogBuilder.setNegativeButton("No", null);

        final AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        Log.i("TAG", "input " + String.valueOf(input.getText().toString() == null));
                        Log.i("TAG", "input " + String.valueOf(input.getText().toString().isEmpty()));

                        if (input.getText().toString() != null &&
                                !input.getText().toString().isEmpty()) {
                            boolean stopHere = false;
                            for (MasjidDetails masjidDetails: alreadyExisting) {
                                Log.i("TAG", "distance " + distance(masjidDetails.getLat(), masjidDetails.getLng(),
                                        latLng.latitude, latLng.longitude));
                                if (masjidDetails.getMasjidName().equalsIgnoreCase(input.getText()
                                        .toString()) || distance(masjidDetails.getLat(), masjidDetails.getLng(),
                                        latLng.latitude, latLng.longitude) < 0.310686) {
                                    stopHere = true;
                                }
                            }
                            if (stopHere) {
                                alertDialog.dismiss();
                                Snackbar.make(getView(),
                                        "This mosque already added",
                                        Snackbar.LENGTH_SHORT).show();

                            } else {
                                Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                                List<Address> addresses = null;
                                try {
                                    addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude,
                                            1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                if (addresses != null && addresses.size() > 0) {
                                    Log.i("TAG", addresses.toString());
                                    String locality = addresses.get(0).getLocality();
                                    String countryName = addresses.get(0).getCountryName();
                                    Log.i("TAG", "locality " + locality);
                                    Log.i("TAG", "locality " + addresses.get(0).getCountryName());
                                    MasjidDetails masjidDetails = new MasjidDetails();
                                    masjidDetails.setCity(locality);
                                    masjidDetails.setCountry(countryName);
                                    masjidDetails.setLat(latLng.latitude);
                                    masjidDetails.setLng(latLng.longitude);
                                    masjidDetails.setMasjidName(input.getText().toString());
                                    saveMajidLocation(countryName, locality, masjidDetails);
                                }
                                AppGlobals.saveHashSet(latLngSet);
                                alertDialog.dismiss();
                                Snackbar.make(getView(),
                                        "Successfully marked Mosque",
                                        Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            input.setError("empty");
                        }

                    }
                });

                Button negButton = ((AlertDialog) alertDialog).getButton(AlertDialog.BUTTON_NEGATIVE);
                negButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        // TODO Do something
                        alertDialog.dismiss();

                    }
                });
            }
        });
        alertDialog.show();
    }

    private double distance(double lat1, double lng1, double lat2, double lng2) {

        double earthRadius = 3958.75; // in miles, change to 6371 for kilometer output

        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);

        double sindLat = Math.sin(dLat / 2);
        double sindLng = Math.sin(dLng / 2);

        double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
                * Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2));

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        double dist = earthRadius * c;

        return dist; // output distance, in MILES
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Delete Mosque Location");
        String text = "<b>Note:</b> Mobile will not go in Silent Mode when you are in the mosque and normal when exit the mosque";
        alertDialogBuilder.setMessage("Do you want to Remove this mosque ? \n \n" +
                Html.fromHtml(text)).setCancelable(false).setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                for (String location : latLngSet) {
                    String[] locations = location.split(",");
                    if (String.valueOf(marker.getPosition().latitude).equals(locations[0])) {
                        latLngSet.remove(marker.getPosition().latitude + "," + marker.getPosition().longitude);
                        AppGlobals.saveHashSet(latLngSet);
                        marker.remove();
                    }
                }

            }
        });
        alertDialogBuilder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
        return false;
    }

    private void saveMajidLocation(String country, String city, MasjidDetails masjidDetails) {
        ref = FirebaseDatabase.getInstance().
                getReference();
        ref.child("Database").child("locations").child(country).child(city).push()
                .setValue(masjidDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                }
            }

        });
    }
}
