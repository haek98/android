package com.example.asus.final_two.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.asus.final_two.asynctasks.AsyncGeocoder;
import com.example.asus.final_two.helperclasses.Constants;
import com.example.asus.final_two.helperclasses.MapClass;
import com.example.asus.final_two.helperclasses.MapMarker;
import com.example.asus.final_two.helperclasses.myLocation;
import com.example.asus.final_two.R;
import com.example.asus.final_two.services.StickyService;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, ActivityCompat.OnRequestPermissionsResultCallback {
    SearchView mSearchView;
    Map<String, Marker> mapMarker;
    @BindView(R.id.searchButton)
    FloatingActionButton button;
    myLocation mycoord;
    GoogleMap googleMap;
    boolean mapReady = false;
    GoogleApiClient googleApiClient;
    Location location;
    FusedLocationProviderClient mFusedLocationClient;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int resultGranted = 0, flag = 0, recreate = 0, locationCheck = 0, loginFlag = 0,locationDialog = 0, settingDialog = 0, exitFlag=0, chatFlag=0;
    MapFragment frag;
    String uid, savedState, userName, userMail;
    Uri userPhotoUrl;
    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener authStateListener;
    static String state = "nothing";
    LoaderManager.LoaderCallbacks<Bundle> loader1;
    Context context;
    AlertDialog.Builder exitDialogB,chatDialogB;
    AlertDialog exitDialog,chatDialog;
    private ChildEventListener childEventListener;
    @BindView(R.id.chatButton)
    ImageButton chatButton;
    DrawerLayout drawerLayout;
    ProgressDialog progressBar;
    @BindView(R.id.exitButton)
    ImageButton exitButton;
    ActionBarDrawerToggle actionBarDrawerToggle;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    ProgressDialog progressDialog;
    Handler handler;
    Thread getStateThread;
    @Override
    protected void onPause() {
        super.onPause();
        if (authStateListener != null)
            firebaseAuth.removeAuthStateListener(authStateListener);
    }

    public void onResume() {
        if (resultGranted == 0 || locationCheck == 0) {
            enableMyLocation();
        }
        if (locationCheck == 1)
            googleApiClient.connect();
        firebaseAuth.addAuthStateListener(authStateListener);
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (googleApiClient.isConnected())
            googleApiClient.disconnect();
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState!=null)
        {
            locationDialog=savedInstanceState.getInt("locationDialog");
            settingDialog=savedInstanceState.getInt("settingDialog");
            exitFlag=savedInstanceState.getInt("exitFlag");
            chatFlag=savedInstanceState.getInt("chatFlag");
        }
        Log.e("MainTag",String.valueOf(locationDialog)+" "+String.valueOf(settingDialog));
        setContentView(R.layout.activity_main);
        Log.e("MainTag", "MainActivity called");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        context = this;
        pref = getSharedPreferences("com.example.asus.final_two", MODE_PRIVATE);
        savedState = pref.getString("savedState", "nothing");
        editor=pref.edit();
        editor.putBoolean("running",true);
        editor.apply();
        Intent stickyService = new Intent(this, StickyService.class);
        startService(stickyService);
        state = savedState;
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("fetching location...");
        progressDialog.setCancelable(false);
        progressBar=new ProgressDialog(this);
        progressBar.setMessage("Resolving Address...");
        progressBar.setCancelable(false);
        chatButton.setVisibility(View.GONE);
        exitButton.setVisibility(View.GONE);
        handler=new Handler();
        getStateThread=new Thread(new stateDialogThread());
        drawerLayout = findViewById(R.id.g_drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        final NavigationView navigationView = findViewById(R.id.nav_view);
        final Intent intent = new Intent(this, SetTimerActivity.class);
        exitDialogB = new AlertDialog.Builder(context);
        exitDialogB.setTitle("Exit application");
        exitDialogB.setMessage("Do you really want to exit");
        exitDialogB.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel() ;
                exitFlag=0;
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (Map.Entry<String, Marker> entry : mapMarker.entrySet()) {
                    entry.getValue().remove();
                }
                MapClass.hash.clear();
                mapMarker.clear();
                editor = pref.edit();
                editor.putString("savedState", "nothing");
                editor.apply();
                databaseReference.child(state).child(uid).removeEventListener(childEventListener);
                googleApiClient.disconnect();
                databaseReference.child("messages").child(state).removeValue();
                databaseReference.child(state).child(uid).removeValue();
                exitFlag=0;
                dialogInterface.cancel();
                finish();
            }
        }).setCancelable(false);
        exitDialog=exitDialogB.create();
        chatDialogB=new AlertDialog.Builder(context);
        chatDialogB.setCancelable(false);
        chatDialogB.setTitle("Enter ChatRoom");
        chatDialogB.setMessage("Do you want to enter ChatRoom");
        chatDialogB.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chatFlag=0;
                dialogInterface.cancel();
            }
        }).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chatFlag=0;
                dialogInterface.cancel();
                Intent intent = new Intent(context, ChatActivity.class);
                startActivity(intent);
            }
        });
        chatDialog=chatDialogB.create();
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        drawerLayout.closeDrawers();
                        int itemId = menuItem.getItemId();
                        if (itemId == R.id.timerItem) {
                            startActivity(intent);
                        } else if (itemId == R.id.logOutItem) {
                            loginFlag = 0;
                            editor = pref.edit();
                            editor.putString("savedState", "nothing");
                            editor.apply();
                            AuthUI.getInstance().signOut(context);
                        }
                        return true;
                    }
                });
        MapClass temp = new MapClass();
        mapMarker = new HashMap<>();
        Log.e("TAG", "2");
        mycoord = new myLocation();
        mycoord.setDestLongi(0.0);
        mycoord.setDestLati(0.0);
        mycoord.setPermanent(true);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        Log.e("TAG", "3");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        Log.e("TAG", "db created");
        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {try{
                    uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
                    userMail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    userPhotoUrl = FirebaseAuth.getInstance().getCurrentUser().getPhotoUrl();
                    View header = navigationView.getHeaderView(0);
                    Glide.with(context).load(userPhotoUrl).into((ImageView) header.findViewById(R.id.profile_image));
                    TextView user_name = (header.findViewById(R.id.user_name)), user_mail = (header.findViewById(R.id.user_mail));
                    user_name.setText(userName);
                    user_mail.setText(userMail);}
                catch(Exception e)
                {
                    Log.e("MainTag",e.getMessage());
                }
                } else {
                    if (loginFlag == 0) {
                        startActivityForResult(
                                AuthUI.getInstance()
                                        .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                                        .setAvailableProviders(Arrays.asList(
                                                new AuthUI.IdpConfig.EmailBuilder().build(),

                                                new AuthUI.IdpConfig.GoogleBuilder().build()
                                        ))
                                        .build(),
                                Constants.RC_SIGN_IN);
                        loginFlag = 1;
                        Log.e("MainTag", "firebaseAuthUi strted again");
                    }
                }
            }
        };

        loader1 = new LoaderManager.LoaderCallbacks<Bundle>() {
            @NonNull
            @Override
            public android.support.v4.content.Loader<Bundle> onCreateLoader(int id, @Nullable Bundle bundle) {
                Log.e("TAG", "thread called");
                String address = bundle.getString("address");
                LatLng latLng = new LatLng(bundle.getDouble("lat"), bundle.getDouble("long"));
                return new AsyncGeocoder(context, address, latLng);
            }

            @Override
            public void onLoadFinished(@NonNull android.support.v4.content.Loader<Bundle> loader, Bundle bundle) {
                Log.e("TAG", "thread also called");
                state = bundle.getString("state");
                savedState = state;
                editor = pref.edit();
                editor.putString("savedState", savedState);
                editor.apply();
                getStateThread.interrupt();
                mycoord.setDestLati(bundle.getDouble("destLat"));
                mycoord.setDestLongi(bundle.getDouble("destLong"));
                Log.e("TAG", state);
            }

            @Override
            public void onLoaderReset(@NonNull android.support.v4.content.Loader<Bundle> loader) {

            }
        };

