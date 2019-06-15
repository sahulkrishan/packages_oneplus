package com.android.settings.search;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ResultPayload implements Parcelable {
    public static final Creator<ResultPayload> CREATOR = new Creator<ResultPayload>() {
        public ResultPayload createFromParcel(Parcel in) {
            return new ResultPayload(in, null);
        }

        public ResultPayload[] newArray(int size) {
            return new ResultPayload[size];
        }
    };
    protected final Intent mIntent;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Availability {
        public static final int AVAILABLE = 0;
        public static final int DISABLED_DEPENDENT_APP = 4;
        public static final int DISABLED_DEPENDENT_SETTING = 1;
        public static final int DISABLED_FOR_USER = 6;
        public static final int DISABLED_UNSUPPORTED = 2;
        public static final int INTENT_ONLY = 5;
        public static final int RESOURCE_CONTENTION = 3;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface PayloadType {
        public static final int INLINE_LIST = 3;
        public static final int INLINE_SLIDER = 1;
        public static final int INLINE_SWITCH = 2;
        public static final int INTENT = 0;
        public static final int SAVED_QUERY = 4;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface SettingsSource {
        public static final int GLOBAL = 3;
        public static final int SECURE = 2;
        public static final int SYSTEM = 1;
        public static final int UNKNOWN = 0;
    }

    /* synthetic */ ResultPayload(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    private ResultPayload(Parcel in) {
        this.mIntent = (Intent) in.readParcelable(ResultPayload.class.getClassLoader());
    }

    public ResultPayload(Intent intent) {
        this.mIntent = intent;
    }

    public int getType() {
        return 0;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mIntent, flags);
    }

    public Intent getIntent() {
        return this.mIntent;
    }
}
