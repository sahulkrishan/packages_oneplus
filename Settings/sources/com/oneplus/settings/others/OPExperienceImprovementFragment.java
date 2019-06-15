package com.oneplus.settings.others;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.utils.OPPreferenceDividerLine;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPExperienceImprovementFragment extends DashboardFragment {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_experience_improvement_programs;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            return super.getNonIndexableKeys(context);
        }
    };
    private static final String TAG = "OPExperienceImprovementFragment";

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.op_experience_improvement_programs;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        OPReceiveNotificationsSwitchPreferenceController mOPOpReceiveNotificationsSwitchPreferenceController = new OPReceiveNotificationsSwitchPreferenceController(context);
        OPUserExperienceSwitchPreferenceController mOPUserExperienceSwitchPreferenceController = new OPUserExperienceSwitchPreferenceController(context);
        OPSystemStabilitySwitchPreferenceController mOPSystemStabilitySwitchPreferenceController = new OPSystemStabilitySwitchPreferenceController(context);
        controllers.add(new OPPreferenceDividerLine(context));
        getLifecycle().addObserver(mOPOpReceiveNotificationsSwitchPreferenceController);
        getLifecycle().addObserver(mOPUserExperienceSwitchPreferenceController);
        getLifecycle().addObserver(mOPSystemStabilitySwitchPreferenceController);
        controllers.add(mOPOpReceiveNotificationsSwitchPreferenceController);
        controllers.add(mOPUserExperienceSwitchPreferenceController);
        controllers.add(mOPSystemStabilitySwitchPreferenceController);
        return controllers;
    }
}
