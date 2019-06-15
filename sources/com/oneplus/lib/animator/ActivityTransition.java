package com.oneplus.lib.animator;

import android.animation.Animator.AnimatorListener;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import com.oneplus.lib.util.AnimatorUtils;
import java.util.ArrayList;
import java.util.Iterator;

public class ActivityTransition {
    public static final String ACTIVITY_TRANSITION_OPTIONS = "activity_transition_options";
    public static final long DEFAULT_TRANSITION_DURATION = 800;

    public static void startActivity(Intent intent, ActivityTransitionOptions options) {
        options.captureViewAttrs();
        intent.putParcelableArrayListExtra(ACTIVITY_TRANSITION_OPTIONS, options.getShareElementViewAttrs());
        Activity activity = options.getActivity();
        activity.startActivity(intent);
        activity.overridePendingTransition(0, 0);
    }

    public static void startActivityForResult(Intent intent, int requestCode, ActivityTransitionOptions options) {
        options.captureViewAttrs();
        intent.putParcelableArrayListExtra(ACTIVITY_TRANSITION_OPTIONS, options.getShareElementViewAttrs());
        Activity activity = options.getActivity();
        activity.startActivityForResult(intent, requestCode);
        activity.overridePendingTransition(0, 0);
    }

    public static void doEnterTransition(Activity activity, long duration, TimeInterpolator interpolator, AnimatorListener listener, ArrayList<MyScene> myScenes) {
        doEnterTransitionInner(activity, activity.getIntent().getParcelableArrayListExtra(ACTIVITY_TRANSITION_OPTIONS), duration, interpolator, listener, myScenes);
    }

    public static void doEnterTransition(Activity activity, long duration, AnimatorListener listener) {
        doEnterTransition(activity, duration, null, listener, null);
    }

    public static void doEnterTransition(Activity activity, TimeInterpolator interpolator, AnimatorListener listener) {
        doEnterTransition(activity, 800, interpolator, listener, null);
    }

    public static void doEnterTransition(Activity activity, AnimatorListener listener) {
        doEnterTransition(activity, 800, null, listener, null);
    }

    public static void doEnterTransition(Activity activity) {
        doEnterTransition(activity, 800, null, null, null);
    }

    public static void doExitTransition(Activity activity, long duration, TimeInterpolator interpolator, AnimatorListener listener, ArrayList<MyScene> myScenes) {
        doExitTransitionInner(activity, activity.getIntent().getParcelableArrayListExtra(ACTIVITY_TRANSITION_OPTIONS), duration, interpolator, listener, myScenes);
    }

    public static void doExitTransition(Activity activity, TimeInterpolator interpolator) {
        doExitTransition(activity, 800, interpolator, null, null);
    }

    public static void doExitTransition(Activity activity, long duration) {
        doExitTransition(activity, duration, null, null, null);
    }

    public static void doExitTransition(Activity activity) {
        doExitTransition(activity, 800, null, null, null);
    }

    private static void doExitTransitionInner(final Activity activity, ArrayList<ShareElementViewAttrs> attrs, long duration, TimeInterpolator interpolator, AnimatorListener listener, ArrayList<MyScene> myScenes) {
        if (attrs != null && attrs.size() != 0) {
            doMyViewAnimator(myScenes);
            Iterator it = attrs.iterator();
            while (it.hasNext()) {
                ShareElementViewAttrs attr = (ShareElementViewAttrs) it.next();
                View view = activity.findViewById(attr.id);
                int[] location = new int[2];
                view.getLocationOnScreen(location);
                view.setPivotX(0.0f);
                view.setPivotY(0.0f);
                view.animate().scaleX(attr.width / ((float) view.getWidth())).scaleY(attr.height / ((float) view.getHeight())).translationX(attr.startX - ((float) location[0])).translationY(attr.startY - ((float) location[1])).setInterpolator(interpolator).setDuration(duration).setListener(listener).withLayer();
            }
            activity.findViewById(((ShareElementViewAttrs) attrs.get(0)).id).postDelayed(new Runnable() {
                public void run() {
                    activity.finish();
                    activity.overridePendingTransition(0, 0);
                }
            }, duration);
        }
    }

    private static void doEnterTransitionInner(Activity activity, ArrayList<ShareElementViewAttrs> attrs, long duration, TimeInterpolator interpolator, AnimatorListener listener, ArrayList<MyScene> myScenes) {
        Activity activity2;
        if (attrs == null || attrs.size() == 0) {
            activity2 = activity;
            return;
        }
        doMyViewAnimator(myScenes);
        Iterator it = attrs.iterator();
        while (it.hasNext()) {
            ShareElementViewAttrs attr = (ShareElementViewAttrs) it.next();
            View view = activity.findViewById(attr.id);
            if (view != null) {
                final View view2 = view;
                final ShareElementViewAttrs shareElementViewAttrs = attr;
                final long j = duration;
                final TimeInterpolator timeInterpolator = interpolator;
                final AnimatorListener animatorListener = listener;
                view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        view2.getViewTreeObserver().removeOnPreDrawListener(this);
                        int[] location = new int[2];
                        view2.getLocationOnScreen(location);
                        view2.setPivotX(0.0f);
                        view2.setPivotY(0.0f);
                        view2.setScaleX(shareElementViewAttrs.width / ((float) view2.getWidth()));
                        view2.setScaleY(shareElementViewAttrs.height / ((float) view2.getHeight()));
                        view2.setTranslationX(shareElementViewAttrs.startX - ((float) location[0]));
                        view2.setTranslationY(shareElementViewAttrs.startY - ((float) location[1]));
                        view2.animate().scaleX(1.0f).scaleY(1.0f).translationX(0.0f).translationY(0.0f).setDuration(j).setInterpolator(timeInterpolator).setListener(animatorListener).withLayer();
                        return true;
                    }
                });
            }
        }
        activity2 = activity;
    }

    private static void doMyViewAnimator(ArrayList<MyScene> myScenes) {
        if (myScenes != null && myScenes.size() > 0) {
            Iterator it = myScenes.iterator();
            while (it.hasNext()) {
                final MyScene scene = (MyScene) it.next();
                final View view = scene.view;
                view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
                    public boolean onPreDraw() {
                        view.getViewTreeObserver().removeOnPreDrawListener(this);
                        AnimatorUtils.setPivotType(view, scene.pivotType);
                        view.animate().alpha(scene.endAlpha).translationY(scene.endY).translationX(scene.endX).scaleX(scene.scaleX).scaleY(scene.scaleY).setDuration((long) scene.duration).setInterpolator(scene.interpolator).withLayer();
                        return true;
                    }
                });
            }
        }
    }
}
