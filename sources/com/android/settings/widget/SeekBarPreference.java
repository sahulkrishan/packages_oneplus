package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v4.content.res.TypedArrayUtils;
import android.support.v7.preference.Preference.BaseSavedState;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnKeyListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.RangeInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import com.android.internal.R;
import com.android.settingslib.RestrictedPreference;

public class SeekBarPreference extends RestrictedPreference implements OnSeekBarChangeListener, OnKeyListener {
    private int mAccessibilityRangeInfoType;
    private boolean mContinuousUpdates;
    private int mDefaultProgress;
    private int mMax;
    private int mMin;
    private int mProgress;
    private SeekBar mSeekBar;
    private CharSequence mSeekBarContentDescription;
    private boolean mShouldBlink;
    private boolean mTrackingTouch;

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        int max;
        int min;
        int progress;

        public SavedState(Parcel source) {
            super(source);
            this.progress = source.readInt();
            this.max = source.readInt();
            this.min = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.max);
            dest.writeInt(this.min);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mDefaultProgress = -1;
        this.mAccessibilityRangeInfoType = 0;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProgressBar, defStyleAttr, defStyleRes);
        setMax(a.getInt(2, this.mMax));
        setMin(a.getInt(26, this.mMin));
        a.recycle();
        a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        int layoutResId = a.getResourceId(0, 17367240);
        a.recycle();
        setLayoutResource(layoutResId);
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, TypedArrayUtils.getAttr(context, com.android.settings.R.attr.seekBarPreferenceStyle, 17891517));
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    public void setShouldBlink(boolean shouldBlink) {
        this.mShouldBlink = shouldBlink;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setOnKeyListener(this);
        this.mSeekBar = (SeekBar) view.findViewById(16909273);
        this.mSeekBar.setOnSeekBarChangeListener(this);
        this.mSeekBar.setMax(this.mMax);
        this.mSeekBar.setMin(this.mMin);
        this.mSeekBar.setProgress(this.mProgress);
        this.mSeekBar.setEnabled(isEnabled());
        CharSequence title = getTitle();
        if (!TextUtils.isEmpty(this.mSeekBarContentDescription)) {
            this.mSeekBar.setContentDescription(this.mSeekBarContentDescription);
        } else if (!TextUtils.isEmpty(title)) {
            this.mSeekBar.setContentDescription(title);
        }
        if (this.mSeekBar instanceof DefaultIndicatorSeekBar) {
            ((DefaultIndicatorSeekBar) this.mSeekBar).setDefaultProgress(this.mDefaultProgress);
        }
        if (this.mShouldBlink) {
            View v = view.itemView;
            v.post(new -$$Lambda$SeekBarPreference$dLBfCJMqk38mmQ3tQY-pIyDA0S8(this, v));
        }
        this.mSeekBar.setAccessibilityDelegate(new AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(view, info);
                RangeInfo rangeInfo = info.getRangeInfo();
                if (rangeInfo != null) {
                    info.setRangeInfo(RangeInfo.obtain(SeekBarPreference.this.mAccessibilityRangeInfoType, rangeInfo.getMin(), rangeInfo.getMax(), rangeInfo.getCurrent()));
                }
            }
        });
    }

    public static /* synthetic */ void lambda$onBindViewHolder$0(SeekBarPreference seekBarPreference, View v) {
        if (v.getBackground() != null) {
            v.getBackground().setHotspot((float) (v.getWidth() / 2), (float) (v.getHeight() / 2));
        }
        v.setPressed(true);
        v.setPressed(false);
        seekBarPreference.mShouldBlink = false;
    }

    public CharSequence getSummary() {
        return null;
    }

    /* Access modifiers changed, original: protected */
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int persistedInt;
        if (restoreValue) {
            persistedInt = getPersistedInt(this.mProgress);
        } else {
            persistedInt = ((Integer) defaultValue).intValue();
        }
        setProgress(persistedInt);
    }

    /* Access modifiers changed, original: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != 0) {
            return false;
        }
        SeekBar seekBar = (SeekBar) v.findViewById(16909273);
        if (seekBar == null) {
            return false;
        }
        return seekBar.onKeyDown(keyCode, event);
    }

    public void setMax(int max) {
        if (max != this.mMax) {
            this.mMax = max;
            notifyChanged();
        }
    }

    public void setMin(int min) {
        if (min != this.mMin) {
            this.mMin = min;
            notifyChanged();
        }
    }

    public int getMax() {
        return this.mMax;
    }

    public int getMin() {
        return this.mMin;
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    public void setDefaultProgress(int defaultProgress) {
        if (this.mDefaultProgress != defaultProgress) {
            this.mDefaultProgress = defaultProgress;
            if (this.mSeekBar instanceof DefaultIndicatorSeekBar) {
                ((DefaultIndicatorSeekBar) this.mSeekBar).setDefaultProgress(this.mDefaultProgress);
            }
        }
    }

    public void setContinuousUpdates(boolean continuousUpdates) {
        this.mContinuousUpdates = continuousUpdates;
    }

    private void setProgress(int progress, boolean notifyChanged) {
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress < this.mMin) {
            progress = this.mMin;
        }
        if (progress != this.mProgress) {
            this.mProgress = progress;
            persistInt(progress);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getProgress() {
        return this.mProgress;
    }

    /* Access modifiers changed, original: 0000 */
    public void syncProgress(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        if (progress == this.mProgress) {
            return;
        }
        if (callChangeListener(Integer.valueOf(progress))) {
            setProgress(progress, false);
        } else {
            seekBar.setProgress(this.mProgress);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) {
            return;
        }
        if (this.mContinuousUpdates || !this.mTrackingTouch) {
            syncProgress(seekBar);
        }
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = true;
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() != this.mProgress) {
            syncProgress(seekBar);
        }
    }

    public void setAccessibilityRangeInfoType(int rangeInfoType) {
        this.mAccessibilityRangeInfoType = rangeInfoType;
    }

    public void setSeekBarContentDescription(CharSequence contentDescription) {
        this.mSeekBarContentDescription = contentDescription;
        if (this.mSeekBar != null) {
            this.mSeekBar.setContentDescription(contentDescription);
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.progress = this.mProgress;
        myState.max = this.mMax;
        myState.min = this.mMin;
        return myState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state.getClass().equals(SavedState.class)) {
            SavedState myState = (SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            this.mProgress = myState.progress;
            this.mMax = myState.max;
            this.mMin = myState.min;
            notifyChanged();
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
