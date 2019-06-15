package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;

public class ProgressCategory extends ProgressCategoryBase {
    private int mEmptyTextRes;
    private boolean mNoDeviceFoundAdded;
    private Preference mNoDeviceFoundPreference;
    private boolean mProgress = false;

    public ProgressCategory(Context context) {
        super(context);
        setLayoutResource(R.layout.preference_progress_category);
    }

    public ProgressCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.preference_progress_category);
    }

    public ProgressCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutResource(R.layout.preference_progress_category);
    }

    public ProgressCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setLayoutResource(R.layout.preference_progress_category);
    }

    public void setEmptyTextRes(int emptyTextRes) {
        this.mEmptyTextRes = emptyTextRes;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        View progressBar = view.findViewById(R.id.scanning_progress);
        boolean noDeviceFound = getPreferenceCount() == 0 || (getPreferenceCount() == 1 && getPreference(0) == this.mNoDeviceFoundPreference);
        progressBar.setVisibility(this.mProgress ? 0 : 8);
        if (this.mProgress || !noDeviceFound) {
            if (this.mNoDeviceFoundAdded) {
                removePreference(this.mNoDeviceFoundPreference);
                this.mNoDeviceFoundAdded = false;
            }
        } else if (!this.mNoDeviceFoundAdded) {
            if (this.mNoDeviceFoundPreference == null) {
                this.mNoDeviceFoundPreference = new Preference(getContext());
                this.mNoDeviceFoundPreference.setLayoutResource(R.layout.preference_empty_list);
                this.mNoDeviceFoundPreference.setTitle(this.mEmptyTextRes);
                this.mNoDeviceFoundPreference.setSelectable(false);
            }
            addPreference(this.mNoDeviceFoundPreference);
            this.mNoDeviceFoundAdded = true;
        }
    }

    public void setProgress(boolean progressOn) {
        this.mProgress = progressOn;
        notifyChanged();
    }
}
