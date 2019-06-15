package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings.System;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.android.settings.R;

public class ToggleFontSizePreferenceFragment extends PreviewSeekBarPreferenceFragment {
    private float[] mValues;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivityLayoutResId = R.layout.font_size_activity;
        int[] iArr = new int[1];
        int i = 0;
        iArr[0] = R.layout.font_size_preview;
        this.mPreviewSampleResIds = iArr;
        Resources res = getContext().getResources();
        ContentResolver resolver = getContext().getContentResolver();
        this.mEntries = res.getStringArray(R.array.entries_font_size);
        String[] strEntryValues = res.getStringArray(2130903124);
        this.mInitialIndex = fontSizeValueToIndex(System.getFloat(resolver, "font_scale", 1.0f), strEntryValues);
        this.mValues = new float[strEntryValues.length];
        while (i < strEntryValues.length) {
            this.mValues[i] = Float.parseFloat(strEntryValues[i]);
            i++;
        }
        getActivity().setTitle(R.string.title_font_size);
    }

    /* Access modifiers changed, original: protected */
    public Configuration createConfig(Configuration origConfig, int index) {
        Configuration config = new Configuration(origConfig);
        config.fontScale = this.mValues[index];
        return config;
    }

    /* Access modifiers changed, original: protected */
    public void commit() {
        if (getContext() != null) {
            System.putFloat(getContext().getContentResolver(), "font_scale", this.mValues[this.mCurrentIndex]);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_font_size;
    }

    public int getMetricsCategory() {
        return 340;
    }

    public static int fontSizeValueToIndex(float val, String[] indices) {
        float lastVal = Float.parseFloat(indices[0]);
        for (int i = 1; i < indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < ((thisVal - lastVal) * 0.5f) + lastVal) {
                return i - 1;
            }
            lastVal = thisVal;
        }
        return indices.length - 1;
    }
}
