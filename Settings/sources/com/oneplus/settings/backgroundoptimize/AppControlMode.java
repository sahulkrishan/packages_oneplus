package com.oneplus.settings.backgroundoptimize;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class AppControlMode implements Parcelable {
    public static final Creator<AppControlMode> CREATOR = new Creator<AppControlMode>() {
        public AppControlMode createFromParcel(Parcel source) {
            return new AppControlMode(source, null);
        }

        public AppControlMode[] newArray(int size) {
            return new AppControlMode[size];
        }
    };
    public int mode;
    public String packageName;
    public int value;

    /* synthetic */ AppControlMode(Parcel x0, AnonymousClass1 x1) {
        this(x0);
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.packageName);
        dest.writeInt(this.mode);
        dest.writeInt(this.value);
    }

    public void readFromParcel(Parcel source) {
        this.packageName = source.readString();
        this.mode = source.readInt();
        this.value = source.readInt();
    }

    public AppControlMode(String packageName, int mode, int value) {
        this.packageName = packageName;
        this.mode = mode;
        this.value = value;
    }

    private AppControlMode(Parcel source) {
        readFromParcel(source);
    }

    public String toString(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append(" packageName=");
        sb.append(this.packageName);
        sb.append(" mode=");
        sb.append(this.mode);
        sb.append(" value=");
        sb.append(this.value);
        return sb.toString();
    }

    public String toString() {
        return toString("");
    }
}
