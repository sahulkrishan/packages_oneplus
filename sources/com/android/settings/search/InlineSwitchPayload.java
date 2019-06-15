package com.android.settings.search;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public class InlineSwitchPayload extends InlinePayload {
    public static final Creator<InlineSwitchPayload> CREATOR = new Creator<InlineSwitchPayload>() {
        public InlineSwitchPayload createFromParcel(Parcel in) {
            return new InlineSwitchPayload(in, null);
        }

        public InlineSwitchPayload[] newArray(int size) {
            return new InlineSwitchPayload[size];
        }
    };
    private static final int OFF = 0;
    private static final int ON = 1;
    private boolean mIsStandard;

    /* synthetic */ InlineSwitchPayload(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public InlineSwitchPayload(String key, int source, int onValue, Intent intent, boolean isDeviceSupported, int defaultValue) {
        super(key, source, intent, isDeviceSupported, defaultValue);
        boolean z = true;
        if (onValue != 1) {
            z = false;
        }
        this.mIsStandard = z;
    }

    private InlineSwitchPayload(Parcel in) {
        super(in);
        boolean z = true;
        if (in.readInt() != 1) {
            z = false;
        }
        this.mIsStandard = z;
    }

    public int getType() {
        return 2;
    }

    /* Access modifiers changed, original: protected */
    public int standardizeInput(int value) {
        if (value != 0 && value != 1) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid input for InlineSwitch. Expected: 1 or 0 but found: ");
            stringBuilder.append(value);
            throw new IllegalArgumentException(stringBuilder.toString());
        } else if (this.mIsStandard) {
            return value;
        } else {
            return 1 - value;
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mIsStandard);
    }

    public boolean isStandard() {
        return this.mIsStandard;
    }
}
