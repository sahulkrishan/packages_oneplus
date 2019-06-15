package com.android.settings.display;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import com.android.settings.PreviewSeekBarPreferenceFragment;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.display.DisplayDensityUtils;
import com.oneplus.settings.utils.OPDisplayDensityUtils;
import java.util.ArrayList;
import java.util.List;

public class ScreenZoomSettings extends PreviewSeekBarPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            Resources res = context.getResources();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = res.getString(R.string.screen_zoom_title);
            data.key = "screen_zoom_settings";
            data.screenTitle = res.getString(R.string.screen_zoom_title);
            data.keywords = res.getString(R.string.screen_zoom_keywords);
            List<SearchIndexableRaw> result = new ArrayList(1);
            result.add(data);
            return result;
        }
    };
    private int mDefaultDensity;
    private int[] mValues;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mActivityLayoutResId = R.layout.screen_zoom_activity;
        this.mPreviewSampleResIds = new int[]{R.layout.screen_zoom_preview_1, R.layout.screen_zoom_preview_settings};
        if (new DisplayDensityUtils(getContext()).getCurrentIndex() < 0) {
            this.mValues = new int[]{getResources().getDisplayMetrics().densityDpi};
            this.mEntries = new String[]{getString(DisplayDensityUtils.SUMMARY_DEFAULT)};
            this.mInitialIndex = 0;
            this.mDefaultDensity = densityDpi;
        } else {
            OPDisplayDensityUtils opDensity = new OPDisplayDensityUtils(getContext());
            this.mValues = opDensity.getValues();
            this.mEntries = opDensity.getEntries();
            this.mInitialIndex = opDensity.getCurrentIndex();
            this.mDefaultDensity = opDensity.getDefaultDensity();
        }
        getActivity().setTitle(R.string.screen_zoom_title);
    }

    /* Access modifiers changed, original: protected */
    public Configuration createConfig(Configuration origConfig, int index) {
        Configuration config = new Configuration(origConfig);
        config.densityDpi = this.mValues[index];
        return config;
    }

    /* Access modifiers changed, original: protected */
    public void commit() {
        int densityDpi = this.mValues[this.mCurrentIndex];
        if (densityDpi == this.mDefaultDensity) {
            DisplayDensityUtils.clearForcedDisplayDensity(0);
        } else {
            DisplayDensityUtils.setForcedDisplayDensity(0, densityDpi);
        }
    }

    public int getHelpResource() {
        return R.string.help_url_display_size;
    }

    public int getMetricsCategory() {
        return 339;
    }
}
