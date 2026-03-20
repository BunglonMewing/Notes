package com.circledev.notes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {

        // Ambil data catatan dari SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("NoteFlowWidget", Context.MODE_PRIVATE);
        String title   = prefs.getString("widget_title",   "Catatan Terbaru");
        String content = prefs.getString("widget_content", "Tap untuk buka NoteFlow");
        String date    = prefs.getString("widget_date",    "Hari ini");

        // Waktu sekarang
        String time = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());

        // Build RemoteViews
        RemoteViews views = new RemoteViews(
            context.getPackageName(),
            R.layout.widget_layout
        );

        // Set teks
        views.setTextViewText(R.id.widget_title,   title);
        views.setTextViewText(R.id.widget_content, content);
        views.setTextViewText(R.id.widget_date,    date);
        views.setTextViewText(R.id.widget_time,    time);

        // Tap widget → buka MainActivity
        Intent openApp = new Intent(context, MainActivity.class);
        openApp.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingOpen = PendingIntent.getActivity(
            context, 0, openApp,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        views.setOnClickPendingIntent(R.id.widget_root, pendingOpen);

        // Update widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
