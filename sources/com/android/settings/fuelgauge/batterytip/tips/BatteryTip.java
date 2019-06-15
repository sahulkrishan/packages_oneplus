package com.android.settings.fuelgauge.batterytip.tips;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IdRes;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.util.SparseIntArray;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public abstract class BatteryTip implements Comparable<BatteryTip>, Parcelable {
    private static final String KEY_PREFIX = "key_battery_tip";
    @VisibleForTesting
    static final SparseIntArray TIP_ORDER = new SparseIntArray();
    protected boolean mNeedUpdate;
    protected boolean mShowDialog;
    protected int mState;
    protected int mType;

    @Retention(RetentionPolicy.SOURCE)
    public @interface StateType {
        public static final int HANDLED = 1;
        public static final int INVISIBLE = 2;
        public static final int NEW = 0;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface TipType {
        public static final int APP_RESTRICTION = 1;
        public static final int BATTERY_SAVER = 3;
        public static final int HIGH_DEVICE_USAGE = 2;
        public static final int LOW_BATTERY = 5;
        public static final int REDUCED_BATTERY = 4;
        public static final int REMOVE_APP_RESTRICTION = 7;
        public static final int SMART_BATTERY_MANAGER = 0;
        public static final int SUMMARY = 6;
    }

    @IdRes
    public abstract int getIconId();

    public abstract CharSequence getSummary(Context context);

    public abstract CharSequence getTitle(Context context);

    public abstract void log(Context context, MetricsFeatureProvider metricsFeatureProvider);

    public abstract void updateState(BatteryTip batteryTip);

    static {
        TIP_ORDER.append(1, 0);
        TIP_ORDER.append(3, 1);
        TIP_ORDER.append(2, 2);
        TIP_ORDER.append(5, 3);
        TIP_ORDER.append(6, 4);
        TIP_ORDER.append(0, 5);
        TIP_ORDER.append(4, 6);
        TIP_ORDER.append(7, 7);
    }

    BatteryTip(Parcel in) {
        this.mType = in.readInt();
        this.mState = in.readInt();
        this.mShowDialog = in.readBoolean();
        this.mNeedUpdate = in.readBoolean();
    }

    BatteryTip(int type, int state, boolean showDialog) {
        this.mType = type;
        this.mState = state;
        this.mShowDialog = showDialog;
        this.mNeedUpdate = true;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mType);
        dest.writeInt(this.mState);
        dest.writeBoolean(this.mShowDialog);
        dest.writeBoolean(this.mNeedUpdate);
    }

    public Preference buildPreference(Context context) {
        Preference preference = new Preference(context);
        preference.setKey(getKey());
        preference.setTitle(getTitle(context));
        preference.setSummary(getSummary(context));
        preference.setIcon(getIconId());
        return preference;
    }

    public boolean shouldShowDialog() {
        return this.mShowDialog;
    }

    public boolean needUpdate() {
        return this.mNeedUpdate;
    }

    public String getKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(KEY_PREFIX);
        stringBuilder.append(this.mType);
        return stringBuilder.toString();
    }

    public int getType() {
        return this.mType;
    }

    public int getState() {
        return this.mState;
    }

    public boolean isVisible() {
        return this.mState != 2;
    }

    public int compareTo(BatteryTip o) {
        return TIP_ORDER.get(this.mType) - TIP_ORDER.get(o.mType);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("type=");
        stringBuilder.append(this.mType);
        stringBuilder.append(" state=");
        stringBuilder.append(this.mState);
        return stringBuilder.toString();
    }
}
