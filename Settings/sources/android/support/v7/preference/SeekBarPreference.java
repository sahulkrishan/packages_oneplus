package android.support.v7.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.v7.preference.Preference.BaseSavedState;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View.OnKeyListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class SeekBarPreference extends Preference {
    private static final String TAG = "SeekBarPreference";
    private boolean mAdjustable;
    private int mMax;
    private int mMin;
    private SeekBar mSeekBar;
    private OnSeekBarChangeListener mSeekBarChangeListener;
    private int mSeekBarIncrement;
    private OnKeyListener mSeekBarKeyListener;
    private int mSeekBarValue;
    private TextView mSeekBarValueTextView;
    private boolean mShowSeekBarValue;
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
        int seekBarValue;

        public SavedState(Parcel source) {
            super(source);
            this.seekBarValue = source.readInt();
            this.min = source.readInt();
            this.max = source.readInt();
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.seekBarValue);
            dest.writeInt(this.min);
            dest.writeInt(this.max);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSeekBarChangeListener = new OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && !SeekBarPreference.this.mTrackingTouch) {
                    SeekBarPreference.this.syncValueInternal(seekBar);
                }
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
                SeekBarPreference.this.mTrackingTouch = true;
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
                SeekBarPreference.this.mTrackingTouch = false;
                if (seekBar.getProgress() + SeekBarPreference.this.mMin != SeekBarPreference.this.mSeekBarValue) {
                    SeekBarPreference.this.syncValueInternal(seekBar);
                }
            }
        };
        this.mSeekBarKeyListener = new OnKeyListener() {
            /* JADX WARNING: Missing block: B:9:0x0018, code skipped:
            return false;
     */
            public boolean onKey(android.view.View r4, int r5, android.view.KeyEvent r6) {
                /*
                r3 = this;
                r0 = r6.getAction();
                r1 = 0;
                if (r0 == 0) goto L_0x0008;
            L_0x0007:
                return r1;
            L_0x0008:
                r0 = android.support.v7.preference.SeekBarPreference.this;
                r0 = r0.mAdjustable;
                if (r0 != 0) goto L_0x0019;
            L_0x0010:
                r0 = 21;
                if (r5 == r0) goto L_0x0018;
            L_0x0014:
                r0 = 22;
                if (r5 != r0) goto L_0x0019;
            L_0x0018:
                return r1;
            L_0x0019:
                r0 = 23;
                if (r5 == r0) goto L_0x003d;
            L_0x001d:
                r0 = 66;
                if (r5 != r0) goto L_0x0022;
            L_0x0021:
                goto L_0x003d;
            L_0x0022:
                r0 = android.support.v7.preference.SeekBarPreference.this;
                r0 = r0.mSeekBar;
                if (r0 != 0) goto L_0x0032;
            L_0x002a:
                r0 = "SeekBarPreference";
                r2 = "SeekBar view is null and hence cannot be adjusted.";
                android.util.Log.e(r0, r2);
                return r1;
            L_0x0032:
                r0 = android.support.v7.preference.SeekBarPreference.this;
                r0 = r0.mSeekBar;
                r0 = r0.onKeyDown(r5, r6);
                return r0;
            L_0x003d:
                return r1;
                */
                throw new UnsupportedOperationException("Method not decompiled: android.support.v7.preference.SeekBarPreference$AnonymousClass2.onKey(android.view.View, int, android.view.KeyEvent):boolean");
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes);
        this.mMin = a.getInt(R.styleable.SeekBarPreference_min, 0);
        setMax(a.getInt(R.styleable.SeekBarPreference_android_max, 100));
        setSeekBarIncrement(a.getInt(R.styleable.SeekBarPreference_seekBarIncrement, 0));
        this.mAdjustable = a.getBoolean(R.styleable.SeekBarPreference_adjustable, true);
        this.mShowSeekBarValue = a.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, true);
        a.recycle();
    }

    public SeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarPreferenceStyle);
    }

    public SeekBarPreference(Context context) {
        this(context, null);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        view.itemView.setOnKeyListener(this.mSeekBarKeyListener);
        this.mSeekBar = (SeekBar) view.findViewById(R.id.seekbar);
        this.mSeekBarValueTextView = (TextView) view.findViewById(R.id.seekbar_value);
        if (this.mShowSeekBarValue) {
            this.mSeekBarValueTextView.setVisibility(0);
        } else {
            this.mSeekBarValueTextView.setVisibility(8);
            this.mSeekBarValueTextView = null;
        }
        if (this.mSeekBar == null) {
            Log.e(TAG, "SeekBar view is null in onBindViewHolder.");
            return;
        }
        this.mSeekBar.setOnSeekBarChangeListener(this.mSeekBarChangeListener);
        this.mSeekBar.setMax(this.mMax - this.mMin);
        if (this.mSeekBarIncrement != 0) {
            this.mSeekBar.setKeyProgressIncrement(this.mSeekBarIncrement);
        } else {
            this.mSeekBarIncrement = this.mSeekBar.getKeyProgressIncrement();
        }
        this.mSeekBar.setProgress(this.mSeekBarValue - this.mMin);
        if (this.mSeekBarValueTextView != null) {
            this.mSeekBarValueTextView.setText(String.valueOf(this.mSeekBarValue));
        }
        this.mSeekBar.setEnabled(isEnabled());
    }

    /* Access modifiers changed, original: protected */
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        int persistedInt;
        if (restoreValue) {
            persistedInt = getPersistedInt(this.mSeekBarValue);
        } else {
            persistedInt = ((Integer) defaultValue).intValue();
        }
        setValue(persistedInt);
    }

    /* Access modifiers changed, original: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return Integer.valueOf(a.getInt(index, 0));
    }

    public void setMin(int min) {
        if (min > this.mMax) {
            min = this.mMax;
        }
        if (min != this.mMin) {
            this.mMin = min;
            notifyChanged();
        }
    }

    public int getMin() {
        return this.mMin;
    }

    public final void setMax(int max) {
        if (max < this.mMin) {
            max = this.mMin;
        }
        if (max != this.mMax) {
            this.mMax = max;
            notifyChanged();
        }
    }

    public final int getSeekBarIncrement() {
        return this.mSeekBarIncrement;
    }

    public final void setSeekBarIncrement(int seekBarIncrement) {
        if (seekBarIncrement != this.mSeekBarIncrement) {
            this.mSeekBarIncrement = Math.min(this.mMax - this.mMin, Math.abs(seekBarIncrement));
            notifyChanged();
        }
    }

    public int getMax() {
        return this.mMax;
    }

    public void setAdjustable(boolean adjustable) {
        this.mAdjustable = adjustable;
    }

    public boolean isAdjustable() {
        return this.mAdjustable;
    }

    public void setValue(int seekBarValue) {
        setValueInternal(seekBarValue, true);
    }

    private void setValueInternal(int seekBarValue, boolean notifyChanged) {
        if (seekBarValue < this.mMin) {
            seekBarValue = this.mMin;
        }
        if (seekBarValue > this.mMax) {
            seekBarValue = this.mMax;
        }
        if (seekBarValue != this.mSeekBarValue) {
            this.mSeekBarValue = seekBarValue;
            if (this.mSeekBarValueTextView != null) {
                this.mSeekBarValueTextView.setText(String.valueOf(this.mSeekBarValue));
            }
            persistInt(seekBarValue);
            if (notifyChanged) {
                notifyChanged();
            }
        }
    }

    public int getValue() {
        return this.mSeekBarValue;
    }

    private void syncValueInternal(SeekBar seekBar) {
        int seekBarValue = this.mMin + seekBar.getProgress();
        if (seekBarValue == this.mSeekBarValue) {
            return;
        }
        if (callChangeListener(Integer.valueOf(seekBarValue))) {
            setValueInternal(seekBarValue, false);
        } else {
            seekBar.setProgress(this.mSeekBarValue - this.mMin);
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.seekBarValue = this.mSeekBarValue;
        myState.min = this.mMin;
        myState.max = this.mMax;
        return myState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state.getClass().equals(SavedState.class)) {
            SavedState myState = (SavedState) state;
            super.onRestoreInstanceState(myState.getSuperState());
            this.mSeekBarValue = myState.seekBarValue;
            this.mMin = myState.min;
            this.mMax = myState.max;
            notifyChanged();
            return;
        }
        super.onRestoreInstanceState(state);
    }
}
