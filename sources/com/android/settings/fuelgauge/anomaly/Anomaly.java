package com.android.settings.fuelgauge.anomaly;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.TextUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

public class Anomaly implements Parcelable {
    public static final int[] ANOMALY_TYPE_LIST = new int[]{0, 1, 2};
    public static final Creator CREATOR = new Creator() {
        public Anomaly createFromParcel(Parcel in) {
            return new Anomaly(in, null);
        }

        public Anomaly[] newArray(int size) {
            return new Anomaly[size];
        }
    };
    public final boolean backgroundRestrictionEnabled;
    public final long bluetoothScanningTimeMs;
    public final CharSequence displayName;
    public final String packageName;
    public final int targetSdkVersion;
    public final int type;
    public final int uid;
    public final long wakelockTimeMs;
    public final int wakeupAlarmCount;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnomalyActionType {
        public static final int BACKGROUND_CHECK = 1;
        public static final int FORCE_STOP = 0;
        public static final int LOCATION_CHECK = 2;
        public static final int STOP_AND_BACKGROUND_CHECK = 3;
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface AnomalyType {
        public static final int BLUETOOTH_SCAN = 2;
        public static final int WAKEUP_ALARM = 1;
        public static final int WAKE_LOCK = 0;
    }

    public static final class Builder {
        private boolean mBgRestrictionEnabled;
        private long mBluetoothScanningTimeMs;
        private CharSequence mDisplayName;
        private String mPackageName;
        private int mTargetSdkVersion;
        private int mType;
        private int mUid;
        private long mWakeLockTimeMs;
        private int mWakeupAlarmCount;

        public Builder setType(int type) {
            this.mType = type;
            return this;
        }

        public Builder setUid(int uid) {
            this.mUid = uid;
            return this;
        }

        public Builder setDisplayName(CharSequence displayName) {
            this.mDisplayName = displayName;
            return this;
        }

        public Builder setPackageName(String packageName) {
            this.mPackageName = packageName;
            return this;
        }

        public Builder setWakeLockTimeMs(long wakeLockTimeMs) {
            this.mWakeLockTimeMs = wakeLockTimeMs;
            return this;
        }

        public Builder setTargetSdkVersion(int targetSdkVersion) {
            this.mTargetSdkVersion = targetSdkVersion;
            return this;
        }

        public Builder setBackgroundRestrictionEnabled(boolean bgRestrictionEnabled) {
            this.mBgRestrictionEnabled = bgRestrictionEnabled;
            return this;
        }

        public Builder setWakeupAlarmCount(int wakeupAlarmCount) {
            this.mWakeupAlarmCount = wakeupAlarmCount;
            return this;
        }

        public Builder setBluetoothScanningTimeMs(long bluetoothScanningTimeMs) {
            this.mBluetoothScanningTimeMs = bluetoothScanningTimeMs;
            return this;
        }

        public Anomaly build() {
            return new Anomaly(this, null);
        }
    }

    private Anomaly(Builder builder) {
        this.type = builder.mType;
        this.uid = builder.mUid;
        this.displayName = builder.mDisplayName;
        this.packageName = builder.mPackageName;
        this.wakelockTimeMs = builder.mWakeLockTimeMs;
        this.targetSdkVersion = builder.mTargetSdkVersion;
        this.backgroundRestrictionEnabled = builder.mBgRestrictionEnabled;
        this.bluetoothScanningTimeMs = builder.mBluetoothScanningTimeMs;
        this.wakeupAlarmCount = builder.mWakeupAlarmCount;
    }

    private Anomaly(Parcel in) {
        this.type = in.readInt();
        this.uid = in.readInt();
        this.displayName = in.readCharSequence();
        this.packageName = in.readString();
        this.wakelockTimeMs = in.readLong();
        this.targetSdkVersion = in.readInt();
        this.backgroundRestrictionEnabled = in.readBoolean();
        this.wakeupAlarmCount = in.readInt();
        this.bluetoothScanningTimeMs = in.readLong();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type);
        dest.writeInt(this.uid);
        dest.writeCharSequence(this.displayName);
        dest.writeString(this.packageName);
        dest.writeLong(this.wakelockTimeMs);
        dest.writeInt(this.targetSdkVersion);
        dest.writeBoolean(this.backgroundRestrictionEnabled);
        dest.writeInt(this.wakeupAlarmCount);
        dest.writeLong(this.bluetoothScanningTimeMs);
    }

    public boolean equals(Object obj) {
        boolean z = true;
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Anomaly)) {
            return false;
        }
        Anomaly other = (Anomaly) obj;
        if (!(this.type == other.type && this.uid == other.uid && this.wakelockTimeMs == other.wakelockTimeMs && TextUtils.equals(this.displayName, other.displayName) && TextUtils.equals(this.packageName, other.packageName) && this.targetSdkVersion == other.targetSdkVersion && this.backgroundRestrictionEnabled == other.backgroundRestrictionEnabled && this.wakeupAlarmCount == other.wakeupAlarmCount && this.bluetoothScanningTimeMs == other.bluetoothScanningTimeMs)) {
            z = false;
        }
        return z;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{Integer.valueOf(this.type), Integer.valueOf(this.uid), this.displayName, this.packageName, Long.valueOf(this.wakelockTimeMs), Integer.valueOf(this.targetSdkVersion), Boolean.valueOf(this.backgroundRestrictionEnabled), Integer.valueOf(this.wakeupAlarmCount), Long.valueOf(this.bluetoothScanningTimeMs)});
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("type=");
        stringBuilder.append(toAnomalyTypeText(this.type));
        stringBuilder.append(" uid=");
        stringBuilder.append(this.uid);
        stringBuilder.append(" package=");
        stringBuilder.append(this.packageName);
        stringBuilder.append(" displayName=");
        stringBuilder.append(this.displayName);
        stringBuilder.append(" wakelockTimeMs=");
        stringBuilder.append(this.wakelockTimeMs);
        stringBuilder.append(" wakeupAlarmCount=");
        stringBuilder.append(this.wakeupAlarmCount);
        stringBuilder.append(" bluetoothTimeMs=");
        stringBuilder.append(this.bluetoothScanningTimeMs);
        return stringBuilder.toString();
    }

    private String toAnomalyTypeText(int type) {
        switch (type) {
            case 0:
                return "wakelock";
            case 1:
                return "wakeupAlarm";
            case 2:
                return "unoptimizedBluetoothScan";
            default:
                return "";
        }
    }
}
