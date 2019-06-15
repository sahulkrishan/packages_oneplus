package com.android.settings;

import android.content.Context;
import android.support.v7.preference.PreferenceCategory;
import android.util.AttributeSet;

public abstract class ProgressCategoryBase extends PreferenceCategory {
    public abstract void setProgress(boolean z);

    public ProgressCategoryBase(Context context) {
        super(context);
    }

    public ProgressCategoryBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressCategoryBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ProgressCategoryBase(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
