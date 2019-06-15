package com.android.settings.search;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;

public abstract class InlinePayload extends ResultPayload {
    public static final int FALSE = 0;
    public static final int TRUE = 1;
    final int mDefaultvalue;
    final boolean mIsDeviceSupported;
    private final String mSettingKey;
    final int mSettingSource;

    public abstract int getType();

    public abstract int standardizeInput(int i) throws IllegalArgumentException;

    public InlinePayload(String key, int source, Intent intent, boolean isDeviceSupported, int defaultValue) {
        super(intent);
        this.mSettingKey = key;
        this.mSettingSource = source;
        this.mIsDeviceSupported = isDeviceSupported;
        this.mDefaultvalue = defaultValue;
    }

    InlinePayload(Parcel parcel) {
        super((Intent) parcel.readParcelable(Intent.class.getClassLoader()));
        this.mSettingKey = parcel.readString();
        this.mSettingSource = parcel.readInt();
        boolean z = true;
        if (parcel.readInt() != 1) {
            z = false;
        }
        this.mIsDeviceSupported = z;
        this.mDefaultvalue = parcel.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(this.mSettingKey);
        dest.writeInt(this.mSettingSource);
        dest.writeInt(this.mIsDeviceSupported);
        dest.writeInt(this.mDefaultvalue);
    }

    public int getAvailability() {
        if (this.mIsDeviceSupported) {
            return 0;
        }
        return 2;
    }

    public int getValue(Context context) {
        int settingsValue = -1;
        switch (this.mSettingSource) {
            case 1:
                settingsValue = System.getInt(context.getContentResolver(), this.mSettingKey, this.mDefaultvalue);
                break;
            case 2:
                settingsValue = Secure.getInt(context.getContentResolver(), this.mSettingKey, this.mDefaultvalue);
                break;
            case 3:
                settingsValue = Global.getInt(context.getContentResolver(), this.mSettingKey, this.mDefaultvalue);
                break;
        }
        return standardizeInput(settingsValue);
    }

    public boolean setValue(Context context, int newValue) {
        newValue = standardizeInput(newValue);
        switch (this.mSettingSource) {
            case 0:
                return false;
            case 1:
                return System.putInt(context.getContentResolver(), this.mSettingKey, newValue);
            case 2:
                return Secure.putInt(context.getContentResolver(), this.mSettingKey, newValue);
            case 3:
                return Global.putInt(context.getContentResolver(), this.mSettingKey, newValue);
            default:
                return false;
        }
    }

    public String getKey() {
        return this.mSettingKey;
    }
}
