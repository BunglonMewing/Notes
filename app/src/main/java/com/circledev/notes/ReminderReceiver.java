package com.circledev.notes;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Util.hasNotificationPermission(context)) return;

        long noteId = intent.getLongExtra("noteId", 0);
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        if (title == null || title.trim().isEmpty()) title = "Pengingat catatan";
        if (body == null || body.trim().isEmpty()) body = "Buka NoteFlow untuk melihat catatan.";

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (noteId > 0) open.putExtra("noteId", noteId);
        PendingIntent pending = PendingIntent.getActivity(context, (int) (noteId & 0x7FFFFFFF),
                open, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(context, App.CHANNEL_REMINDER);
        } else {
            builder = new Notification.Builder(context);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        builder.setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("🔔 " + title)
                .setContentText(body)
                .setStyle(new Notification.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pending);

        NotificationManager nm = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) (noteId & 0x7FFFFFFF) + 1, builder.build());
    }
}
