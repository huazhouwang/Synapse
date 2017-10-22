package io.whz.synapse.element;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

public class AutoFitWidthCardView extends CardView {
    private static final double SCALE = 16F / 9F;

    public AutoFitWidthCardView(Context context) {
        super(context);
    }

    public AutoFitWidthCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AutoFitWidthCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
