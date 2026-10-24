package com.circledev.notes;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

/** Applies the selected palette to XML and dynamically created views. */
public final class Themer {

    private Themer() {
    }

    public static Theme apply(Activity activity) {
        Theme t = ThemeManager.current(activity);
        Window w = activity.getWindow();
        w.setStatusBarColor(t.bg);
        w.setNavigationBarColor(t.bg);
        View decor = w.getDecorView();
        int flags = decor.getSystemUiVisibility();
        if (t.dark) {
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags &= ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        } else {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        }
        decor.setSystemUiVisibility(flags);

        ViewGroup content = activity.findViewById(android.R.id.content);
        if (content != null) {
            content.setBackgroundColor(t.bg);
            walk(content, t, activity);
        }
        return t;
    }

    public static void walk(View view, Theme t, Context c) {
        if (view == null) return;
        style(view, t, c);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                walk(group.getChildAt(i), t, c);
            }
        }
    }

    public static void style(View v, Theme t, Context c) {
        Object raw = v.getTag();
        if (!(raw instanceof String)) return;
        String tag = (String) raw;

        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            switch (tag) {
                case "title":
                    tv.setTextColor(t.text);
                    tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
                    return;
                case "text":
                    tv.setTextColor(t.text);
                    return;
                case "sub":
                    tv.setTextColor(t.textVariant);
                    return;
                case "primary":
                    tv.setTextColor(t.primary);
                    return;
                case "onp":
                    tv.setTextColor(t.onPrimary);
                    return;
                case "error":
                    tv.setTextColor(t.error);
                    return;
                case "icon":
                    tv.setTextColor(t.textVariant);
                    return;
                case "input":
                    tv.setTextColor(t.text);
                    if (tv instanceof EditText) {
                        tv.setHintTextColor(t.textVariant);
                    }
                    return;
                default:
                    break;
            }
        }

        switch (tag) {
            case "card":
                v.setBackground(Util.round(t.surface1, 20, c));
                break;
            case "s1":
                v.setBackground(Util.round(t.surface1, 16, c));
                break;
            case "s2":
                v.setBackground(Util.round(t.surface2, 16, c));
                break;
            case "s3":
                v.setBackground(Util.round(t.surface3, 16, c));
                break;
            case "search":
                v.setBackground(Util.round(t.surface2, 100, c));
                break;
            case "chip":
                v.setBackground(Util.strokeRound(0x00000000, t.outlineVariant, 100, c));
                break;
            case "chip_act":
                v.setBackground(Util.round(t.secondaryContainer, 100, c));
                break;
            case "pill":
                v.setBackground(Util.round(t.surface2, 100, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.text);
                break;
            case "pill_act":
                v.setBackground(Util.round(t.primaryContainer, 100, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.onPrimaryContainer);
                break;
            case "fab":
                v.setBackground(Util.round(t.primaryContainer, 100, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.onPrimaryContainer);
                v.setElevation(Util.dp(c, 4));
                break;
            case "badge":
                v.setBackground(Util.round(t.secondaryContainer, 100, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.primary);
                break;
            case "avatar":
                v.setBackground(Util.round(t.secondaryContainer, 100, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.text);
                break;
            case "save":
                v.setBackground(Util.round(t.surface2, 12, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.textVariant);
                break;
            case "divider":
                v.setBackgroundColor(t.outlineVariant);
                break;
            case "handle":
                v.setBackground(Util.round(t.outlineVariant, 2, c));
                break;
            case "sheet":
                v.setBackground(Util.topRound(t.surface1, 28, c));
                break;
            case "nav":
                v.setBackgroundColor(t.bg);
                break;
            case "bubble_ai":
                v.setBackground(Util.round(t.surface2, 16, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.text);
                break;
            case "bubble_user":
                v.setBackground(Util.round(t.primary, 18, c));
                if (v instanceof TextView) ((TextView) v).setTextColor(t.onPrimary);
                break;
            default:
                break;
        }
    }

    public static void selectChip(TextView chip, boolean selected, Context c) {
        Theme t = ThemeManager.current(c);
        chip.setTag(selected ? "chip_act" : "chip");
        if (selected) {
            chip.setBackground(Util.round(t.secondaryContainer, 100, c));
            chip.setTextColor(t.text);
            chip.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        } else {
            chip.setBackground(Util.strokeRound(0x00000000, t.outlineVariant, 100, c));
            chip.setTextColor(t.textVariant);
            chip.setTypeface(Typeface.create("sans-serif", Typeface.NORMAL));
        }
    }

    public static void selectPill(TextView pill, boolean active, Context c) {
        Theme t = ThemeManager.current(c);
        if (active) {
            pill.setBackground(Util.round(t.primaryContainer, 100, c));
            pill.setTextColor(t.onPrimaryContainer);
        } else {
            pill.setBackground(Util.round(t.surface2, 100, c));
            pill.setTextColor(t.text);
        }
    }
}
