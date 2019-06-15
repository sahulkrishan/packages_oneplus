package com.android.settings.applications;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.preference.Preference;
import android.text.format.Formatter;
import com.android.internal.util.Preconditions;
import com.android.settingslib.applications.StorageStatsSource.AppStorageStats;

public class AppStorageSizesController {
    private final Preference mAppSize;
    private final Preference mCacheSize;
    private boolean mCachedCleared;
    @StringRes
    private final int mComputing;
    private boolean mDataCleared;
    private final Preference mDataSize;
    @StringRes
    private final int mError;
    private long mLastCacheSize;
    private long mLastCodeSize;
    private long mLastDataSize;
    @Nullable
    private AppStorageStats mLastResult;
    private boolean mLastResultFailed;
    private long mLastTotalSize;
    private final Preference mTotalSize;

    public static class Builder {
        private Preference mAppSize;
        private Preference mCacheSize;
        @StringRes
        private int mComputing;
        private Preference mDataSize;
        @StringRes
        private int mError;
        private Preference mTotalSize;

        public Builder setAppSizePreference(Preference preference) {
            this.mAppSize = preference;
            return this;
        }

        public Builder setDataSizePreference(Preference preference) {
            this.mDataSize = preference;
            return this;
        }

        public Builder setCacheSizePreference(Preference preference) {
            this.mCacheSize = preference;
            return this;
        }

        public Builder setTotalSizePreference(Preference preference) {
            this.mTotalSize = preference;
            return this;
        }

        public Builder setComputingString(@StringRes int sequence) {
            this.mComputing = sequence;
            return this;
        }

        public Builder setErrorString(@StringRes int sequence) {
            this.mError = sequence;
            return this;
        }

        public AppStorageSizesController build() {
            return new AppStorageSizesController((Preference) Preconditions.checkNotNull(this.mTotalSize), (Preference) Preconditions.checkNotNull(this.mAppSize), (Preference) Preconditions.checkNotNull(this.mDataSize), (Preference) Preconditions.checkNotNull(this.mCacheSize), this.mComputing, this.mError);
        }
    }

    private AppStorageSizesController(Preference total, Preference app, Preference data, Preference cache, @StringRes int computing, @StringRes int error) {
        this.mLastCodeSize = -1;
        this.mLastDataSize = -1;
        this.mLastCacheSize = -1;
        this.mLastTotalSize = -1;
        this.mTotalSize = total;
        this.mAppSize = app;
        this.mDataSize = data;
        this.mCacheSize = cache;
        this.mComputing = computing;
        this.mError = error;
    }

    public void updateUi(Context context) {
        if (this.mLastResult == null) {
            int errorRes = this.mLastResultFailed ? this.mError : this.mComputing;
            this.mAppSize.setSummary(errorRes);
            this.mDataSize.setSummary(errorRes);
            this.mCacheSize.setSummary(errorRes);
            this.mTotalSize.setSummary(errorRes);
            return;
        }
        long codeSize = this.mLastResult.getCodeBytes();
        long j = 0;
        long dataSize = this.mDataCleared ? 0 : this.mLastResult.getDataBytes() - this.mLastResult.getCacheBytes();
        if (this.mLastCodeSize != codeSize) {
            this.mLastCodeSize = codeSize;
            this.mAppSize.setSummary(getSizeStr(context, codeSize));
        }
        if (this.mLastDataSize != dataSize) {
            this.mLastDataSize = dataSize;
            this.mDataSize.setSummary(getSizeStr(context, dataSize));
        }
        if (!(this.mDataCleared || this.mCachedCleared)) {
            j = this.mLastResult.getCacheBytes();
        }
        long cacheSize = j;
        if (this.mLastCacheSize != cacheSize) {
            this.mLastCacheSize = cacheSize;
            this.mCacheSize.setSummary(getSizeStr(context, cacheSize));
        }
        long totalSize = (codeSize + dataSize) + cacheSize;
        if (this.mLastTotalSize != totalSize) {
            this.mLastTotalSize = totalSize;
            this.mTotalSize.setSummary(getSizeStr(context, totalSize));
        }
    }

    public void setResult(AppStorageStats result) {
        this.mLastResult = result;
        this.mLastResultFailed = result == null;
    }

    public void setCacheCleared(boolean isCleared) {
        this.mCachedCleared = isCleared;
    }

    public void setDataCleared(boolean isCleared) {
        this.mDataCleared = isCleared;
    }

    public AppStorageStats getLastResult() {
        return this.mLastResult;
    }

    private String getSizeStr(Context context, long size) {
        return Formatter.formatFileSize(context, size);
    }
}
