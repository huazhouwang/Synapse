package io.whz.synapse.component;

import android.support.v7.app.AppCompatActivity;

import io.whz.synapse.util.Versatile;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onDestroy() {
        super.onDestroy();

        Versatile.removeActivityFromTransitionManager(this);
    }
}
