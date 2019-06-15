package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.SearchIndexableResource;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import com.android.settings.R;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.actionbar.SearchMenuController;
import com.android.settings.support.actionbar.HelpResourceProvider;
import java.util.Arrays;
import java.util.List;

public final class MagnificationPreferenceFragment extends DashboardFragment {
    private static final String MAGNIFICATION_COMPONENT_ID = "com.android.server.accessibility.MagnificationController";
    @VisibleForTesting
    static final int OFF = 0;
    @VisibleForTesting
    static final int ON = 1;
    private static final String PREFERENCE_TITLE_KEY = "magnification_preference_screen_title";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.accessibility_magnification_settings;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        /* Access modifiers changed, original: protected */
        public boolean isPageSearchEnabled(Context context) {
            return MagnificationPreferenceFragment.isApplicable(context.getResources());
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> keys = super.getNonIndexableKeys(context);
            keys.add(MagnificationPreferenceFragment.PREFERENCE_TITLE_KEY);
            return keys;
        }
    };
    private static final String TAG = "MagnificationPreferenceFragment";
    private boolean mLaunchedFromSuw = false;

    public int getMetricsCategory() {
        return 922;
    }

    /* Access modifiers changed, original: protected */
    public String getLogTag() {
        return TAG;
    }

    public int getHelpResource() {
        return R.string.help_url_magnification;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.accessibility_magnification_settings;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle args = getArguments();
        if (args != null && args.containsKey("from_suw")) {
            this.mLaunchedFromSuw = args.getBoolean("from_suw");
        }
        ((MagnificationGesturesPreferenceController) use(MagnificationGesturesPreferenceController.class)).setIsFromSUW(this.mLaunchedFromSuw);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mLaunchedFromSuw) {
            preference.setFragment(ToggleScreenMagnificationPreferenceFragmentForSetupWizard.class.getName());
            Bundle args = preference.getExtras();
            args.putInt(HelpResourceProvider.HELP_URI_RESOURCE_KEY, 0);
            args.putBoolean(SearchMenuController.NEED_SEARCH_ICON_IN_ACTION_BAR, false);
        }
        return super.onPreferenceTreeClick(preference);
    }

    static CharSequence getConfigurationWarningStringForSecureSettingsKey(String key, Context context) {
        if (!"accessibility_display_magnification_navbar_enabled".equals(key) || Secure.getInt(context.getContentResolver(), "accessibility_display_magnification_navbar_enabled", 0) == 0) {
            return null;
        }
        AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
        String assignedId = Secure.getString(context.getContentResolver(), "accessibility_button_target_component");
        if (!(TextUtils.isEmpty(assignedId) || MAGNIFICATION_COMPONENT_ID.equals(assignedId))) {
            ComponentName assignedComponentName = ComponentName.unflattenFromString(assignedId);
            List<AccessibilityServiceInfo> activeServices = am.getEnabledAccessibilityServiceList(-1);
            int serviceCount = activeServices.size();
            for (int i = 0; i < serviceCount; i++) {
                if (((AccessibilityServiceInfo) activeServices.get(i)).getComponentName().equals(assignedComponentName)) {
                    return context.getString(R.string.accessibility_screen_magnification_navbar_configuration_warning, new Object[]{((AccessibilityServiceInfo) activeServices.get(i)).getResolveInfo().loadLabel(context.getPackageManager())});
                }
            }
        }
        return null;
    }

    static boolean isChecked(ContentResolver contentResolver, String settingsKey) {
        return Secure.getInt(contentResolver, settingsKey, 0) == 1;
    }

    static boolean setChecked(ContentResolver contentResolver, String settingsKey, boolean isChecked) {
        return Secure.putInt(contentResolver, settingsKey, isChecked);
    }

    static boolean isApplicable(Resources res) {
        return res.getBoolean(17957029);
    }
}
