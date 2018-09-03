package com.example.asus.final_two.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;

import com.example.asus.final_two.R;

public class SplashActivity extends Activity {
    boolean isConnected=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences pref=getSharedPreferences("com.example.asus.final_two",MODE_PRIVATE);
        boolean runState=pref.getBoolean("running",false);
        final Context context=this;
        {
            Log.e("SplashTag","network state checked");
            ConnectivityManager cm =
                    (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
        }
        {
        if(runState)
        {
            Log.e("SplashTag","just orientation change");
            Intent intent=new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }else{
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                if(isConnected)
                {
                    Log.e("SplashTag","main call");
                    Intent mainIntent=new Intent(context,MainActivity.class);
                    startActivity(mainIntent);
                    finish();
                }
                else{
                    Log.e("SplashTag","ni call");
                    Intent niIntent=new Intent(context,NoInternetActivity.class);
                    startActivity(niIntent);
                    finish();
                }
            }
        }, 1000);}}
    }
}
