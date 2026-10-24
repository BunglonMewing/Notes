package com.circledev.notes;

import android.content.Context;
import android.content.SharedPreferences;

public final class ThemeManager {

    private static final String PREFS = "noteflow";
    private static final String KEY_THEME = "theme";

    private ThemeManager() {
    }

    public static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static Theme current(Context context) {
        return Theme.byId(prefs(context).getString(KEY_THEME, "teal"));
    }

    public static String currentId(Context context) {
        return current(context).id;
    }

    public static void set(Context context, String id) {
        prefs(context).edit().putString(KEY_THEME, Theme.byId(id).id).apply();
    }
}
