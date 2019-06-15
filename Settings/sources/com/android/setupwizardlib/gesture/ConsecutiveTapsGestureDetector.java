package com.android.setupwizardlib.gesture;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public final class ConsecutiveTapsGestureDetector {
    private final int mConsecutiveTapTimeout;
    private final int mConsecutiveTapTouchSlopSquare;
    private int mConsecutiveTapsCounter = 0;
    private final OnConsecutiveTapsListener mListener;
    private MotionEvent mPreviousTapEvent;
    private final View mView;

    public interface OnConsecutiveTapsListener {
        void onConsecutiveTaps(int i);
    }

    public ConsecutiveTapsGestureDetector(OnConsecutiveTapsListener listener, View view) {
        this.mListener = listener;
        this.mView = view;
        int doubleTapSlop = ViewConfiguration.get(this.mView.getContext()).getScaledDoubleTapSlop();
        this.mConsecutiveTapTouchSlopSquare = doubleTapSlop * doubleTapSlop;
        this.mConsecutiveTapTimeout = ViewConfiguration.getDoubleTapTimeout();
    }

    public void onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == 1) {
            Rect viewRect = new Rect();
            int[] leftTop = new int[2];
            this.mView.getLocationOnScreen(leftTop);
            viewRect.set(leftTop[0], leftTop[1], leftTop[0] + this.mView.getWidth(), leftTop[1] + this.mView.getHeight());
            if (viewRect.contains((int) ev.getX(), (int) ev.getY())) {
                if (isConsecutiveTap(ev)) {
                    this.mConsecutiveTapsCounter++;
                } else {
                    this.mConsecutiveTapsCounter = 1;
                }
                this.mListener.onConsecutiveTaps(this.mConsecutiveTapsCounter);
            } else {
                this.mConsecutiveTapsCounter = 0;
            }
            if (this.mPreviousTapEvent != null) {
                this.mPreviousTapEvent.recycle();
            }
            this.mPreviousTapEvent = MotionEvent.obtain(ev);
        }
    }

    public void resetCounter() {
        this.mConsecutiveTapsCounter = 0;
    }

    private boolean isConsecutiveTap(MotionEvent currentTapEvent) {
        boolean z = false;
        if (this.mPreviousTapEvent == null) {
            return false;
        }
        double deltaX = (double) (this.mPreviousTapEvent.getX() - currentTapEvent.getX());
        double deltaY = (double) (this.mPreviousTapEvent.getY() - currentTapEvent.getY());
        long deltaTime = currentTapEvent.getEventTime() - this.mPreviousTapEvent.getEventTime();
        if ((deltaX * deltaX) + (deltaY * deltaY) <= ((double) this.mConsecutiveTapTouchSlopSquare) && deltaTime < ((long) this.mConsecutiveTapTimeout)) {
            z = true;
        }
        return z;
    }
}
