package com.android.settings.fuelgauge.batterytip;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.ArraySet;
import java.util.Objects;

public class AppInfo implements Comparable<AppInfo>, Parcelable {
    public static final Creator CREATOR = new Creator() {
        public AppInfo createFromParcel(Parcel in) {
            return new AppInfo(in);
        }

        public AppInfo[] newArray(int size) {
            return new AppInfo[size];
        }
    };
    public final ArraySet<Integer> anomalyTypes;
    public final String packageName;
    public final long screenOnTimeMs;
    public final int uid;

    public static final class Builder {
        private ArraySet<Integer> mAnomalyTypes = new ArraySet();
        private String mPackageName;
        private long mScreenOnTimeMs;
        private int mUid;

        public Builder addAnomalyType(int type) {
            this.mAnomalyTypes.add(Integer.valueOf(type));
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        public Builder setScreenOnTimeMs(long screenOnTimeMs) {
            this.mScreenOnTimeMs = screenOnTimeMs;
            return this;
        }

        public Builder setUid(int uid) {
            this.mUid = uid;
            return this;
        }

        public AppInfo build() {
            return new AppInfo(this, null);
        }
    }

    /* synthetic */ AppInfo(Builder x0, AnonymousClass1 x1) {
        this(x0);
    }

    private AppInfo(Builder builder) {
        this.packageName = builder.mPackageName;
        this.anomalyTypes = builder.mAnomalyTypes;
        this.screenOnTimeMs = builder.mScreenOnTimeMs;
        this.uid = builder.mUid;
    }

    @VisibleForTesting
    AppInfo(Parcel in) {
        this.packageName = in.readString();
        this.anomalyTypes = in.readArraySet(null);
        this.screenOnTimeMs = in.readLong();
        this.uid = in.readInt();
    }

    public int compareTo(AppInfo o) {
        return Long.compare(this.screenOnTimeMs, o.screenOnTimeMs);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeArraySet(this.anomalyTypes);
        dest.writeLong(this.screenOnTimeMs);
        dest.writeInt(this.uid);
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("packageName=");
        stringBuilder.append(this.packageName);
        stringBuilder.append(",anomalyTypes=");
        stringBuilder.append(this.anomalyTypes);
        stringBuilder.append(",screenTime=");
        stringBuilder.append(this.screenOnTimeMs);
        return stringBuilder.toString();
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AppInfo)) {
            return false;
        }
        AppInfo other = (AppInfo) obj;
        if (!(Objects.equals(this.anomalyTypes, other.anomalyTypes) && this.uid == other.uid && this.screenOnTimeMs == other.screenOnTimeMs && TextUtils.equals(this.packageName, other.packageName))) {
            z = false;
        }
        return z;
    }
}
