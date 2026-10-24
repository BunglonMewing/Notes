package com.circledev.notes;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class Util {

    public static final Locale ID = new Locale("id", "ID");

    private Util() {
    }

    public static int dp(Context c, float value) {
        return Math.round(value * c.getResources().getDisplayMetrics().density);
    }

    public static String today() {
        return new SimpleDateFormat("d MMM yyyy", ID).format(new Date());
    }

    public static String time() {
        return new SimpleDateFormat("HH:mm", ID).format(new Date());
    }

    public static String fmtRemind(long millis) {
        if (millis <= 0) return "";
        return new SimpleDateFormat("d MMM yyyy HH:mm", ID).format(new Date(millis));
    }

    public static String fmtDateLong() {
        return new SimpleDateFormat("d MMMM yyyy", ID).format(new Date());
    }

    public static int wordCount(String text) {
        if (text == null) return 0;
        String trimmed = text.trim();
        if (trimmed.isEmpty()) return 0;
        return trimmed.split("\\s+").length;
    }

    public static GradientDrawable round(int fill, float radiusDp, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(fill);
        d.setCornerRadius(dp(c, radiusDp));
        return d;
    }

    public static GradientDrawable topRound(int fill, float radiusDp, Context c) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.RECTANGLE);
        d.setColor(fill);
        float r = dp(c, radiusDp);
        d.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return d;
    }

    public static GradientDrawable strokeRound(int fill, int stroke, float radiusDp, Context c) {
        GradientDrawable d = round(fill, radiusDp, c);
        d.setStroke(dp(c, 1.4f), stroke);
        return d;
    }

    public static GradientDrawable circle(int fill) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(fill);
        return d;
    }

    public static ViewGroup content(Activity a) {
        return a.findViewById(android.R.id.content);
    }

    public static void snack(Activity activity, String message) {
        try {
            ViewGroup content = content(activity);
            if (content == null) return;
            Theme t = ThemeManager.current(activity);
            TextView tv = new TextView(activity);
            tv.setText(message);
            tv.setTextSize(13);
            tv.setTextColor(t.bg);
            tv.setPadding(dp(activity, 18), dp(activity, 11), dp(activity, 18), dp(activity, 11));
            tv.setBackground(round(t.text, 12, activity));
            tv.setElevation(dp(activity, 6));
            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            lp.bottomMargin = dp(activity, 88);
            lp.leftMargin = dp(activity, 16);
            lp.rightMargin = dp(activity, 16);
            tv.setLayoutParams(lp);
            tv.setAlpha(0f);
            content.addView(tv);
            tv.animate().alpha(1f).setDuration(160).start();
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                tv.animate().alpha(0f).setDuration(200).withEndAction(() -> {
                    ViewParentHelper.remove(tv);
                }).start();
            }, 2400);
        } catch (Exception ignored) {
        }
    }

    private static final class ViewParentHelper {
        static void remove(View v) {
            try {
                if (v.getParent() instanceof ViewGroup) {
                    ((ViewGroup) v.getParent()).removeView(v);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public static void copy(Activity a, String text) {
        ClipboardManager cm = (ClipboardManager) a.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("NoteFlow", text));
    }

    public static void share(Activity a, String subject, String text) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.putExtra(Intent.EXTRA_TEXT, text);
        a.startActivity(Intent.createChooser(send, "Bagikan catatan"));
    }

    public static void hideKeyboard(Activity a) {
        View view = a.getCurrentFocus();
        if (view == null) return;
        InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void confirm(Activity a, String title, String message, String positive,
                               Runnable onConfirm) {
        Theme t = ThemeManager.current(a);
        Dialog d = new Dialog(a, R.style.DialogTheme);
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(a, 22), dp(a, 20), dp(a, 22), dp(a, 14));
        root.setBackground(round(t.surface2, 24, a));

        TextView tvTitle = new TextView(a);
        tvTitle.setText(title);
        tvTitle.setTextColor(t.text);
        tvTitle.setTextSize(18);
        tvTitle.setTypeface(android.graphics.Typeface.create("sans-serif-medium",
                android.graphics.Typeface.NORMAL));
        root.addView(tvTitle);

        TextView tvMsg = new TextView(a);
        tvMsg.setText(message);
        tvMsg.setTextColor(t.textVariant);
        tvMsg.setTextSize(13);
        tvMsg.setLineSpacing(dp(a, 2), 1f);
        LinearLayout.LayoutParams msgLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msgLp.topMargin = dp(a, 6);
        root.addView(tvMsg, msgLp);

        LinearLayout row = new LinearLayout(a);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.END);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(a, 16);

        TextView cancel = new TextView(a);
        cancel.setText("Batal");
        cancel.setTextColor(t.primary);
        cancel.setTextSize(13);
        cancel.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
        cancel.setOnClickListener(v -> d.dismiss());

        TextView ok = new TextView(a);
        ok.setText(positive);
        ok.setTextColor(t.error);
        ok.setTextSize(13);
        ok.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
        ok.setOnClickListener(v -> {
            d.dismiss();
            if (onConfirm != null) onConfirm.run();
        });

        row.addView(cancel);
        row.addView(ok);
        root.addView(row, rowLp);

        d.setContentView(root);
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            w.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = Math.min(dp(a, 320), a.getResources().getDisplayMetrics().widthPixels - dp(a, 40));
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            w.setAttributes(lp);
        }
        d.show();
    }

    public static void prompt(Activity a, String title, String hint, String initial,
                              OnText onText) {
        Theme t = ThemeManager.current(a);
        Dialog d = new Dialog(a, R.style.DialogTheme);
        LinearLayout root = new LinearLayout(a);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(a, 22), dp(a, 20), dp(a, 22), dp(a, 14));
        root.setBackground(round(t.surface2, 24, a));

        TextView tvTitle = new TextView(a);
        tvTitle.setText(title);
        tvTitle.setTextColor(t.text);
        tvTitle.setTextSize(17);
        root.addView(tvTitle);

        EditText input = new EditText(a);
        input.setHint(hint);
        input.setText(initial == null ? "" : initial);
        input.setHintTextColor(t.textVariant);
        input.setTextColor(t.text);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        input.setBackground(round(t.surface3, 10, a));
        input.setPadding(dp(a, 12), dp(a, 9), dp(a, 12), dp(a, 9));
        LinearLayout.LayoutParams inputLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        inputLp.topMargin = dp(a, 12);
        root.addView(input, inputLp);

        LinearLayout row = new LinearLayout(a);
        row.setGravity(Gravity.END);
        LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rowLp.topMargin = dp(a, 12);

        TextView cancel = new TextView(a);
        cancel.setText("Batal");
        cancel.setTextColor(t.textVariant);
        cancel.setTextSize(13);
        cancel.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
        cancel.setOnClickListener(v -> d.dismiss());

        TextView ok = new TextView(a);
        ok.setText("Terapkan");
        ok.setTextColor(t.primary);
        ok.setTextSize(13);
        ok.setPadding(dp(a, 12), dp(a, 8), dp(a, 12), dp(a, 8));
        ok.setOnClickListener(v -> {
            d.dismiss();
            if (onText != null) onText.onText(input.getText().toString().trim());
        });

        row.addView(cancel);
        row.addView(ok);
        root.addView(row, rowLp);

        d.setContentView(root);
        Window w = d.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(w.getAttributes());
            lp.width = Math.min(dp(a, 330), a.getResources().getDisplayMetrics().widthPixels - dp(a, 40));
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.gravity = Gravity.CENTER;
            w.setAttributes(lp);
        }
        d.show();
        input.requestFocus();
        input.postDelayed(() -> {
            InputMethodManager imm = (InputMethodManager) a.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
        }, 150);
    }

    public interface OnText {
        void onText(String value);
    }

    public static boolean hasNotificationPermission(Context c) {
        if (Build.VERSION.SDK_INT < 33) return true;
        return c.checkSelfPermission("android.permission.POST_NOTIFICATIONS")
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestNotificationPermission(Activity a) {
        if (Build.VERSION.SDK_INT >= 33 && !hasNotificationPermission(a)) {
            a.requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 9101);
        }
    }
}
