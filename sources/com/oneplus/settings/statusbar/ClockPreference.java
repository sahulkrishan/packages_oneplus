package com.oneplus.settings.statusbar;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;

public class ClockPreference extends ListPreference {
    private static final String DEFAULT = "default";
    private static final String DISABLED = "disabled";
    private static final String SECONDS = "seconds";
    private static final String TAG = "ClockPreference";
    private ArraySet<String> mBlacklist;
    private final String mClock;
    private boolean mClockEnabled;
    private boolean mHasSeconds;
    private boolean mHasSetValue;
    private Utils mUtils;

    public ClockPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mUtils = new Utils(context);
        this.mClock = context.getString(17040966);
        setEntryValues(new CharSequence[]{SECONDS, DEFAULT, DISABLED});
    }

    public void onAttached() {
        super.onAttached();
        updateUI();
    }

    public void onDetached() {
        super.onDetached();
    }

    private void updateUI() {
        updateStatus();
        if (!this.mHasSetValue) {
            this.mHasSetValue = true;
            if (this.mClockEnabled && this.mHasSeconds) {
                setValue(SECONDS);
            } else if (this.mClockEnabled) {
                setValue(DEFAULT);
            } else {
                setValue(DISABLED);
            }
        }
    }

    private void updateStatus() {
        this.mBlacklist = Utils.getIconBlacklist(this.mUtils.getValue(Utils.ICON_BLACKLIST));
        boolean z = true;
        this.mClockEnabled = this.mBlacklist.contains(this.mClock) ^ 1;
        if (this.mUtils.getValue(Utils.CLOCK_SECONDS, 0) == 0) {
            z = false;
        }
        this.mHasSeconds = z;
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("updateStatus mBlacklist:");
        stringBuilder.append(this.mBlacklist);
        stringBuilder.append(" TextUtils.join:");
        stringBuilder.append(TextUtils.join(",", this.mBlacklist));
        Log.i(str, stringBuilder.toString());
    }

    /* Access modifiers changed, original: protected */
    public boolean persistString(String value) {
        if (this.mUtils == null) {
            return true;
        }
        updateStatus();
        this.mUtils.setValue(Utils.CLOCK_SECONDS, SECONDS.equals(value));
        if (DISABLED.equals(value)) {
            this.mBlacklist.add(this.mClock);
        } else {
            this.mBlacklist.remove(this.mClock);
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("update value:");
        stringBuilder.append(value);
        stringBuilder.append(" mBlacklist:");
        stringBuilder.append(TextUtils.join(",", this.mBlacklist));
        Log.i(str, stringBuilder.toString());
        this.mUtils.setValue(Utils.ICON_BLACKLIST, TextUtils.join(",", this.mBlacklist));
        return true;
    }
}
