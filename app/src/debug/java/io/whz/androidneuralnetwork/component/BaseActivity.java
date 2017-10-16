package io.whz.androidneuralnetwork.component;

import android.app.Activity;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.jakewharton.scalpel.ScalpelFrameLayout;

import java.util.HashSet;
import java.util.Set;

import io.whz.androidneuralnetwork.R;

public class BaseActivity extends AppCompatActivity {
    private static final Set<Class<? extends Activity>> sDebugging;

    static {
        sDebugging = new HashSet<>();

//        sDebugging.add(PlayActivity.class);
    }

    private final boolean mIsDebugging = sDebugging.contains(getClass());

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        if (mIsDebugging) {
            final ScalpelFrameLayout layout = new ScalpelFrameLayout(this);

            layout.setLayerInteractionEnabled(true);
            layout.setDrawViews(true);
            layout.setDrawIds(true);
            layout.setChromeColor(R.color.black$1);
            layout.setChromeShadowColor(R.color.white$1);

            LayoutInflater.from(this).inflate(layoutResID, layout, true);

            final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);

            getWindow().setBackgroundDrawableResource(R.color.black$1);

            super.setContentView(layout, lp);
        } else {
            super.setContentView(layoutResID);
        }
    }
}