//        newDestination=new Destination(this,this);
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final MapMarker mm = new MapMarker();
                final myLocation obj = dataSnapshot.getValue(myLocation.class);
                Log.e("TAG", "wrong call");
                if (mm.distC(location.getLatitude(), location.getLongitude(), obj.getmLati(), obj.getmLongi()) <= 200
                        && mm.distC(mycoord.destLati, mycoord.destLongi, obj.getDestLati(), obj.getDestLongi()) <= 300 && !dataSnapshot.getKey().equals(uid)) {
                    if (obj.getPermanent())
                        mapMarker.put(dataSnapshot.getKey(), googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(obj.mLati, obj.mLongi)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                    else
                        mapMarker.put(dataSnapshot.getKey(), googleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(obj.mLati, obj.mLongi)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                    Log.e("MainTag", "marker set " + String.valueOf(mapMarker.size()));
                    MapClass.hash.put(dataSnapshot.getKey(), new LatLng(obj.mLati, obj.mLongi));
                }
                if (mapMarker.size() >= 1) {
                    chatButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final MapMarker mm = new MapMarker();
                final myLocation obj = dataSnapshot.getValue(myLocation.class);
                Log.e("TAG", dataSnapshot.getKey());
                Log.e("TAG", String.valueOf(obj.destLati));
                Log.e("TAG", String.valueOf(mapMarker.containsKey(dataSnapshot.getKey())));
                if (mapMarker.containsKey(dataSnapshot.getKey())) {
                    Marker ma = mapMarker.get(dataSnapshot.getKey());
                    if (ma == null)
                        Log.e("TAG", "null marker");
                    else {
                        ma.remove();
                        MapClass.hash.remove(dataSnapshot.getKey());
                        mapMarker.remove(dataSnapshot.getKey());
                    }
                }
                if (mm.distC(location.getLatitude(), location.getLongitude(), obj.getmLati(), obj.getmLongi()) <= 200
                        && mm.distC(mycoord.destLati, mycoord.destLongi, obj.getDestLati(), obj.getDestLongi()) <= 300 && !dataSnapshot.getKey().equals(uid)) {
                    Log.e("TAG", "true again");
                    mapMarker.put(dataSnapshot.getKey(), googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(obj.mLati, obj.mLongi)).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))));
                    MapClass.hash.put(dataSnapshot.getKey(), new LatLng(obj.mLati, obj.mLongi));
                }
                if (mapMarker.size() >= 1) {
                    chatButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                if (mapMarker.containsKey(dataSnapshot.getKey())) {
                    (mapMarker.get(dataSnapshot.getKey())).remove();
                    mapMarker.remove(dataSnapshot.getKey());
                    MapClass.hash.remove(dataSnapshot.getKey());
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        frag = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        frag.getMapAsync(this);
        Log.e("TAG", "reached");
    }


    @SuppressLint("MissingPermission")
    @OnClick(R.id.searchButton)
    public void click() {
        Log.e("TAG", "clicked");
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(location!=null)
        {
        mycoord.setmLati(location.getLatitude());
        mycoord.setmLongi(location.getLongitude());
        Log.e("TAG", "state is " + state);
        if (!state.equals("nothing")) {
            databaseReference.child(state).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        databaseReference.child(state).child(uid).setValue(mycoord);
                    } else databaseReference.child(state).child(uid).setValue(mycoord);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            chatButton.setVisibility(View.VISIBLE);
            button.setVisibility(View.GONE);
            exitButton.setVisibility(View.VISIBLE);
            databaseReference.child(state).addChildEventListener(childEventListener);
        } else {
            Snackbar snackbar = Snackbar
                    .make(getWindow().getDecorView().getRootView(), "Enter Destination", Snackbar.LENGTH_LONG);
            snackbar.show();
        }}
        else{
            Snackbar snackbar = Snackbar
                    .make(getWindow().getDecorView().getRootView(), "Please turn on location services and restart the app", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);

        mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Enter destination");
        mSearchView.setIconified(false);
        mSearchView.setFocusable(false);
        mSearchView.setClickable(true);
//        mSearchView.setInputType(0x00000000);
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint("MissingPermission")
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.e("TAG", "button called");
                Bundle bundle = new Bundle();
                String str;
                str = String.valueOf(mSearchView.getQuery());
                Log.e("TAG", "we are getting " + str);
                bundle.putString("address", str);
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                bundle.putDouble("lat", location.getLatitude());
                bundle.putDouble("long", location.getLongitude());
                getStateThread.start();
                if (flag == 0)
                    getSupportLoaderManager().initLoader(1, bundle, loader1).forceLoad();
                else getSupportLoaderManager().restartLoader(1, bundle, loader1).forceLoad();
                flag = 1;
//                newDestination.setDestination(str,flag,googleApiClient);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String str = String.valueOf(mSearchView.getQuery());
                if (!str.equals(""))
                    Toast.makeText(context, "Click on Search icon to confirm", Toast.LENGTH_SHORT).show();
                else {

                    PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                    try {
                        startActivityForResult(builder.build((Activity) context), Constants.PLACE_PICKER_REQUEST);
                    } catch (GooglePlayServicesRepairableException e) {
                        e.printStackTrace();
                    } catch (GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (actionBarDrawerToggle.onOptionsItemSelected(item))
            return true;
        return super.onOptionsItemSelected(item);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("TAG", "it maybe works");
        Log.e("TAG", "7");
        mapReady = true;
        this.googleMap = googleMap;
        googleMap.setPadding(0, 200, 0, 0);
        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        googleMap.setTrafficEnabled(true);
        CameraPosition target = CameraPosition.builder().target(new LatLng(-96.865, 111.111)).zoom(15).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        enableMyLocation();

    }

    private void displayLocationSettingsRequest(Context context) {
        GoogleApiClient googleApiClient1 = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient1.connect();

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(100);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        Log.e("MainTag","builder built");
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient1, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        Log.i("TAG", "All location settings are satisfied.");
                        Log.e("MainTag","builder result");
                        locationCheck = 1;
                        Log.e("MainTag", "locationCheck granted without check");
                        googleMap.setMyLocationEnabled(true);
//                        progressDialog.show();
                        googleApiClient.connect();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i("TAG", "Location settings are not satisfied. Show the user a dialog to upgrade location settings ");

                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the result
                            // in onActivityResult().
                            if(locationDialog!=1) {
                                locationDialog = 1;
                                status.startResolutionForResult(MainActivity.this, Constants.REQUEST_CHECK_SETTINGS);
                            }
                        } catch (IntentSender.SendIntentException e) {
                            Log.i("TAG", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        Log.i("TAG", "Location settings are inadequate, and cannot be fixed here. Dialog not created.");
                        break;
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                Log.e("TAG", "location granted");
                googleMap.setMyLocationEnabled(true);
                Log.e("MainTag", "locationCheck granted after check");
                locationCheck = 1;
//                progressDialog.show();
                locationDialog=0;
                googleApiClient.connect();
            }
        } else if (requestCode == Constants.RC_SIGN_IN) {
            if (resultCode == RESULT_CANCELED)
                finish();
        } else if (requestCode == Constants.PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                mSearchView.setQuery(place.getAddress(), false);
            }
        }
    }


    private void enableMyLocation() {
        Log.e("TAG", "ask");
        Log.e("TAG", "4");

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            resultGranted = 1;
            displayLocationSettingsRequest(this);

        } else {
            if (settingDialog!=1) {
                settingDialog=1;
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        Constants.REQUEST_CODE);
            }
        }
    }


    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        resultGranted = 0;
        switch (requestCode) {
            case Constants.REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("TAG", "code granted");
                    settingDialog=0;
                    resultGranted = 1;
                    enableMyLocation();
                }
            }
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
        CameraPosition target = CameraPosition.builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();
        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
        Log.e("TAG", location.toString() + "  yes");
        mycoord.setmLati(location.getLatitude());
        mycoord.setmLongi(location.getLongitude());
        if (!state.equals("nothing")) {
            databaseReference.child(state).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        databaseReference.child(state).child(uid).setValue(mycoord);
                    }
                }


                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("TAG", "it works " + String.valueOf(resultGranted + " " + String.valueOf(locationCheck)));
        //  locationRequest = LocationRequest.create();
