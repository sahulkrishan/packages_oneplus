package com.android.settings.deviceinfo.storage;

import android.content.Context;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.deviceinfo.StorageVolumeProvider;

public class StorageSummaryDonutPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private StorageSummaryDonutPreference mSummary;
    private long mTotalBytes;
    private long mUsedBytes;

    public StorageSummaryDonutPreferenceController(Context context) {
        super(context);
    }

    public void displayPreference(PreferenceScreen screen) {
        this.mSummary = (StorageSummaryDonutPreference) screen.findPreference("pref_summary");
        this.mSummary.setEnabled(true);
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        StorageSummaryDonutPreference summary = (StorageSummaryDonutPreference) preference;
        BytesResult result = Formatter.formatBytes(this.mContext.getResources(), this.mUsedBytes, 0);
        summary.setTitle(TextUtils.expandTemplate(this.mContext.getText(R.string.storage_size_large_alternate), new CharSequence[]{result.value, result.units}));
        summary.setSummary((CharSequence) this.mContext.getString(R.string.storage_volume_total, new Object[]{Formatter.formatShortFileSize(this.mContext, this.mTotalBytes)}));
        summary.setPercent(this.mUsedBytes, this.mTotalBytes);
        summary.setEnabled(true);
    }

    public void invalidateData() {
        if (this.mSummary != null) {
            updateState(this.mSummary);
        }
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return "pref_summary";
    }

    public void updateBytes(long used, long total) {
        this.mUsedBytes = used;
        this.mTotalBytes = total;
        invalidateData();
    }

    public void updateSizes(StorageVolumeProvider svp, VolumeInfo volume) {
        long sharedDataSize = volume.getPath().getTotalSpace();
        long totalSize = svp.getPrimaryStorageSize();
        if (totalSize <= 0) {
            totalSize = sharedDataSize;
        }
        updateBytes(totalSize - volume.getPath().getFreeSpace(), totalSize);
    }
}
