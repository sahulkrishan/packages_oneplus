package com.android.settings.deviceinfo;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import com.android.settings.R;
import com.android.settings.utils.FileSizeFormatter;

public class StorageItemPreference extends Preference {
    private static final int PROGRESS_MAX = 100;
    private static final int UNINITIALIZED = -1;
    private ProgressBar mProgressBar;
    private int mProgressPercent;
    public int userHandle;

    public StorageItemPreference(Context context) {
        this(context, null);
    }

    public StorageItemPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mProgressPercent = -1;
        setLayoutResource(R.layout.storage_item);
        setSummary((int) R.string.memory_calculating_size);
    }

    public void setStorageSize(long size, long total) {
        setSummary((CharSequence) FileSizeFormatter.formatFileSize(getContext(), size, getGigabyteSuffix(getContext().getResources()), FileSizeFormatter.GIGABYTE_IN_BYTES));
        if (total == 0) {
            this.mProgressPercent = 0;
        } else {
            this.mProgressPercent = (int) ((100 * size) / total);
        }
        updateProgressBar();
    }

    /* Access modifiers changed, original: protected */
    public void updateProgressBar() {
        if (this.mProgressBar != null && this.mProgressPercent != -1) {
            this.mProgressBar.setMax(100);
            this.mProgressBar.setProgress(this.mProgressPercent);
        }
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        this.mProgressBar = (ProgressBar) view.findViewById(16908301);
        updateProgressBar();
        super.onBindViewHolder(view);
    }

    private static int getGigabyteSuffix(Resources res) {
        return res.getIdentifier("gigabyteShort", "string", "android");
    }
}
