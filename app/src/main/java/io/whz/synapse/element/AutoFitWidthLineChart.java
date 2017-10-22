package io.whz.synapse.element;

import android.content.Context;
import android.util.AttributeSet;

import com.github.mikephil.charting.charts.LineChart;

public class AutoFitWidthLineChart extends LineChart {
    private static final double SCALE = 16F / 9F;

    public AutoFitWidthLineChart(Context context) {
        super(context);
    }

    public AutoFitWidthLineChart(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitWidthLineChart(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = (int) (width / SCALE);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
    }
}
