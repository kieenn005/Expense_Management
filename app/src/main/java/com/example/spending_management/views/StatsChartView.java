package com.example.spending_management.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.spending_management.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class StatsChartView extends View {
    public static class ChartEntry {
        public final String label;
        public final double value;
        public final int color;

        public ChartEntry(String label, double value, int color) {
            this.label = label;
            this.value = value;
            this.color = color;
        }
    }

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final ArrayList<ChartEntry> entries = new ArrayList<>();
    private int mode = 0;
    private double total = 0;

    public StatsChartView(Context context) {
        super(context);
        init();
    }

    public StatsChartView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        textPaint.setColor(getResources().getColor(R.color.textPrimary));
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setChartData(List<ChartEntry> newEntries, double newTotal, int newMode) {
        entries.clear();
        entries.addAll(newEntries);
        total = newTotal;
        mode = newMode;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (entries.isEmpty() || total <= 0) {
            drawEmpty(canvas);
            return;
        }

        if (mode == 2) {
            drawLine(canvas);
        } else {
            drawDonut(canvas);
        }
    }

    private void drawEmpty(Canvas canvas) {
        textPaint.setTextSize(sp(16));
        textPaint.setFakeBoldText(false);
        canvas.drawText("Chưa có dữ liệu", getWidth() / 2f, getHeight() / 2f, textPaint);
    }

    private void drawDonut(Canvas canvas) {
        float chartSize = Math.min(getWidth() * 0.44f, getHeight() * 0.62f);
        float left = dp(16);
        float top = dp(30);
        RectF rect = new RectF(left, top, left + chartSize, top + chartSize);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.BUTT);
        paint.setStrokeWidth(chartSize * 0.22f);

        float start = -90f;
        for (ChartEntry entry : entries) {
            float sweep = (float) (entry.value / total * 360f);
            paint.setColor(entry.color);
            canvas.drawArc(rect, start, sweep, false, paint);
            start += sweep;
        }

        textPaint.setColor(getResources().getColor(R.color.textPrimary));
        textPaint.setTextSize(sp(18));
        textPaint.setFakeBoldText(true);
        canvas.drawText(formatMoney(total), rect.centerX(), rect.centerY() + dp(6), textPaint);

        float legendX = getWidth() * 0.56f;
        float legendY = top + dp(34);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(sp(15));
        DecimalFormat percentFormat = new DecimalFormat("0.##");
        for (int i = 0; i < Math.min(entries.size(), 4); i++) {
            ChartEntry entry = entries.get(i);
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(entry.color);
            canvas.drawCircle(legendX, legendY + i * dp(42), dp(7), paint);

            textPaint.setColor(getResources().getColor(R.color.textPrimary));
            canvas.drawText(entry.label, legendX + dp(18), legendY + dp(5) + i * dp(42), textPaint);
            textPaint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText(percentFormat.format(entry.value / total * 100) + "%", getWidth() - dp(12), legendY + dp(5) + i * dp(42), textPaint);
            textPaint.setTextAlign(Paint.Align.LEFT);
        }
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    private void drawLine(Canvas canvas) {
        float left = dp(42);
        float right = getWidth() - dp(16);
        float top = dp(78);
        float bottom = getHeight() - dp(58);
        double max = 0;
        for (ChartEntry entry : entries) {
            max = Math.max(max, entry.value);
        }
        if (max <= 0) max = 1;

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dp(1));
        paint.setColor(getResources().getColor(R.color.surfaceMuted));
        canvas.drawLine(left, bottom, right, bottom, paint);
        canvas.drawLine(left, top, left, bottom, paint);

        paint.setStrokeWidth(dp(3));
        paint.setColor(getResources().getColor(R.color.primaryBlueDark));
        float previousX = 0;
        float previousY = 0;
        for (int i = 0; i < entries.size(); i++) {
            float x = entries.size() == 1 ? left : left + (right - left) * i / (entries.size() - 1);
            float y = bottom - (float) (entries.get(i).value / max * (bottom - top));
            if (i > 0) {
                canvas.drawLine(previousX, previousY, x, y, paint);
            }
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(x, y, dp(5), paint);
            paint.setStyle(Paint.Style.STROKE);
            previousX = x;
            previousY = y;
        }

        textPaint.setColor(getResources().getColor(R.color.textPrimary));
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setFakeBoldText(false);
        textPaint.setTextSize(sp(14));
        canvas.drawText("Tổng cộng: " + formatMoney(total), dp(14), dp(24), textPaint);
        canvas.drawText("Trung bình: " + formatMoney(total / Math.max(nonZeroCount(), 1)), dp(14), dp(48), textPaint);

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(sp(12));
        textPaint.setColor(getResources().getColor(R.color.textSecondary));
        int labelStep = Math.max(1, entries.size() / 4);
        for (int i = 0; i < entries.size(); i += labelStep) {
            float x = entries.size() == 1 ? left : left + (right - left) * i / (entries.size() - 1);
            canvas.drawText(entries.get(i).label, x, getHeight() - dp(18), textPaint);
        }
    }

    private String formatMoney(double value) {
        DecimalFormat format = new DecimalFormat("#,###");
        return format.format(value).replace(",", ".");
    }

    private int nonZeroCount() {
        int count = 0;
        for (ChartEntry entry : entries) {
            if (entry.value > 0) count++;
        }
        return count;
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(int value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
