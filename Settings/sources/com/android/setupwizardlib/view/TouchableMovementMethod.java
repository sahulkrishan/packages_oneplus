package com.android.setupwizardlib.view;

import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.widget.TextView;

public interface TouchableMovementMethod {

    public static class TouchableLinkMovementMethod extends LinkMovementMethod implements TouchableMovementMethod {
        MotionEvent mLastEvent;
        boolean mLastEventResult = false;

        public static TouchableLinkMovementMethod getInstance() {
            return new TouchableLinkMovementMethod();
        }

        public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
            this.mLastEvent = event;
            boolean result = super.onTouchEvent(widget, buffer, event);
            if (event.getAction() == 0) {
                this.mLastEventResult = Selection.getSelectionStart(buffer) != -1;
            } else {
                this.mLastEventResult = result;
            }
            return result;
        }

        public MotionEvent getLastTouchEvent() {
            return this.mLastEvent;
        }

        public boolean isLastTouchEventHandled() {
            return this.mLastEventResult;
        }
    }

    MotionEvent getLastTouchEvent();

    boolean isLastTouchEventHandled();
}
