package com.circledev.notes;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class RingView extends View {

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF rect = new RectF();
    private float progress = 1f;
    private int color = 0xFF006A6A;
    private int trackColor = 0xFFBEC9C9;

    public RingView(Context context) {
        super(context);
    }

    public RingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setColors(int color, int trackColor) {
        this.color = color;
        this.trackColor = trackColor;
        invalidate();
    }

    public void setProgress(float progress) {
        this.progress = Math.max(0f, Math.min(1f, progress));
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float stroke = Util.dp(getContext(), 12);
        float padding = stroke / 2f + Util.dp(getContext(), 4);
        float size = Math.min(getWidth(), getHeight()) - padding * 2;
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        rect.set(cx - size / 2f, cy - size / 2f, cx + size / 2f, cy + size / 2f);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(stroke);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor(trackColor);
        canvas.drawCircle(cx, cy, size / 2f, paint);

        if (progress > 0.001f) {
            paint.setColor(color);
            canvas.drawArc(rect, -90f, 360f * progress, false, paint);
        }
    }
}
