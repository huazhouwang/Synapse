package io.whz.androidneuralnetwork.element;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class VerticalGap extends RecyclerView.ItemDecoration {
    private final int mSpace;

    public VerticalGap(int space) {
        mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (parent.getChildAdapterPosition(view) != 0) {
            outRect.top = mSpace;
        }
    }
}
