package com.oneplus.lib.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

public class AnimatorUtils {
    public static final TimeInterpolator FAST_OUT_LINEAR_IN_INTERPOLATOR = new FastOutLinearInInterpolator();
    public static final Interpolator FastOutLinearInInterpolator = new PathInterpolator(0.4f, 0.0f, 1.0f, 1.0f);
    public static final Interpolator FastOutLinearInInterpolatorSine = new PathInterpolator(0.4f, 0.0f, 0.6f, 1.0f);
    public static final Interpolator FastOutSlowInInterpolator = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);
    public static final TimeInterpolator LINEAR_OUT_SLOW_IN_INTERPOLATOR = new LinearOutSlowInInterpolator();
    public static final Interpolator LinearOutSlowInInterpolator = new PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f);
    public static String PROPNAME_HEIGHT = "space:height";
    public static String PROPNAME_SCREENLOCATION_LEFT = "location:left";
    public static String PROPNAME_SCREENLOCATION_TOP = "location:top";
    public static String PROPNAME_WIDTH = "space:width";
    public static final String VIEW_INFO_EXTRA = "VIEW_INFO_EXTRA";
    public static final float alpha1 = 0.0f;
    public static final float alpha10 = 0.9f;
    public static final float alpha11 = 1.0f;
    public static final float alpha2 = 0.08f;
    public static final float alpha3 = 0.12f;
    public static final float alpha4 = 0.18f;
    public static final float alpha5 = 0.2f;
    public static final float alpha6 = 0.24f;
    public static final float alpha7 = 0.4f;
    public static final float alpha8 = 0.48f;
    public static final float alpha9 = 0.54f;
    public static final int location_center_center = 5;
    public static final int location_center_under = 8;
    public static final int location_center_upper = 2;
    public static final int location_left_center = 4;
    public static final int location_left_under = 7;
    public static final int location_left_upper = 1;
    public static final int location_right_center = 6;
    public static final int location_right_under = 9;
    public static final int location_right_upper = 3;
    private static Bundle mEndValues = new Bundle();
    private static Bundle mStartValues = null;
    public static final float rotate_angle1 = 15.0f;
    public static final float rotate_angle2 = 30.0f;
    public static final float rotate_angle3 = 45.0f;
    public static final float rotate_angle4 = 90.0f;
    public static final float rotate_angle5 = 120.0f;
    public static final float rotate_angle6 = 180.0f;
    public static final float rotate_angle7 = 270.0f;
    public static final float rotate_angle8 = 360.0f;
    public static final int time_part1 = 30;
    public static final int time_part2 = 45;
    public static final int time_part3 = 75;
    public static final int time_part4 = 150;
    public static final int time_part5 = 225;
    public static final int time_part6 = 300;
    public static final int time_part7 = 375;
    public static final float zoom_ratio1 = 0.0f;
    public static final float zoom_ratio10 = 1.5f;
    public static final float zoom_ratio2 = 0.1f;
    public static final float zoom_ratio3 = 0.2f;
    public static final float zoom_ratio4 = 0.3f;
    public static final float zoom_ratio5 = 0.5f;
    public static final float zoom_ratio6 = 0.75f;
    public static final float zoom_ratio7 = 0.8f;
    public static final float zoom_ratio8 = 1.0f;
    public static final float zoom_ratio9 = 1.2f;

    public static void setPivotType(View animView, int type) {
        switch (type) {
            case 1:
                animView.setPivotY(0.0f);
                animView.setPivotX(0.0f);
                return;
            case 2:
                animView.setPivotY(0.0f);
                animView.setPivotX((float) (animView.getWidth() / 2));
                return;
            case 3:
                animView.setPivotY(0.0f);
                animView.setPivotX((float) animView.getWidth());
                return;
            case 4:
                animView.setPivotY((float) (animView.getHeight() / 2));
                animView.setPivotX(0.0f);
                return;
            case 5:
                animView.setPivotY((float) (animView.getHeight() / 2));
                animView.setPivotX((float) (animView.getWidth() / 2));
                return;
            case 6:
                animView.setPivotY((float) (animView.getHeight() / 2));
                animView.setPivotX((float) animView.getWidth());
                return;
            case 7:
                animView.setPivotY((float) animView.getHeight());
                animView.setPivotX(0.0f);
                return;
            case 8:
                animView.setPivotY((float) animView.getHeight());
                animView.setPivotX((float) (animView.getWidth() / 2));
                return;
            case 9:
                animView.setPivotY((float) animView.getHeight());
                animView.setPivotX((float) animView.getWidth());
                return;
            default:
                return;
        }
    }

    public static Bundle captureValues(View view) {
        Bundle b = new Bundle();
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        b.putInt(PROPNAME_SCREENLOCATION_LEFT, screenLocation[0]);
        b.putInt(PROPNAME_SCREENLOCATION_TOP, screenLocation[1]);
        b.putInt(PROPNAME_WIDTH, view.getWidth());
        b.putInt(PROPNAME_HEIGHT, view.getHeight());
        return b;
    }

    public static void onUiReady(Intent intent, final View view) {
        mStartValues = intent.getBundleExtra(VIEW_INFO_EXTRA);
        view.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            public boolean onPreDraw() {
                view.getViewTreeObserver().removeOnPreDrawListener(this);
                AnimatorUtils.prepareScene(view);
                AnimatorUtils.runEnterAnimation(view);
                return true;
            }
        });
    }

    private static void prepareScene(View view) {
        mEndValues.putInt(PROPNAME_WIDTH, view.getWidth());
        mEndValues.putInt(PROPNAME_HEIGHT, view.getHeight());
        float scaleX = scaleDelta(mStartValues, mEndValues, PROPNAME_WIDTH);
        float scaleY = scaleDelta(mStartValues, mEndValues, PROPNAME_HEIGHT);
        view.setScaleX(scaleX);
        view.setScaleY(scaleY);
        int[] screenLocation = new int[2];
        view.getLocationOnScreen(screenLocation);
        mEndValues.putInt(PROPNAME_SCREENLOCATION_LEFT, screenLocation[0]);
        mEndValues.putInt(PROPNAME_SCREENLOCATION_TOP, screenLocation[1]);
        int deltaX = translationDelta(mStartValues, mEndValues, PROPNAME_SCREENLOCATION_LEFT);
        int deltaY = translationDelta(mStartValues, mEndValues, PROPNAME_SCREENLOCATION_TOP);
        view.setTranslationX((float) deltaX);
        view.setTranslationY((float) deltaY);
    }

    private static void runEnterAnimation(View view) {
        view.animate().setDuration(225).setInterpolator(FastOutLinearInInterpolatorSine).scaleX(1.0f).scaleY(1.0f).alpha(1.0f).translationX(0.0f).translationY(0.0f).withLayer();
    }

    public static void runExitAnimation(final Activity activity, View view, View toolbar) {
        int deltaX = translationDelta(mStartValues, mEndValues, PROPNAME_SCREENLOCATION_LEFT);
        int deltaY = translationDelta(mStartValues, mEndValues, PROPNAME_SCREENLOCATION_TOP);
        float scaleX = scaleDelta(mStartValues, mEndValues, PROPNAME_WIDTH);
        float scaleY = scaleDelta(mStartValues, mEndValues, PROPNAME_HEIGHT);
        if (toolbar != null) {
            toolbar.animate().setDuration(225).setInterpolator(FastOutLinearInInterpolatorSine).alpha(0.0f).withLayer();
        }
        view.animate().setDuration(225).setInterpolator(FastOutLinearInInterpolatorSine).scaleX(scaleX).scaleY(scaleY).alpha(0.0f).translationX((float) deltaX).translationY((float) deltaY).setListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                activity.finish();
                activity.overridePendingTransition(0, 0);
            }
        }).withLayer();
    }

    private static float scaleDelta(Bundle startValues, Bundle endValues, String propertyName) {
        if (startValues == null) {
            return 0.0f;
        }
        return ((float) startValues.getInt(propertyName)) / ((float) endValues.getInt(propertyName));
    }

    private static int translationDelta(Bundle startValues, Bundle endValues, String propertyName) {
        if (startValues != null) {
            return startValues.getInt(propertyName) - endValues.getInt(propertyName);
        }
        return 0;
    }
}