//        locationRequest.setInterval(1000)
//                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (resultGranted == 1 && locationCheck == 1) {
            Log.e("MainTag", "conditions satisfied");
            if (recreate == 0) {
                if (!savedState.equals("nothing")) {
                    Log.e("MainTag", savedState);
                    chatButton.setVisibility(View.VISIBLE);
                    exitButton.setVisibility(View.VISIBLE);
                    button.setVisibility(View.GONE);
                    databaseReference.child(savedState).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            myLocation temp = dataSnapshot.getValue(myLocation.class);
                            mycoord.setDestLati(temp.getDestLati());
                            mycoord.setDestLongi(temp.getDestLongi());
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    databaseReference.child(savedState).addChildEventListener(childEventListener);
                    if(exitFlag==1)
                    {
                        exitDialog.show();
                    }
                    if(chatFlag==1)
                        chatDialog.show();
                }


                recreate = 1;
            }
            if (location != null)
                Log.e("MainTag", "location not null beforehand");
            location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            new Thread(new progressThread()).start();
            /*do {
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                Log.e("MainTag", "fetching");
            } while (location == null);

            if (location != null) {
                progressDialog.dismiss();
                CameraPosition target = CameraPosition.builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(20).build();
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                Log.e("TAG", location.toString() + "  connected");
            } else {
                Log.e("MainTag", "location null");
            }*/
        }
    }


    @OnClick(R.id.chatButton)
    public void chatClick(View view) {
        chatFlag=1;
        chatDialog.show();
    }

    @OnClick(R.id.exitButton)
    void exitClick() {
        exitFlag=1;
        exitDialog.show();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show();
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("locationDialog",locationDialog);
        outState.putInt("settingDialog",settingDialog);
        outState.putInt("exitFlag",exitFlag);
        outState.putInt("chatFlag",chatFlag);
        Log.e("MainTag","Restore called");
    }

    class progressThread implements  Runnable
    {

        @SuppressLint("MissingPermission")
        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.show();
                }
            });
            while(location==null)
            {
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
                Log.e("TAG", "getting location");
            }
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                    CameraPosition target = CameraPosition.builder().target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(15).build();
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(target));
                    Log.e("TAG", location.toString() + "  connected");
                }
            });
        }
    }

    class stateDialogThread implements  Runnable
    {

        @Override
        public void run() {

            while(true) {
                try {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.show();
                        }
                    });
                    Thread.sleep(4);
                } catch (InterruptedException e) {
                    if (progressBar.isShowing())
                        progressBar.cancel();
                    break;
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(exitFlag==1)
            exitDialog.dismiss();
        if(chatFlag==1)
            chatDialog.dismiss();
        if(!isChangingConfigurations())
        {
            editor=pref.edit();
            editor.putBoolean("running",false);
            editor.apply();
        }
        if(!savedState.equals("nothing"))
            databaseReference.child(state).child(uid).removeEventListener(childEventListener);
    }
}