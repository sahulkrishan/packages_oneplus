package com.oneplus.settings.highpowerapp;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class HighPowerApp implements Parcelable {
    public static final Creator<HighPowerApp> CREATOR = new Creator<HighPowerApp>() {
        public HighPowerApp createFromParcel(Parcel in) {
            return new HighPowerApp(in);
        }

        public HighPowerApp[] newArray(int size) {
            return new HighPowerApp[size];
        }
    };
    public static final int HIGH_POWER_USAGE = 1;
    public static final int MIDDLE_POWER_USAGE = 0;
    public boolean isLocked;
    public boolean isStopped;
    public String pkgName;
    public int powerLevel;
    public long timeStamp;
    public int uid;

    public HighPowerApp(String pkg, int level, boolean locked, boolean stopped, long time) {
        this.pkgName = pkg;
        this.powerLevel = level;
        this.isLocked = locked;
        this.isStopped = stopped;
        this.timeStamp = time;
    }

    public HighPowerApp(String pkg, int uid, int level, boolean locked, boolean stopped, long time) {
        this.pkgName = pkg;
        this.uid = uid;
        this.powerLevel = level;
        this.isLocked = locked;
        this.isStopped = stopped;
        this.timeStamp = time;
    }

    public int getState() {
        return this.powerLevel;
    }

    public int describeContents() {
        return 0;
    }

    public HighPowerApp(Parcel in) {
        readFromParcel(in);
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.pkgName);
        out.writeInt(this.powerLevel);
        out.writeInt(this.isLocked);
        out.writeInt(this.isStopped);
        out.writeLong(this.timeStamp);
        out.writeInt(this.uid);
    }

    public void readFromParcel(Parcel in) {
        this.pkgName = in.readString();
        this.powerLevel = in.readInt();
        boolean z = false;
        this.isLocked = in.readInt() == 1;
        if (in.readInt() == 1) {
            z = true;
        }
        this.isStopped = z;
        this.timeStamp = in.readLong();
        this.uid = in.readInt();
    }
}
