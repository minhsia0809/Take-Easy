package com.example.takeiteasy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class ScheduleNotifier extends BroadcastReceiver
{
    public static final String CHANNEL_ID = "myChannel";
    public static final String CHANNEL_NAME = "else";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onReceive(Context context, Intent intent)
    {
        String title = intent.getStringExtra("title");
        String msg = intent.getStringExtra("msg");

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
        manager.createNotificationChannel(channel);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                                                                new Intent(context, HomeUser.class),
                                                                PendingIntent.FLAG_CANCEL_CURRENT);
        Notification builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                                                     .setContentTitle(title)
                                                     .setContentText(msg)
                                                     .setSmallIcon(R.drawable.ic_launcher_foreground)
                                                     .setContentIntent(pendingIntent)
                                                     .setAutoCancel(true)
                                                     .build();
        manager.notify(1, builder);
    }
}
