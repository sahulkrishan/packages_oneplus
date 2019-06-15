package com.android.settings.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.widget.ExploreByTouchHelper;
import android.support.v7.preference.OPUtils;
import android.support.v7.preference.VibratorSceneUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import java.util.List;

public class LabeledSeekBar extends SeekBar {
    private final ExploreByTouchHelper mAccessHelper;
    private String[] mLabels;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    private final OnSeekBarChangeListener mProxySeekBarListener;
    private long[] mVibratePattern;
    private Vibrator mVibrator;

    private class LabeledSeekBarExploreByTouchHelper extends ExploreByTouchHelper {
        private boolean mIsLayoutRtl;

        public LabeledSeekBarExploreByTouchHelper(LabeledSeekBar forView) {
            super(forView);
            boolean z = true;
            if (forView.getResources().getConfiguration().getLayoutDirection() != 1) {
                z = false;
            }
            this.mIsLayoutRtl = z;
        }

        /* Access modifiers changed, original: protected */
        public int getVirtualViewAt(float x, float y) {
            return getVirtualViewIdIndexFromX(x);
        }

        /* Access modifiers changed, original: protected */
        public void getVisibleVirtualViews(List<Integer> list) {
            int c = LabeledSeekBar.this.getMax();
            for (int i = 0; i <= c; i++) {
                list.add(Integer.valueOf(i));
            }
        }

        /* Access modifiers changed, original: protected */
        public boolean onPerformActionForVirtualView(int virtualViewId, int action, Bundle arguments) {
            if (virtualViewId == -1 || action != 16) {
                return false;
            }
            LabeledSeekBar.this.setProgress(virtualViewId);
            sendEventForVirtualView(virtualViewId, 1);
            return true;
        }

        /* Access modifiers changed, original: protected */
        public void onPopulateNodeForVirtualView(int virtualViewId, AccessibilityNodeInfoCompat node) {
            node.setClassName(RadioButton.class.getName());
            node.setBoundsInParent(getBoundsInParentFromVirtualViewId(virtualViewId));
            node.addAction(16);
            node.setContentDescription(LabeledSeekBar.this.mLabels[virtualViewId]);
            boolean z = true;
            node.setClickable(true);
            node.setCheckable(true);
            if (virtualViewId != LabeledSeekBar.this.getProgress()) {
                z = false;
            }
            node.setChecked(z);
        }

        /* Access modifiers changed, original: protected */
        public void onPopulateEventForVirtualView(int virtualViewId, AccessibilityEvent event) {
            event.setClassName(RadioButton.class.getName());
            event.setContentDescription(LabeledSeekBar.this.mLabels[virtualViewId]);
            event.setChecked(virtualViewId == LabeledSeekBar.this.getProgress());
        }

        /* Access modifiers changed, original: protected */
        public void onPopulateNodeForHost(AccessibilityNodeInfoCompat node) {
            node.setClassName(RadioGroup.class.getName());
        }

        /* Access modifiers changed, original: protected */
        public void onPopulateEventForHost(AccessibilityEvent event) {
            event.setClassName(RadioGroup.class.getName());
        }

        private int getHalfVirtualViewWidth() {
            return Math.max(0, ((LabeledSeekBar.this.getWidth() - LabeledSeekBar.this.getPaddingStart()) - LabeledSeekBar.this.getPaddingEnd()) / (LabeledSeekBar.this.getMax() * 2));
        }

        private int getVirtualViewIdIndexFromX(float x) {
            int posBase = Math.min((Math.max(0, (((int) x) - LabeledSeekBar.this.getPaddingStart()) / getHalfVirtualViewWidth()) + 1) / 2, LabeledSeekBar.this.getMax());
            return this.mIsLayoutRtl ? LabeledSeekBar.this.getMax() - posBase : posBase;
        }

        private Rect getBoundsInParentFromVirtualViewId(int virtualViewId) {
            int updatedVirtualViewId = this.mIsLayoutRtl ? LabeledSeekBar.this.getMax() - virtualViewId : virtualViewId;
            int right = (((updatedVirtualViewId * 2) + 1) * getHalfVirtualViewWidth()) + LabeledSeekBar.this.getPaddingStart();
            int left = updatedVirtualViewId == 0 ? 0 : (((updatedVirtualViewId * 2) - 1) * getHalfVirtualViewWidth()) + LabeledSeekBar.this.getPaddingStart();
            right = updatedVirtualViewId == LabeledSeekBar.this.getMax() ? LabeledSeekBar.this.getWidth() : right;
            Rect r = new Rect();
            r.set(left, 0, right, LabeledSeekBar.this.getHeight());
            return r;
        }
    }

    public LabeledSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 16842875);
    }

    public LabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public LabeledSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mProxySeekBarListener = new OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onStopTrackingTouch(seekBar);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onStartTrackingTouch(seekBar);
                }
            }

            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (LabeledSeekBar.this.mOnSeekBarChangeListener != null) {
                    LabeledSeekBar.this.mOnSeekBarChangeListener.onProgressChanged(seekBar, progress, fromUser);
                    LabeledSeekBar.this.sendClickEventForAccessibility(progress);
                    if (VibratorSceneUtils.systemVibrateEnabled(seekBar.getContext())) {
                        LabeledSeekBar.this.mVibratePattern = VibratorSceneUtils.getVibratorScenePattern(seekBar.getContext(), LabeledSeekBar.this.mVibrator, VibratorSceneUtils.VIBRATOR_SCENE_DISPLAY_FONT);
                        VibratorSceneUtils.vibrateIfNeeded(LabeledSeekBar.this.mVibratePattern, LabeledSeekBar.this.mVibrator);
                    }
                }
            }
        };
        this.mAccessHelper = new LabeledSeekBarExploreByTouchHelper(this);
        ViewCompat.setAccessibilityDelegate(this, this.mAccessHelper);
        super.setOnSeekBarChangeListener(this.mProxySeekBarListener);
        if (OPUtils.isSupportXVibrate()) {
            this.mVibrator = (Vibrator) context.getSystemService("vibrator");
        }
    }

    public synchronized void setProgress(int progress) {
        if (this.mAccessHelper != null) {
            this.mAccessHelper.invalidateRoot();
        }
        super.setProgress(progress);
    }

    public void setLabels(String[] labels) {
        this.mLabels = labels;
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        this.mOnSeekBarChangeListener = l;
    }

    /* Access modifiers changed, original: protected */
    public boolean dispatchHoverEvent(MotionEvent event) {
        return this.mAccessHelper.dispatchHoverEvent(event) || super.dispatchHoverEvent(event);
    }

    private void sendClickEventForAccessibility(int progress) {
        this.mAccessHelper.invalidateRoot();
        this.mAccessHelper.sendEventForVirtualView(progress, 1);
    }
}
