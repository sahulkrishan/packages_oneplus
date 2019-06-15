package com.android.settings.applications.assist;

import android.content.Context;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.gestures.AssistGestureSettingsPreferenceController;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ManageAssist extends DashboardFragment {
    private static final String KEY_ASSIST = "gesture_assist_application";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.manage_assist;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return ManageAssist.buildPreferenceControllers(context, null);
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(ManageAssist.KEY_ASSIST);
            return keys;
        }
    };
    private static final String TAG = "ManageAssist";

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.manage_assist;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context, getLifecycle());
    }

    public int getMetricsCategory() {
        return 201;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        ((AssistGestureSettingsPreferenceController) use(AssistGestureSettingsPreferenceController.class)).setAssistOnly(true);
    }

    public void onResume() {
        super.onResume();
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.assist_footer);
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context, Lifecycle lifecycle) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        controllers.add(new DefaultAssistPreferenceController(context, "default_assist", true));
        controllers.add(new AssistContextPreferenceController(context, lifecycle));
        controllers.add(new AssistScreenshotPreferenceController(context, lifecycle));
        controllers.add(new AssistFlashScreenPreferenceController(context, lifecycle));
        controllers.add(new DefaultVoiceInputPreferenceController(context, lifecycle));
        return controllers;
    }
}
