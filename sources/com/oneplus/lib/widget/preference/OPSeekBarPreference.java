package com.oneplus.lib.widget.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.SeekBar;
import com.oneplus.commonctrl.R;
import com.oneplus.lib.preference.Preference.BaseSavedState;
import com.oneplus.lib.widget.OPSeekBar;
import com.oneplus.lib.widget.OPSeekBar.OnSeekBarChangeListener;
import com.oneplus.lib.widget.util.utils;

public class OPSeekBarPreference extends OPPreference implements OnSeekBarChangeListener {
    private int mMax;
    private int mProgress;
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
        int progress;

        public SavedState(Parcel source) {
            super(source);
            this.progress = source.readInt();
            this.max = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.progress);
            dest.writeInt(this.max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public OPSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
        setMax(100);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, utils.resolveDefStyleAttr(context, defStyleAttr), defStyleRes);
        int layoutResId = a.getResourceId(R.styleable.SeekBarPreference_android_layout, R.layout.preference_widget_seekbar);
        a.recycle();
        setLayoutResource(layoutResId);
    }

    public OPSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.style.OnePlus_DeviceDefault_Preference_Material_SeekBarPreference);
    }

    public OPSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.op_seekBarPreferenceStyle);
    }

    public OPSeekBarPreference(Context context) {
        this(context, null);
    }

    /* Access modifiers changed, original: protected */
    public void onBindView(View view) {
        super.onBindView(view);
        ((SeekBar) view.findViewById(R.id.seekbar)).setVisibility(8);
        OPSeekBar opSeekBar = (OPSeekBar) view.findViewById(R.id.opseekbar);
        if (opSeekBar != null) {
            opSeekBar.setOnSeekBarChangeListener(this);
            opSeekBar.setMax(this.mMax);
            opSeekBar.setProgress(this.mProgress);
            opSeekBar.setEnabled(isEnabled());
            opSeekBar.setVisibility(0);
        }
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
        if (event.getAction() != 1) {
            if (keyCode == 81 || keyCode == 70) {
                setProgress(getProgress() + 1);
                return true;
            } else if (keyCode == 69) {
                setProgress(getProgress() - 1);
                return true;
            }
        }
        return false;
    }

    public void setMax(int max) {
        if (max != this.mMax) {
            this.mMax = max;
            notifyChanged();
        }
    }

    public void setProgress(int progress) {
        setProgress(progress, true);
    }

    private void setProgress(int progress, boolean notifyChanged) {
        if (progress > this.mMax) {
            progress = this.mMax;
        }
        if (progress < 0) {
            progress = 0;
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
    public void syncProgress(OPSeekBar seekBar) {
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

    public void onProgressChanged(OPSeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser && !this.mTrackingTouch) {
            syncProgress(seekBar);
        }
    }

    public void onStartTrackingTouch(OPSeekBar seekBar) {
        this.mTrackingTouch = true;
    }

    public void onStopTrackingTouch(OPSeekBar seekBar) {
        this.mTrackingTouch = false;
        if (seekBar.getProgress() != this.mProgress) {
            syncProgress(seekBar);
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
        return myState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state.getClass().equals(SavedState.class)) {
            SavedState myState = (SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            this.mProgress = myState.progress;
            this.mMax = myState.max;
            notifyChanged();
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
