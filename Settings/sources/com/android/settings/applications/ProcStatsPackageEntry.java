package com.android.settings.applications;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import java.util.ArrayList;

public class ProcStatsPackageEntry implements Parcelable {
    private static final float ALWAYS_THRESHOLD = 0.95f;
    public static final Creator<ProcStatsPackageEntry> CREATOR = new Creator<ProcStatsPackageEntry>() {
        public ProcStatsPackageEntry createFromParcel(Parcel in) {
            return new ProcStatsPackageEntry(in);
        }

        public ProcStatsPackageEntry[] newArray(int size) {
            return new ProcStatsPackageEntry[size];
        }
    };
    private static boolean DEBUG = false;
    private static final float SOMETIMES_THRESHOLD = 0.25f;
    private static final String TAG = "ProcStatsEntry";
    long mAvgBgMem;
    long mAvgRunMem;
    long mBgDuration;
    double mBgWeight;
    final ArrayList<ProcStatsEntry> mEntries = new ArrayList();
    long mMaxBgMem;
    long mMaxRunMem;
    final String mPackage;
    long mRunDuration;
    double mRunWeight;
    public String mUiLabel;
    public ApplicationInfo mUiTargetApp;
    private long mWindowLength;

    public ProcStatsPackageEntry(String pkg, long windowLength) {
        this.mPackage = pkg;
        this.mWindowLength = windowLength;
    }

    public ProcStatsPackageEntry(Parcel in) {
        this.mPackage = in.readString();
        in.readTypedList(this.mEntries, ProcStatsEntry.CREATOR);
        this.mBgDuration = in.readLong();
        this.mAvgBgMem = in.readLong();
        this.mMaxBgMem = in.readLong();
        this.mBgWeight = in.readDouble();
        this.mRunDuration = in.readLong();
        this.mAvgRunMem = in.readLong();
        this.mMaxRunMem = in.readLong();
        this.mRunWeight = in.readDouble();
    }

    public CharSequence getRunningFrequency(Context context) {
        return getFrequency(((float) this.mRunDuration) / ((float) this.mWindowLength), context);
    }

    public CharSequence getBackgroundFrequency(Context context) {
        return getFrequency(((float) this.mBgDuration) / ((float) this.mWindowLength), context);
    }

    public void addEntry(ProcStatsEntry entry) {
        this.mEntries.add(entry);
    }

    public void updateMetrics() {
        this.mMaxBgMem = 0;
        this.mAvgBgMem = 0;
        this.mBgDuration = 0;
        this.mBgWeight = 0.0d;
        this.mMaxRunMem = 0;
        this.mAvgRunMem = 0;
        this.mRunDuration = 0;
        this.mRunWeight = 0.0d;
        int N = this.mEntries.size();
        for (int i = 0; i < N; i++) {
            ProcStatsEntry entry = (ProcStatsEntry) this.mEntries.get(i);
            this.mBgDuration = Math.max(entry.mBgDuration, this.mBgDuration);
            this.mAvgBgMem += entry.mAvgBgMem;
            this.mBgWeight += entry.mBgWeight;
            this.mRunDuration = Math.max(entry.mRunDuration, this.mRunDuration);
            this.mAvgRunMem += entry.mAvgRunMem;
            this.mRunWeight += entry.mRunWeight;
            this.mMaxBgMem += entry.mMaxBgMem;
            this.mMaxRunMem += entry.mMaxRunMem;
        }
        this.mAvgBgMem /= (long) N;
        this.mAvgRunMem /= (long) N;
    }

    public void retrieveUiData(Context context, PackageManager pm) {
        this.mUiTargetApp = null;
        this.mUiLabel = this.mPackage;
        try {
            if (Utils.OS_PKG.equals(this.mPackage)) {
                this.mUiTargetApp = pm.getApplicationInfo("android", 4227584);
                this.mUiLabel = context.getString(R.string.process_stats_os_label);
                return;
            }
            this.mUiTargetApp = pm.getApplicationInfo(this.mPackage, 4227584);
            this.mUiLabel = this.mUiTargetApp.loadLabel(pm).toString();
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("could not find package: ");
            stringBuilder.append(this.mPackage);
            Log.d(str, stringBuilder.toString());
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mPackage);
        dest.writeTypedList(this.mEntries);
        dest.writeLong(this.mBgDuration);
        dest.writeLong(this.mAvgBgMem);
        dest.writeLong(this.mMaxBgMem);
        dest.writeDouble(this.mBgWeight);
        dest.writeLong(this.mRunDuration);
        dest.writeLong(this.mAvgRunMem);
        dest.writeLong(this.mMaxRunMem);
        dest.writeDouble(this.mRunWeight);
    }

    public static CharSequence getFrequency(float amount, Context context) {
        if (amount > ALWAYS_THRESHOLD) {
            return context.getString(R.string.always_running, new Object[]{com.android.settingslib.Utils.formatPercentage((int) (100.0f * amount))});
        } else if (amount > SOMETIMES_THRESHOLD) {
            return context.getString(R.string.sometimes_running, new Object[]{com.android.settingslib.Utils.formatPercentage((int) (100.0f * amount))});
        } else {
            return context.getString(R.string.rarely_running, new Object[]{com.android.settingslib.Utils.formatPercentage((int) (100.0f * amount))});
        }
    }

    public double getRunWeight() {
        return this.mRunWeight;
    }

    public double getBgWeight() {
        return this.mBgWeight;
    }

    public ArrayList<ProcStatsEntry> getEntries() {
        return this.mEntries;
    }
}
