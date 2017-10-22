package io.whz.synapse.track;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;

import static io.whz.synapse.pojo.constant.TrackCons.Lifecycle.ENTER;
import static io.whz.synapse.pojo.constant.TrackCons.Lifecycle.LEAVE;
import static io.whz.synapse.pojo.constant.TrackCons.concat;

class ActivityLifecycleTracker implements Application.ActivityLifecycleCallbacks {
    private final Tracker mTrack;

    ActivityLifecycleTracker(@NonNull Tracker track) {
        mTrack = track;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityStarted(Activity activity) {
        mTrack.logEvent(concat(ENTER, activity.getClass().getSimpleName().toLowerCase()));
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {
    }

    @Override
    public void onActivityStopped(Activity activity) {
        mTrack.logEvent(concat(LEAVE, activity.getClass().getSimpleName().toLowerCase()));
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
