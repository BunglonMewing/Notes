package com.circledev.notes;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;

/** Home screen widget with the four NoteFlow styles. */
public class NoteWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int id : appWidgetIds) {
            updateWidget(context, appWidgetManager, id);
        }
    }

    public static void updateAll(Context context) {
        try {
            AppWidgetManager manager = AppWidgetManager.getInstance(context);
            if (manager == null) return;
            ComponentName name = new ComponentName(context, NoteWidgetProvider.class);
            int[] ids = manager.getAppWidgetIds(name);
            for (int id : ids) updateWidget(context, manager, id);
        } catch (Exception ignored) {
        }
    }

    private static void updateWidget(Context context, AppWidgetManager manager, int widgetId) {
        Store store = Store.get(context);
        int style = store.widgetStyle;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);

        views.setViewVisibility(R.id.widget_line1, View.GONE);
        views.setViewVisibility(R.id.widget_line2, View.GONE);
        views.setViewVisibility(R.id.widget_line3, View.GONE);
        views.setViewVisibility(R.id.widget_body, View.GONE);

        if (style == 1) {
            Todo todo = store.todos.isEmpty() ? null : store.todos.get(0);
            if (todo == null) {
                views.setTextViewText(R.id.widget_title, "To-Do");
                views.setViewVisibility(R.id.widget_body, View.VISIBLE);
                views.setTextViewText(R.id.widget_body, "Belum ada to-do. Tap untuk membuat.");
            } else {
                views.setTextViewText(R.id.widget_title, todo.title);
                int[] lineIds = {R.id.widget_line1, R.id.widget_line2, R.id.widget_line3};
                for (int i = 0; i < lineIds.length && i < todo.items.size(); i++) {
                    Todo.TodoItem item = todo.items.get(i);
                    views.setViewVisibility(lineIds[i], View.VISIBLE);
                    views.setTextViewText(lineIds[i],
                            (item.done ? "✓ " : "○ ") + item.text);
                }
                if (todo.items.isEmpty()) {
                    views.setViewVisibility(R.id.widget_body, View.VISIBLE);
                    views.setTextViewText(R.id.widget_body, "Belum ada item.");
                }
            }
        } else if (style == 2) {
            Note note = store.notes.isEmpty() ? null : store.notes.get(0);
            views.setTextViewText(R.id.widget_title,
                    note == null ? "NoteFlow" : note.title);
            views.setViewVisibility(R.id.widget_body, View.VISIBLE);
            views.setTextViewText(R.id.widget_body,
                    note == null ? "Belum ada catatan." : preview(note));
            views.setViewVisibility(R.id.widget_line1, View.VISIBLE);
            views.setTextViewText(R.id.widget_line1, "+ Baru");
        } else if (style == 3) {
            views.setTextViewText(R.id.widget_title, "Catatan");
            int[] lineIds = {R.id.widget_line1, R.id.widget_line2, R.id.widget_line3};
            for (int i = 0; i < lineIds.length && i < store.notes.size(); i++) {
                views.setViewVisibility(lineIds[i], View.VISIBLE);
                views.setTextViewText(lineIds[i], "• " + store.notes.get(i).title);
            }
            if (store.notes.isEmpty()) {
                views.setViewVisibility(R.id.widget_body, View.VISIBLE);
                views.setTextViewText(R.id.widget_body, "Belum ada catatan.");
            }
        } else {
            Note note = store.notes.isEmpty() ? null : store.notes.get(0);
            views.setTextViewText(R.id.widget_title,
                    note == null ? "Catatan Terbaru" : note.title);
            views.setViewVisibility(R.id.widget_body, View.VISIBLE);
            views.setTextViewText(R.id.widget_body,
                    note == null ? "Tap untuk membuka NoteFlow" : preview(note));
        }

        views.setTextViewText(R.id.widget_date, Util.today());
        views.setTextViewText(R.id.widget_time, Util.time());

        Intent open = new Intent(context, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(context, 0, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_root, pending);

        manager.updateAppWidget(widgetId, views);
    }

    private static String preview(Note note) {
        String text = note.body == null ? "" : note.body.replace('\n', ' ').trim();
        if (text.length() > 120) text = text.substring(0, 120);
        return text.isEmpty() ? "Catatan kosong" : text;
    }
}
