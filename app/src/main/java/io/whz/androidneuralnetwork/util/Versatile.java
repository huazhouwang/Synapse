package io.whz.androidneuralnetwork.util;

import android.app.Activity;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;

public class Versatile {
    /**
     * Solve TransitionManager leak problem
     */
    public static void removeActivityFromTransitionManager(Activity activity) {
        final Class transitionManagerClass = TransitionManager.class;

        try {
            final Field runningTransitionsField = transitionManagerClass.getDeclaredField("sRunningTransitions");

            if (runningTransitionsField == null) {
                return;
            }

            runningTransitionsField.setAccessible(true);

            //noinspection unchecked
            final ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>> runningTransitions
                    = (ThreadLocal<WeakReference<ArrayMap<ViewGroup, ArrayList<Transition>>>>)
                    runningTransitionsField.get(transitionManagerClass);

            if (runningTransitions == null
                    || runningTransitions.get() == null
                    || runningTransitions.get().get() == null) {
                return;
            }

            final ArrayMap map = runningTransitions.get().get();
            final View decorView = activity.getWindow().getDecorView();

            if (map.containsKey(decorView)) {
                map.remove(decorView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
