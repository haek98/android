package com.example.asus.final_two.receivers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.example.asus.final_two.helperclasses.Constants;
import com.example.asus.final_two.R;
import com.example.asus.final_two.services.ChoiceService;

public class ChoiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("ServiceTag","broadcast started");
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 1000};
        v.vibrate(pattern,0);
        Intent okIntent = new Intent(context, ChoiceService.class);
        okIntent.setAction(Constants.ACTION_OK);
        okIntent.putExtra("state",intent.getStringExtra("state"));
        okIntent.putExtra("uid",intent.getStringExtra("uid"));
        Log.e("RServiceTag",intent.getStringExtra("state")+" "+intent.getStringExtra("uid"));
        PendingIntent p_okIntent = PendingIntent.getService(context, 0, okIntent, 0);
        Intent removeIntent = new Intent(context, ChoiceService.class);
        removeIntent.putExtra("state",intent.getStringExtra("state"));
        removeIntent.putExtra("uid",intent.getStringExtra("uid"));
        removeIntent.setAction(Constants.ACTION_REMOVE);
        PendingIntent p_removeIntent = PendingIntent.getService(context, 0, removeIntent, 0);
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setContentTitle("Iter Simul")
                .setTicker("Iter Simul")
                .setContentText("Loaction Alarm")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setAutoCancel(true)
                .setPriority(Notification.PRIORITY_HIGH)
                .addAction(android.R.drawable.ic_menu_save,
                        "Set", p_okIntent)
                .addAction(android.R.drawable.ic_notification_clear_all,
                        "Cancel", p_removeIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Constants.NOTIFICATION_ID.BROADCAST, notification.build());
    }
}
