package com.circledev.notes;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

public class FocusActivity extends Activity {

    private RingView ring;
    private TextView timeView;
    private TextView labelView;
    private TextView startButton;

    private Theme theme;
    private int totalSeconds = 25 * 60;
    private int left = 25 * 60;
    private boolean running;
    private boolean finished;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            if (!running) return;
            left--;
            if (left <= 0) {
                left = 0;
                running = false;
                finished = true;
                startButton.setText("▶ Mulai");
                updateUi();
                notifyFinished();
                Util.snack(FocusActivity.this, "⏰ Pomodoro selesai! Istirahat 5 menit.");
                return;
            }
            updateUi();
            handler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);
        theme = Themer.apply(this);

        ring = findViewById(R.id.ring);
        timeView = findViewById(R.id.focusTime);
        labelView = findViewById(R.id.focusLabel);
        startButton = findViewById(R.id.btnStart);

        ring.setColors(theme.primary, theme.outlineVariant);

        findViewById(R.id.btnClose).setOnClickListener(v -> finish());
        findViewById(R.id.btnReset).setOnClickListener(v -> reset());
        startButton.setOnClickListener(v -> toggle());
        findViewById(R.id.btn25).setOnClickListener(v -> setDuration(25));
        findViewById(R.id.btn15).setOnClickListener(v -> setDuration(15));
        findViewById(R.id.btn5).setOnClickListener(v -> setDuration(5));

        updateUi();
    }

    private void setDuration(int minutes) {
        handler.removeCallbacks(tick);
        running = false;
        finished = false;
        totalSeconds = minutes * 60;
        left = totalSeconds;
        startButton.setText("▶ Mulai");
        updateUi();
    }

    private void toggle() {
        if (running) {
            running = false;
            handler.removeCallbacks(tick);
            startButton.setText("▶ Lanjut");
            updateUi();
            return;
        }
        if (left <= 0) left = totalSeconds;
        finished = false;
        running = true;
        startButton.setText("⏸ Pause");
        Util.requestNotificationPermission(this);
        handler.postDelayed(tick, 1000);
        updateUi();
    }

    private void reset() {
        handler.removeCallbacks(tick);
        running = false;
        finished = false;
        left = totalSeconds;
        startButton.setText("▶ Mulai");
        updateUi();
    }

    private void updateUi() {
        int minutes = left / 60;
        int seconds = left % 60;
        timeView.setText(String.format(Locale.US, "%02d:%02d", minutes, seconds));
        ring.setProgress(totalSeconds == 0 ? 0 : left / (float) totalSeconds);

        if (finished) {
            labelView.setText("🎉 Selesai! Istirahat dulu.");
        } else {
            labelView.setText("Pomodoro " + (totalSeconds / 60) + " menit — "
                    + (running ? "Berjalan ⏱" : "Siap"));
        }
    }

    private void notifyFinished() {
        if (!Util.hasNotificationPermission(this)) return;
        Intent open = new Intent(this, MainActivity.class);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pending = PendingIntent.getActivity(this, 20, open,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, App.CHANNEL_FOCUS);
        } else {
            builder = new Notification.Builder(this);
            builder.setPriority(Notification.PRIORITY_DEFAULT);
        }
        builder.setSmallIcon(R.drawable.ic_notify)
                .setContentTitle("Mode Fokus selesai 🎯")
                .setContentText("Istirahat 5 menit, lalu lanjut lagi.")
                .setAutoCancel(true)
                .setContentIntent(pending);

        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) nm.notify(2025, builder.build());
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(tick);
        super.onDestroy();
    }
}
