package com.circledev.notes;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    private static int requestCode(long id) {
        return (int) (id & 0x7FFFFFFF);
    }

    private static PendingIntent pending(Context context, Note note, int flags) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("noteId", note.id);
        intent.putExtra("title", note.title);
        String plain = note.body == null ? "" : note.body.replace('\n', ' ').trim();
        intent.putExtra("body", plain.length() > 140 ? plain.substring(0, 140) : plain);
        return PendingIntent.getBroadcast(context, requestCode(note.id), intent, flags);
    }

    public static void schedule(Context context, Note note) {
        if (note == null) return;
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = pending(context, note,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (note.remind > System.currentTimeMillis()) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, note.remind, pi);
        } else {
            am.cancel(pi);
            pi.cancel();
        }
    }

    public static void cancel(Context context, long noteId) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode(noteId), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        am.cancel(pi);
        pi.cancel();
    }

    public static void rescheduleAll(Context context) {
        Store store = Store.get(context);
        for (Note note : store.notes) {
            if (note.remind > 0) schedule(context, note);
        }
    }
}
