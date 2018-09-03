package com.example.asus.final_two.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.asus.final_two.activities.MainActivity;
import com.example.asus.final_two.helperclasses.Constants;
import com.example.asus.final_two.helperclasses.MapMarker;
import com.example.asus.final_two.R;
import com.example.asus.final_two.receivers.ChoiceReceiver;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SetFutureService extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {
    GoogleApiClient googleApiClient;
    LocationRequest locationRequest;
    LatLng latLng;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    String state,uid;
    int sid;
    @Override
    public void onCreate() {
        super.onCreate();
        firebaseDatabase=FirebaseDatabase.getInstance();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sid=startId;
        if(intent.getAction().equals(Constants.ACTION_START_SERVICE)) {
            latLng=new LatLng(intent.getDoubleExtra("latKey",0.0),intent.getDoubleExtra("longKey",0.0));
            state=intent.getStringExtra("state");
            uid=intent.getStringExtra("uid");
            Log.e("ServiceTag","service started");
            databaseReference=firebaseDatabase.getReference().child(state).child(uid);
            Intent activityIntent = new Intent(this, MainActivity.class);
            activityIntent.setAction(Constants.ACTION_START_ACTIVITY);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent p_activityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
            Intent stopIntent = new Intent(this, SetFutureService.class);
            stopIntent.setAction(Constants.ACTION_STOP_SERVICE);
            PendingIntent p_stopIntent = PendingIntent.getService(this, 0, stopIntent, 0);
            googleApiClient=new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            googleApiClient.connect();
            Notification notification = new NotificationCompat.Builder(this)
                    .setContentTitle("Saathi")
                    .setTicker("Saathi")
                    .setContentText("Location Timer")
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(p_activityIntent)
                    .setOngoing(true)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .addAction(android.R.drawable.ic_notification_clear_all,
                            "Cancel", p_stopIntent)
                    .build();
            startForeground(Constants.NOTIFICATION_ID.FOREGROUND_SERVICE,
                    notification);
        }
        else if(intent.getAction().equals(Constants.ACTION_STOP_SERVICE))
        {
            Log.e("ServiceTag","service stopped");
            databaseReference.removeValue();
            SharedPreferences pref=getSharedPreferences("com.example.asus.final_two",MODE_PRIVATE);
            SharedPreferences.Editor editor=pref.edit();
            editor.putString("savedState","nothing");
            editor.apply();
            stopForeground(true);
            googleApiClient.disconnect();
            stopSelf();
            Intent stoppedIntent=new Intent(this,MainActivity.class);
            startActivity(stoppedIntent);
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        MapMarker obj=new MapMarker();
        locationRequest=LocationRequest.create().setInterval(30000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        double temp=obj.distC(location.getLatitude(),location.getLongitude(),latLng.latitude,latLng.longitude);
        if(temp<=200){
            Log.e("ServiceTag","connected stopped after finding destination "+String.valueOf(temp));
            googleApiClient.disconnect();
            stopForeground(true);
            Intent intent=new Intent("ac");
            intent.putExtra("state",state);
            intent.putExtra("uid",uid);
            intent.setClass(this,ChoiceReceiver.class);
            Log.e("ServiceTag",state+" "+uid);
            this.sendBroadcast(intent);
            stopSelf(sid);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        MapMarker obj=new MapMarker();
        double dist=obj.distC(location.getLatitude(),location.getLongitude(),latLng.latitude,latLng.longitude);
        Log.e("ServiceTag","onlocationChanged called");
        Log.e("ServiceTag","locations sent to service "+(dist));
        if(dist<=200){
            Log.e("ServiceTag","service stopped after finding destination");
            googleApiClient.disconnect();
            stopForeground(true);
            Intent intent=new Intent("abc");
            intent.putExtra("state",state);
            intent.putExtra("uid",uid);
            intent.setClass(this,ChoiceReceiver.class);
            Log.e("ServiceTag",state+" "+uid);
            this.sendBroadcast(intent);
            stopSelf(sid);
        }
    }
}
