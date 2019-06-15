package com.android.settings.search;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable.Creator;

public class InlineListPayload extends InlinePayload {
    public static final Creator<InlineListPayload> CREATOR = new Creator<InlineListPayload>() {
        public InlineListPayload createFromParcel(Parcel in) {
            return new InlineListPayload(in, null);
        }

        public InlineListPayload[] newArray(int size) {
            return new InlineListPayload[size];
        }
    };
    private int mNumOptions;

    /* synthetic */ InlineListPayload(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public InlineListPayload(String key, int payloadType, Intent intent, boolean isDeviceSupported, int numOptions, int defaultValue) {
        super(key, payloadType, intent, isDeviceSupported, defaultValue);
        this.mNumOptions = numOptions;
    }

    private InlineListPayload(Parcel in) {
        super(in);
        this.mNumOptions = in.readInt();
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mNumOptions);
    }

    /* Access modifiers changed, original: protected */
    public int standardizeInput(int input) throws IllegalArgumentException {
        if (input >= 0 && input < this.mNumOptions) {
            return input;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid argument for ListSelect. Expected between 0 and ");
        stringBuilder.append(this.mNumOptions);
        stringBuilder.append(" but found: ");
        stringBuilder.append(input);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getType() {
        return 3;
    }
}
