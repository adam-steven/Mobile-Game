package com.example.cmp309spacegame;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;

import androidx.core.app.NotificationCompat;

//this stores all the data for the notification
public class NotificationHelper extends ContextWrapper {
    public static final String channelID = "channelID";
    public static final String channelName = "Channel Name";

    NotificationManager mManager;

    public NotificationHelper(Context base)
    {
        super(base);
        //check to insure channels are supported on the device
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
        //create a new channel with low importance ("Shows in the shade")
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_LOW);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        //initializes the NotificationManager
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    public NotificationCompat.Builder getChannelNotification() {
        //this is the notification that is displayed when the AlertReceiver requests it
        return new NotificationCompat.Builder(getApplicationContext(), channelID)
                .setContentTitle("CMP309")
                .setContentText("Reminder: A game a day keeps the skills from going away.")
                .setSmallIcon(R.drawable.sprite);
    }
}
