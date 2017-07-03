package com.ehab.driverbroadcast.ui;

import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
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
import com.ehab.driverbroadcast.model.User;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.models.consumer.PNPublishResult;
import com.pubnub.api.models.consumer.PNStatus;

import org.json.JSONException;
import org.json.JSONObject;

import static com.google.android.gms.location.LocationServices.FusedLocationApi;

public class LocationBroadcastActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, LocationListener, OnMapReadyCallback {

    public static final String TAG = LocationBroadcastActivity.class.getName();

    Toolbar mToolbar;
    private Switch mSwitcher;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleClientApi;
    private LocationRequest mLocationRequest;
    PendingResult<LocationSettingsResult> result;

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
    private DatabaseReference mUsersReference;
    String username;
    String email;

    Drawer drawer;
    AccountHeader headerResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_broadcast);

        mToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        this.buildGoogleApiClient();

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

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        mSwitcher = (Switch) findViewById(R.id.switcher);
        mSwitcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked)
                    if ( manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        mGoogleClientApi.connect();
                    } else{
                        Toast.makeText(LocationBroadcastActivity.this, "Enabling GPS is MANDATORY", Toast.LENGTH_SHORT).show();
                        mSwitcher.setChecked(false);
                        mSwitcher.setChecked(false);
                        askForGPS2();
                    }
                else
                    mGoogleClientApi.disconnect();
            }
        });

        //retrieve user data from firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        userId = currentUser.getUid();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUsersReference = mFirebaseDatabase.getReference().child("users");

        mUsersReference.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username = user.username;
                email = user.email;

/*
                if(user.photoUrl == null)
                    mProfileImageView.setImageResource(R.drawable.default_thumbnail);
                else
                    Glide.with(mProfileImageView.getContext()).load(user.photoUrl).into(mProfileImageView);
*/
                //headerResult.updateProfileByIdentifier(new ProfileDrawerItem().withName(username));

                SetupNavigationDrawer(mToolbar, username, email);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

        //Setting up Navigation Drawer
        email = "ehabhamdy2012@gmail.com";
        SetupNavigationDrawer(mToolbar, username, email);

    }

    private void SetupNavigationDrawer(Toolbar mToolbar, String username, String email) {
        // Create the AccountHeader
        headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.drawer_bg)
                .addProfiles(
                        new ProfileDrawerItem().withName(username).withEmail(email).withIcon(getResources().getDrawable(R.drawable.toobar_icon))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();

        //new DrawerBuilder().withActivity(this).withAccountHeader(headerResult).build();

        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem main = new PrimaryDrawerItem().withIdentifier(1).withName(R.string.nav_main_label).withIcon(R.drawable.ic_room_black_24dp);
        PrimaryDrawerItem subsToLine = new PrimaryDrawerItem().withIdentifier(2).withName(R.string.nav_subscribe_label).withIcon(R.drawable.ic_trending_up_black_24dp);
        PrimaryDrawerItem profile = new PrimaryDrawerItem().withIdentifier(3).withName(R.string.nav_profile_label).withIcon(R.drawable.ic_person_black_24dp);
        PrimaryDrawerItem settings = new PrimaryDrawerItem().withIdentifier(4).withName(R.string.nav_settings_label).withIcon(R.drawable.ic_settings_black_24dp);
        PrimaryDrawerItem logout = new PrimaryDrawerItem().withIdentifier(5).withName(R.string.nav_logout_label).withIcon(R.drawable.ic_out_black_24dp);
        //create the drawer and remember the `Drawer` result object
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(mToolbar)
                .addDrawerItems(
                        main,
                        new DividerDrawerItem(),
                        subsToLine,
                        new DividerDrawerItem(),
                        profile,
                        new DividerDrawerItem(),
                        settings,
                        new DividerDrawerItem(),
                        logout)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        switch (position) {
                            case 1:
                                drawer.closeDrawer();
                                return true;
                            case 3:
                                Toast.makeText(LocationBroadcastActivity.this, "lines", Toast.LENGTH_SHORT).show();
                                return true;
                            case 5:
                                Intent openProfileIntent = new Intent(getApplicationContext(), ProfileActivity.class);
                                startActivity(openProfileIntent);
                                return true;
                            case 7:
                                break;
                            case 9:
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                        }


                        return true;
                    }
                })
                .withAccountHeader(headerResult)
                .build();


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
        mGoogleClientApi.disconnect();
        super.onStop();
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

       /* Location current = LocationServices.FusedLocationApi.getLastLocation(mGoogleClientApi);

        if(current != null) {
            LatLng latLng = new LatLng(current.getLatitude(), current.getLongitude());
            CameraPosition cp = CameraPosition.builder().target(latLng).zoom(16).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp), 1000, null);
        }*/
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
        broadcastLocation(location);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            if(requestCode == LOCATION_REQUEST){
                askForGPS();
            }
        }

    }

    private void broadcastLocation(Location location) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", location.getLatitude());
            message.put("lng", location.getLongitude());
            message.put("alt", location.getAltitude());
        } catch (JSONException e) {
            //Log.e(TAG, e.toString());
        }
        Toast.makeText(this, "Sent", Toast.LENGTH_SHORT).show();
        mPubnub.publish()
                .message(message)
                .channel("awesome-channel")
                .async(new PNCallback<PNPublishResult>() {
                    @Override
                    public void onResponse(PNPublishResult result, PNStatus status) {
                        // handle publish result, status always present, result if successful
                        // status.isError to see if error happened
                        if(status.isError()){
                            Toast.makeText(LocationBroadcastActivity.this, "Error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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

        if(mGoogleClientApi.isConnected()) {
            Location current = LocationServices.FusedLocationApi.getLastLocation(mGoogleClientApi);
            LatLng latLng = new LatLng(current.getLatitude(), current.getLongitude());
            CameraPosition cp = CameraPosition.builder().target(latLng).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cp), 1000, null);
        }
    }
}