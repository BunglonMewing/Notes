package com.circledev.notes;

import android.app.Activity;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Ui {

    private Ui() {
    }

    public static LinearLayout row(Activity a) {
        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return row;
    }

    public static LinearLayout column(Activity a) {
        LinearLayout col = new LinearLayout(a);
        col.setOrientation(LinearLayout.VERTICAL);
        return col;
    }

    public static TextView text(Activity a, String value, float sizeSp, int color) {
        TextView tv = new TextView(a);
        tv.setText(value);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        return tv;
    }

    public static TextView chip(Activity a, String label) {
        Theme t = ThemeManager.current(a);
        TextView tv = new TextView(a);
        tv.setText(label);
        tv.setTextSize(13);
        tv.setTextColor(t.textVariant);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(Util.dp(a, 13), Util.dp(a, 6), Util.dp(a, 13), Util.dp(a, 6));
        tv.setBackground(Util.strokeRound(0x00000000, t.outlineVariant, 100, a));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.rightMargin = Util.dp(a, 7);
        tv.setLayoutParams(lp);
        tv.setClickable(true);
        tv.setFocusable(true);
        return tv;
    }

    public static View colorDot(Activity a, int fill, boolean selected) {
        Theme t = ThemeManager.current(a);
        View v = new View(a);
        int size = Util.dp(a, 23);
        android.graphics.drawable.GradientDrawable bg = Util.circle(fill);
        bg.setStroke(Util.dp(a, selected ? 2.2f : 1.2f), selected ? t.text : t.outlineVariant);
        v.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
        lp.rightMargin = Util.dp(a, 7);
        v.setLayoutParams(lp);
        return v;
    }

    public static TextView actionPill(Activity a, String label) {
        Theme t = ThemeManager.current(a);
        TextView tv = new TextView(a);
        tv.setText(label);
        tv.setTextSize(13);
        tv.setTextColor(t.text);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(Util.dp(a, 16), Util.dp(a, 10), Util.dp(a, 16), Util.dp(a, 10));
        tv.setBackground(Util.round(t.surface2, 100, a));
        tv.setClickable(true);
        tv.setFocusable(true);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Util.dp(a, 8);
        tv.setLayoutParams(lp);
        return tv;
    }

    public static TextView sectionLabel(Activity a, String label) {
        Theme t = ThemeManager.current(a);
        TextView tv = text(a, label, 11, t.textVariant);
        tv.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        tv.setLetterSpacing(0.08f);
        tv.setAllCaps(true);
        tv.setPadding(Util.dp(a, 4), Util.dp(a, 12), Util.dp(a, 4), Util.dp(a, 4));
        return tv;
    }

    public static void addRow(LinearLayout parent, View... children) {
        LinearLayout row = new LinearLayout(parent.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        for (View child : children) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            lp.setMargins(Util.dp(parent.getContext(), 4), Util.dp(parent.getContext(), 4),
                    Util.dp(parent.getContext(), 4), Util.dp(parent.getContext(), 4));
            row.addView(child, lp);
        }
        parent.addView(row);
    }

    public static LinearLayout.LayoutParams fullWidth() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}
