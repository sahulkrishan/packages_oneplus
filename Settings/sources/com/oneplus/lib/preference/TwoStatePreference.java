package com.oneplus.lib.preference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.oneplus.lib.preference.Preference.BaseSavedState;

public abstract class TwoStatePreference extends Preference {
    boolean mChecked;
    private boolean mCheckedSet;
    private boolean mDisableDependentsState;
    private CharSequence mSummaryOff;
    private CharSequence mSummaryOn;

    static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        boolean checked;

        public SavedState(Parcel source) {
            super(source);
            boolean z = true;
            if (source.readInt() != 1) {
                z = false;
            }
            this.checked = z;
        }

        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(this.checked);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }
    }

    public TwoStatePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TwoStatePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TwoStatePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoStatePreference(Context context) {
        this(context, null);
    }

    /* Access modifiers changed, original: protected */
    public void onClick() {
        super.onClick();
        boolean newValue = isChecked() ^ 1;
        if (callChangeListener(Boolean.valueOf(newValue))) {
            setChecked(newValue);
        }
    }

    public void setChecked(boolean checked) {
        boolean changed = this.mChecked != checked;
        if (changed || !this.mCheckedSet) {
            this.mChecked = checked;
            this.mCheckedSet = true;
            persistBoolean(checked);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    public boolean isChecked() {
        return this.mChecked;
    }

    public boolean shouldDisableDependents() {
        boolean shouldDisable = this.mDisableDependentsState ? this.mChecked : !this.mChecked;
        if (shouldDisable || super.shouldDisableDependents()) {
            return true;
        }
        return false;
    }

    public void setSummaryOn(CharSequence summary) {
        setSummaryOnFromTwoState(summary);
        this.mSummaryOn = summary;
        if (isChecked()) {
            notifyChanged();
        }
    }

    public void setSummaryOn(int summaryResId) {
        setSummaryOn(getContext().getString(summaryResId));
    }

    public CharSequence getSummaryOn() {
        return this.mSummaryOn;
    }

    public void setSummaryOff(CharSequence summary) {
        setSummaryOffFromTwoState(summary);
        this.mSummaryOff = summary;
        if (!isChecked()) {
            notifyChanged();
        }
    }

    public void setSummaryOff(int summaryResId) {
        setSummaryOff(getContext().getString(summaryResId));
    }

    public CharSequence getSummaryOff() {
        return this.mSummaryOff;
    }

    public boolean getDisableDependentsState() {
        return this.mDisableDependentsState;
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        this.mDisableDependentsState = disableDependentsState;
    }

    /* Access modifiers changed, original: protected */
    public Object onGetDefaultValue(TypedArray a, int index) {
        return Boolean.valueOf(a.getBoolean(index, false));
    }

    /* Access modifiers changed, original: protected */
    public void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        boolean persistedBoolean;
        if (restoreValue) {
            persistedBoolean = getPersistedBoolean(this.mChecked);
        } else {
            persistedBoolean = ((Boolean) defaultValue).booleanValue();
        }
        setChecked(persistedBoolean);
    }

    /* Access modifiers changed, original: 0000 */
    public void syncSummaryView(View view) {
        TextView summaryView = (TextView) view.findViewById(16908304);
        if (summaryView != null) {
            boolean useDefaultSummary = true;
            if (this.mChecked && !TextUtils.isEmpty(this.mSummaryOn)) {
                summaryView.setText(this.mSummaryOn);
                useDefaultSummary = false;
            } else if (!(this.mChecked || TextUtils.isEmpty(this.mSummaryOff))) {
                summaryView.setText(this.mSummaryOff);
                useDefaultSummary = false;
            }
            if (useDefaultSummary) {
                CharSequence summary = getSummary();
                if (!TextUtils.isEmpty(summary)) {
                    summaryView.setText(summary);
                    useDefaultSummary = false;
                }
            }
            int newVisibility = 8;
            if (!useDefaultSummary) {
                newVisibility = 0;
            }
            if (newVisibility != summaryView.getVisibility()) {
                summaryView.setVisibility(newVisibility);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            return superState;
        }
        SavedState myState = new SavedState(superState);
        myState.checked = isChecked();
        return myState;
    }

    /* Access modifiers changed, original: protected */
    public void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.checked);
    }
}
