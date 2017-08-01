package com.ehab.driverbroadcast.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.ehab.driverbroadcast.BuildConfig;
import com.ehab.driverbroadcast.R;
import com.ehab.driverbroadcast.model.Driver;
import com.ehab.driverbroadcast.model.LocationMessage;
import com.ehab.driverbroadcast.model.Ticket;
import com.ehab.driverbroadcast.utils.NavigationDrawerUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class LocationBroadcastActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        LocationListener,
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener{

    public static final String TAG = LocationBroadcastActivity.class.getName();
    private static final int BROADCAST_NOTIFICATION_ID = 0;

    Toolbar mToolbar;
    private Switch mSwitcher;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleClientApi;
    private LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

    private HashMap<String, Marker> passengerMarkers;
    private List<String> mTicketsIds = new ArrayList<>();

    private PubNub mPubnub;

    //TODO: Add the appropriate api keys for the pubnub service
    public static final String SUB_KEY = BuildConfig.SUB_KEY;
    public static final String PUB_KEY = BuildConfig.PUB_KEY;

    private static final String[] LOCATION_PERMS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION
    };
    private static final int LOCATION_REQUEST = 50;


    FirebaseUser currentUser;
    String userId;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDriversReference;
    DatabaseReference ticketsReference;
    ChildEventListener childEventListener;
    String username;
    String email;
    String lineChannel;
    String busNumber;

    int c = 0;

    //Drawer drawer;
    //AccountHeader headerResult;

    NavigationDrawerUtil drawerUtil = new NavigationDrawerUtil();
    NotificationManager notificationManger;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_broadcast);
        passengerMarkers = new HashMap<>();
        notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        ////////////////////////////////////////////////////////////////
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            if (user.isEmailVerified()) {
                mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
                if (getSupportActionBar() != null) {
                    setSupportActionBar(mToolbar);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                }
                this.buildGoogleApiClient();

                lineChannel = "awesome-channel";
                PNConfiguration pnConfiguration = new PNConfiguration();
                pnConfiguration.setSubscribeKey(SUB_KEY);
                pnConfiguration.setPublishKey(PUB_KEY);

                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.map);
                mapFragment.getMapAsync(this);

                mPubnub = new PubNub(pnConfiguration);

                Button finishBtn = (Button) findViewById(R.id.finishBtn);
                finishBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        finish();
                    }
                });

                final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                mSwitcher = (Switch) findViewById(R.id.switcher);
                mSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                        if (isChecked)
                            if (manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                                mGoogleClientApi.connect();
                                setupOngoingNotification();
                            } else {
                                Toast.makeText(LocationBroadcastActivity.this, "Enabling GPS is MANDATORY", Toast.LENGTH_SHORT).show();
                                mSwitcher.setChecked(false);
                                mSwitcher.setChecked(false);
                                askForGPS2();
                            }

                        else {
                            notificationManger.cancel(BROADCAST_NOTIFICATION_ID);
                            JSONObject stopMessage = new JSONObject();
                            try {
                                stopMessage.put("closeSignal", 1.0);
                                stopMessage.put("bnum", Double.parseDouble(busNumber));

                            } catch (JSONException e) {
                                //Log.e(TAG, e.toString());
                            }
                            //Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
                            mPubnub.publish()
                                    .message(stopMessage)
                                    .channel(lineChannel)
                                    .async(new PNCallback<PNPublishResult>() {
                                        @Override
                                        public void onResponse(PNPublishResult result, PNStatus status) {
                                            if (status.isError()) {
                                            }
                                        }
                                    });
                            mGoogleClientApi.disconnect();
                        }
                    }
                });
                //retrieve user data from firebase
                //currentUser = FirebaseAuth.getInstance().getCurrentUser();
                userId = user.getUid();
                mFirebaseDatabase = FirebaseDatabase.getInstance();
                mDriversReference = mFirebaseDatabase.getReference().child("drivers");

                // addValueEventListener will always listen for changes so if the user update his profile or
                // change subscription line every thing will be updated properly in thins activity
                mDriversReference.child(userId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final Driver user = dataSnapshot.getValue(Driver.class);
                        username = user.username;
                        email = user.email;
                        lineChannel = user.line;
                        busNumber = user.busNumber;

                        drawerUtil.SetupNavigationDrawer(mToolbar, LocationBroadcastActivity.this, user);

                        ticketsReference = mFirebaseDatabase.getReference()
                                .child("reservations").child(busNumber);

                        childEventListener = new ChildEventListener() {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());

                                Ticket ticket = dataSnapshot.getValue(Ticket.class);
                                //Toast.makeText(getApplicationContext(), "add called " + ++c, Toast.LENGTH_SHORT).show();
                                if(ticket.valid == true) {
                                    Marker m = mMap.addMarker(new MarkerOptions().position(new LatLng(ticket.userLocation.getLatitude(), ticket.userLocation.getLongitude())));
                                    m.setTitle(ticket.ticketNumber);
                                    passengerMarkers.put(ticket.ticketNumber, m);
                                }
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot) {
                                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                                Ticket ticket = dataSnapshot.getValue(Ticket.class);

                                if (passengerMarkers.get(ticket.ticketNumber) != null) {
                                    passengerMarkers.get(ticket.ticketNumber).remove();
                                    passengerMarkers.remove(ticket.ticketNumber);
                                }
                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
                                Toast.makeText(getApplicationContext(), "Failed to load orders.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        };
                        ticketsReference.addChildEventListener(childEventListener);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
                //Setting up Navigation Drawer
                email = "ehabhamdy2012@gmail.com";
                //drawerUtil.SetupNavigationDrawer(mToolbar, this, username, email);
                new DrawerBuilder().withActivity(this).withToolbar(mToolbar).build();


            } else {
                // Driver signed out or No Network Connection
                openLoginActivity();
            }

        } else {
            openLoginActivity();
        }
    }

    private void setupOngoingNotification() {
        Intent intent = new Intent(this, LocationBroadcastActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, BROADCAST_NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle("Rakkebny");
        builder.setContentText("Your are online and your location is being sent now");
        builder.setSubText("Warning");
        builder.setNumber(101);
        builder.setContentIntent(pendingIntent);
        builder.setTicker("Fancy Notification");
        builder.setSmallIcon(R.drawable.logo);
        builder.setOngoing(true);
        builder.setAutoCancel(true);
        builder.setPriority(Notification.PRIORITY_HIGH);
        Notification notification = builder.build();
        notificationManger.notify(BROADCAST_NOTIFICATION_ID, notification);

    }

    private void openLoginActivity() {
        Intent intent = new Intent(this, ActivityLogin.class);
        //Removing HomeActivity from the back stack
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (drawerUtil.getDrawer() != null) {
            drawerUtil.getDrawer().setSelection(1);
            drawerUtil.getDrawer().closeDrawer();
        }

    }

    private void askForGPS2() {
        Intent gpsOptionsIntent = new Intent(
                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(gpsOptionsIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mGoogleClientApi != null && mGoogleClientApi.isConnected()) {
            notificationManger.cancel(BROADCAST_NOTIFICATION_ID);
            mGoogleClientApi.disconnect();
        }
        super.onDestroy();
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleClientApi = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LocationRequest mLocationRequest = createLocationRequest();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this,
                        LOCATION_PERMS,
                        LOCATION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        LOCATION_PERMS,
                        LOCATION_REQUEST);
            }
            return;
        }
        FusedLocationApi.requestLocationUpdates(mGoogleClientApi, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(4000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Location Update", "Latitude: " + location.getLatitude() +
                " Longitude: " + location.getLongitude());
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraPosition cp = CameraPosition.builder().target(latLng).zoom(16).build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp), 1000, null);
        LocationMessage info = new LocationMessage(lineChannel, location);
        new PublishLocationTask().execute(info);
        //broadcastLocation(location, lineChannel);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == LOCATION_REQUEST) {
                askForGPS();
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String ticketId = marker.getTitle();
        if (passengerMarkers.get(ticketId) != null) {
            passengerMarkers.get(ticketId).remove();
            passengerMarkers.remove(ticketId);
            FirebaseDatabase.getInstance().getReference().child("reservations").child(busNumber).child(ticketId).child("valid").setValue(false);
        }
        return false;
    }


    class PublishLocationTask extends AsyncTask<LocationMessage, Void, Void> {
        @Override
        protected Void doInBackground(LocationMessage... params) {
            LocationMessage info = params[0];
            broadcastLocation(info.getLocation(), info.getChannel());
            return null;
        }

        private void broadcastLocation(Location location, String channel) {
            JSONObject message = new JSONObject();
            try {
                message.put("lat", location.getLatitude());
                message.put("lng", location.getLongitude());
                message.put("bnum", Double.parseDouble(busNumber));
                message.put("free", 12.0);
                //message.put("free", );
            } catch (JSONException e) {
                //Log.e(TAG, e.toString());
            }
            //Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
            mPubnub.publish()
                    .message(message)
                    .channel(channel)
                    .async(new PNCallback<PNPublishResult>() {
                        @Override
                        public void onResponse(PNPublishResult result, PNStatus status) {
                            // handle publish result, status always present, result if successful
                            // status.isError to see if error happened
                            if (status.isError()) {
                                //Toast.makeText(LocationBroadcastActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void askForGPS() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        result = LocationServices.SettingsApi.checkLocationSettings(mGoogleClientApi, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            status.startResolutionForResult(LocationBroadcastActivity.this, 100);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                        break;
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMarkerClickListener(this);
        boolean success = mMap.setMapStyle(new MapStyleOptions(getResources().getString(R.string.style_json)));


        if (!success) {
            Log.e(TAG, "Style parsing failed.");
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                ActivityCompat.requestPermissions(this,
                        LOCATION_PERMS,
                        LOCATION_REQUEST);
            } else {
                ActivityCompat.requestPermissions(this,
                        LOCATION_PERMS,
                        LOCATION_REQUEST);
            }
            return;
        }
        mMap.setMyLocationEnabled(true);

        if (mGoogleClientApi.isConnected()) {
            Location current = LocationServices.FusedLocationApi.getLastLocation(mGoogleClientApi);
            LatLng latLng = new LatLng(current.getLatitude(), current.getLongitude());
            CameraPosition cp = CameraPosition.builder().target(latLng).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp), 1000, null);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerUtil.getDrawer() != null && drawerUtil.getDrawer().isDrawerOpen()) {
            drawerUtil.getDrawer().closeDrawer();
        } else {
            super.onBackPressed();
        }
    }


}