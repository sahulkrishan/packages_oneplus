package com.android.settings.applications;

import android.app.Activity;
import android.content.Context;
import android.provider.SearchIndexableResource;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.applications.assist.DefaultAssistPreferenceController;
import com.android.settings.applications.defaultapps.DefaultBrowserPreferenceController;
import com.android.settings.applications.defaultapps.DefaultEmergencyPreferenceController;
import com.android.settings.applications.defaultapps.DefaultHomePreferenceController;
import com.android.settings.applications.defaultapps.DefaultPhonePreferenceController;
import com.android.settings.applications.defaultapps.DefaultSmsPreferenceController;
import com.android.settings.applications.defaultapps.DefaultWorkBrowserPreferenceController;
import com.android.settings.applications.defaultapps.DefaultWorkPhonePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.widget.PreferenceCategoryController;
import com.android.settingslib.core.AbstractPreferenceController;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.defaultapp.DefaultAppLogic;
import com.oneplus.settings.defaultapp.controller.DefaultCameraController;
import com.oneplus.settings.defaultapp.controller.DefaultGalleryController;
import com.oneplus.settings.defaultapp.controller.DefaultMailController;
import com.oneplus.settings.defaultapp.controller.DefaultMusicController;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DefaultAppSettings extends DashboardFragment {
    private static final String KEY_ASSIST_VOICE_INPUT = "assist_and_voice_input";
    private static final String KEY_DEFAULT_WORK_CATEGORY = "work_app_defaults";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.app_default_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(DefaultAppSettings.KEY_ASSIST_VOICE_INPUT);
            keys.add(DefaultWorkPhonePreferenceController.KEY);
            keys.add(DefaultWorkBrowserPreferenceController.KEY);
            return keys;
        }

        public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
            return DefaultAppSettings.buildPreferenceControllers(context);
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    static final String TAG = "DefaultAppSettings";

    static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final DefaultBrowserPreferenceController mDefaultBrowserPreferenceController = new DefaultBrowserPreferenceController(this.mContext);
        private final DefaultPhonePreferenceController mDefaultPhonePreferenceController = new DefaultPhonePreferenceController(this.mContext);
        private final DefaultSmsPreferenceController mDefaultSmsPreferenceController = new DefaultSmsPreferenceController(this.mContext);
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening) {
                this.mSummaryLoader.setSummary(this, this.mContext.getString(R.string.op_default_apps_summary));
            }
        }

        private CharSequence concatSummaryText(CharSequence summary1, CharSequence summary2) {
            if (TextUtils.isEmpty(summary1)) {
                return summary2;
            }
            if (TextUtils.isEmpty(summary2)) {
                return summary1;
            }
            return this.mContext.getString(R.string.join_many_items_middle, new Object[]{summary1, summary2});
        }
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.app_default_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        return buildPreferenceControllers(context);
    }

    public int getMetricsCategory() {
        return 130;
    }

    private static List<AbstractPreferenceController> buildPreferenceControllers(Context context) {
        List<AbstractPreferenceController> controllers = new ArrayList();
        List<AbstractPreferenceController> workControllers = new ArrayList();
        workControllers.add(new DefaultWorkPhonePreferenceController(context));
        workControllers.add(new DefaultWorkBrowserPreferenceController(context));
        controllers.addAll(workControllers);
        controllers.add(new PreferenceCategoryController(context, KEY_DEFAULT_WORK_CATEGORY).setChildren(workControllers));
        controllers.add(new DefaultAssistPreferenceController(context, KEY_ASSIST_VOICE_INPUT, false));
        controllers.add(new DefaultBrowserPreferenceController(context));
        controllers.add(new DefaultPhonePreferenceController(context));
        controllers.add(new DefaultSmsPreferenceController(context));
        controllers.add(new DefaultEmergencyPreferenceController(context));
        controllers.add(new DefaultHomePreferenceController(context));
        DefaultAppLogic.getInstance(SettingsBaseApplication.mApplication).initDefaultAppSettings();
        controllers.add(new DefaultCameraController(context));
        controllers.add(new DefaultGalleryController(context));
        controllers.add(new DefaultMusicController(context));
        controllers.add(new DefaultMailController(context));
        return controllers;
    }
}
