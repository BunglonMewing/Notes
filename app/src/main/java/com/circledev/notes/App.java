package com.circledev.notes;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {

    public static final String CHANNEL_REMINDER = "noteflow_reminders";
    public static final String CHANNEL_FOCUS = "noteflow_focus";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm == null) return;

            NotificationChannel reminders = new NotificationChannel(
                    CHANNEL_REMINDER, "Pengingat catatan", NotificationManager.IMPORTANCE_DEFAULT);
            reminders.setDescription("Pemberitahuan saat pengingat catatan tiba");
            nm.createNotificationChannel(reminders);

            NotificationChannel focus = new NotificationChannel(
                    CHANNEL_FOCUS, "Mode fokus", NotificationManager.IMPORTANCE_DEFAULT);
            focus.setDescription("Pemberitahuan saat sesi Pomodoro selesai");
            nm.createNotificationChannel(focus);
        }
    }
}
