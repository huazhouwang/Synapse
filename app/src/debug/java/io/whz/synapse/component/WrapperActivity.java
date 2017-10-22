package io.whz.synapse.component;

import android.graphics.drawable.Drawable;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.jakewharton.scalpel.ScalpelFrameLayout;

import io.whz.synapse.R;

public class WrapperActivity extends BaseActivity {
    private Drawable mDrawableBackUp;
    private boolean mScalpelEnable = false;
    private MenuItem mScalpelMenu;
    private ScalpelFrameLayout mScalpelLayout;

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        mScalpelLayout = new ScalpelFrameLayout(this);

        mScalpelLayout.setLayerInteractionEnabled(false);
        mScalpelLayout.setDrawViews(true);
        mScalpelLayout.setDrawIds(true);
        mScalpelLayout.setChromeColor(ContextCompat.getColor(this, R.color.white$1));
        mScalpelLayout.setChromeShadowColor(ContextCompat.getColor(this, R.color.red$1));

        LayoutInflater.from(this).inflate(layoutResID, mScalpelLayout, true);

        final ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        super.setContentView(mScalpelLayout, lp);
    }

    @CallSuper
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mScalpelMenu = menu.add(Menu.NONE, R.id.scalpel_menu, Menu.NONE, "Enable Scalpel");

        return true;
    }

    @CallSuper
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.scalpel_menu) {

            mScalpelEnable = !mScalpelEnable;

            if (mScalpelEnable) {
                mDrawableBackUp = getWindow().getDecorView().getBackground();
                getWindow().getDecorView().setBackgroundResource(R.color.black$2);

                mScalpelLayout.setLayerInteractionEnabled(true);
                mScalpelMenu.setTitle("Disable Scalpel");
            } else {
                getWindow().getDecorView().setBackground(mDrawableBackUp);

                mScalpelLayout.setLayerInteractionEnabled(false);
                mScalpelMenu.setTitle("Enable Scalpel");
            }

            Toast.makeText(this, "Submit change", Toast.LENGTH_SHORT)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }
}
