package com.lumoo;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lumoo.HomeActivity;
import com.lumoo.LoginActivity;
import com.lumoo.R;

import io.reactivex.rxjava3.annotations.NonNull;

 public class MessageService extends FirebaseMessagingService {
    private NotificationManager notificationManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        updateNewToken(token);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        // Titreşim ayarları
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 100, 2000, 200, 2000}; // Titreşim deseni
        vibrator.vibrate(pattern, -1);

        // Bildirim sesi
        Uri soundUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.notiorder);

        // Data payload kullanımı
        String title = "";
        String body = "";
        if (message.getData().size() > 0) {
            title = message.getData().get("title");
            body = message.getData().get("body");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Notification");
        Intent resultIntent = new Intent(this, HomeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, resultIntent, PendingIntent.FLAG_MUTABLE);

        builder.setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.lunoo_logo)
                .setVibrate(pattern)
                .setSound(soundUri)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Android 8+ için kanal
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelID = "Notification";
            NotificationChannel channel = new NotificationChannel(
                    channelID, "Bildirim Kanalı", NotificationManager.IMPORTANCE_HIGH
            );
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setVibrationPattern(pattern);

            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            channel.setSound(soundUri, audioAttributes);

            channel.setBypassDnd(true);
            notificationManager.createNotificationChannel(channel);
            builder.setChannelId(channelID);
        }

        notificationManager.notify(100, builder.build());
    }

    private void updateNewToken(String token) {
        // Token güncelleme işlemi
    }
}
