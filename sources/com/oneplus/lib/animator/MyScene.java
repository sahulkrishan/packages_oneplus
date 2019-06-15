package com.oneplus.lib.animator;

import android.animation.TimeInterpolator;
import android.view.View;
import com.oneplus.lib.util.AnimatorUtils;

public class MyScene {
    public int duration;
    public float endAlpha;
    public float endX;
    public float endY;
    public TimeInterpolator interpolator;
    public int pivotType;
    public float scaleX;
    public float scaleY;
    public View view;

    public MyScene(View animatorView, float endX, float endY, float scaleX, float scaleY, float endAlpha, int duration, TimeInterpolator interpolator) {
        this.view = animatorView;
        this.endX = endX;
        this.endY = endY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.endAlpha = endAlpha;
        this.duration = duration;
        this.interpolator = interpolator;
    }

    public MyScene setPivotType(int pivotType) {
        this.pivotType = pivotType;
        return this;
    }

    public int getDuration() {
        return this.duration;
    }

    public MyScene setDuration(int duration) {
        this.duration = duration;
        return this;
    }

    public TimeInterpolator getInterpolator() {
        return this.interpolator;
    }

    public MyScene setInterpolator(TimeInterpolator interpolator) {
        this.interpolator = interpolator;
        return this;
    }

    public float getEndX() {
        return this.endX;
    }

    public MyScene setEndX(float endX) {
        this.endX = endX;
        return this;
    }

    public float getEndY() {
        return this.endY;
    }

    public MyScene setEndY(float endY) {
        this.endY = endY;
        return this;
    }

    public float getScaleX() {
        return this.scaleX;
    }

    public MyScene setScaleX(float scaleX) {
        this.scaleX = scaleX;
        return this;
    }

    public float getScaleY() {
        return this.scaleY;
    }

    public MyScene setScaleY(float scaleY) {
        this.scaleY = scaleY;
        return this;
    }

    public float getEndAlpha() {
        return this.endAlpha;
    }

    public MyScene setEndAlpha(float endAlpha) {
        this.endAlpha = endAlpha;
        return this;
    }

    public int getPivotType() {
        return this.pivotType;
    }

    public View getView() {
        return this.view;
    }

    public MyScene setView(View view) {
        this.view = view;
        return this;
    }

    public static MyScene create(View animatorView, float endX, float endY, float scaleX, float scaleY, float endAlpha, int duration, TimeInterpolator interpolator) {
        return new MyScene(animatorView, endX, endY, scaleX, scaleY, endAlpha, duration, interpolator);
    }

    public static MyScene create(View animatorView, float endX, float endY, float scaleX, float scaleY, float endAlpha, int duration) {
        return new MyScene(animatorView, endX, endY, scaleX, scaleY, endAlpha, duration, AnimatorUtils.LinearOutSlowInInterpolator);
    }

    public static MyScene create(View animatorView, float endX, float endY, float scaleX, float scaleY, float endAlpha) {
        return new MyScene(animatorView, endX, endY, scaleX, scaleY, endAlpha, AnimatorUtils.time_part7, AnimatorUtils.LinearOutSlowInInterpolator);
    }
}
