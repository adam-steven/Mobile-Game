package com.example.cmp309spacegame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

//when set goes off this output a notification
public class AlertReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //this displays thew notification that is stored in the notificationHelper
        NotificationHelper notificationHelper = new NotificationHelper(context);
        NotificationCompat.Builder nBuilder = notificationHelper.getChannelNotification();
        notificationHelper.getManager().notify(1, nBuilder.build());
    }
}
