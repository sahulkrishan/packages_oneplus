package com.android.settings.system;

import android.content.Context;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.R;
import com.android.settings.backup.BackupSettingsActivityPreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPPreferenceDividerLine;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SystemDashboardFragment extends DashboardFragment {
    private static final String KEY_OP_RECEIVE_NOTIFICATIONS = "onepus_receive_notifications";
    private static final String KEY_RESET = "reset_dashboard";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.system_dashboard_fragment;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(new BackupSettingsActivityPreferenceController(context).getPreferenceKey());
            keys.add(SystemDashboardFragment.KEY_RESET);
            if (OPUtils.isAppExist(context, OPConstants.PACKAGENAME_OP_PUSH)) {
                keys.add(SystemDashboardFragment.KEY_OP_RECEIVE_NOTIFICATIONS);
            }
            return keys;
        }
    };
    private static final String TAG = "SystemDashboardFrag";

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        PreferenceScreen screen = getPreferenceScreen();
        if (getVisiblePreferenceCount(screen) == screen.getInitialExpandedChildrenCount() + 1) {
            screen.setInitialExpandedChildrenCount(Integer.MAX_VALUE);
        }
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new OPOTGPreferenceController(context, getLifecycle()));
        controllers.add(new OPPreferenceDividerLine(context));
        return controllers;
    }

    public int getMetricsCategory() {
        return 744;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.system_dashboard_fragment;
    }

    public int getHelpResource() {
        return R.string.help_url_system_dashboard;
    }

    private int getVisiblePreferenceCount(PreferenceGroup group) {
        int visibleCount = 0;
        for (int i = 0; i < group.getPreferenceCount(); i++) {
            Preference preference = group.getPreference(i);
            if (preference instanceof PreferenceGroup) {
                visibleCount += getVisiblePreferenceCount((PreferenceGroup) preference);
            } else if (preference.isVisible()) {
                visibleCount++;
            }
        }
        return visibleCount;
    }
}
