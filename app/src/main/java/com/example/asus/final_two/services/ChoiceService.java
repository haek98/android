package com.example.asus.final_two.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.asus.final_two.activities.MainActivity;
import com.example.asus.final_two.helperclasses.Constants;
import com.example.asus.final_two.helperclasses.myLocation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChoiceService extends IntentService {

    public ChoiceService() {
        super("choice");
    }
    public ChoiceService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.cancel(Constants.NOTIFICATION_ID.BROADCAST);
        String uid=intent.getStringExtra("uid");
        String state=intent.getStringExtra("state");
        SharedPreferences pref=getSharedPreferences("com.example.asus.final_two",MODE_PRIVATE);
        SharedPreferences.Editor editor=pref.edit();
        Log.e("CServiceTag",state+" "+uid);
        Vibrator v= (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        v.cancel();
        if(intent.getAction().equals(Constants.ACTION_OK))
        {
            final DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child(state).child(uid);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    myLocation obj= dataSnapshot.getValue(myLocation.class);
                    obj.setPermanent(true);
                    databaseReference.setValue(obj);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            editor.putString("savedState",state);
            editor.apply();
            Intent mainIntent=new Intent(this,MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);
        }
        else if(intent.getAction().equals(Constants.ACTION_REMOVE))
        {
            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference().child(state).child(uid);
            databaseReference.removeValue();
            editor.putString("savedState","nothing");
            editor.apply();
            Intent mainIntent=new Intent(this,MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(mainIntent);
        }
    }
}
