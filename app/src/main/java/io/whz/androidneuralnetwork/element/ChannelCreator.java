package io.whz.androidneuralnetwork.element;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import io.whz.androidneuralnetwork.util.Precondition;

public class ChannelCreator {
    public static final String CHANNEL_ID = "io.whz.androidneuralnetwork.notification";
    private static final String CHANNEL_NAME = "Synapse Channel";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createChannel(@NonNull Context context) {
        Precondition.checkNotNull(context);

        final NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        final NotificationManager manager = getManager(context);
        manager.deleteNotificationChannel(CHANNEL_ID);
        manager.createNotificationChannel(channel);
    }

    private static NotificationManager getManager(@NonNull Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
