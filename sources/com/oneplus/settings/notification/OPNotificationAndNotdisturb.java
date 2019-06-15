package com.oneplus.settings.notification;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceGroup;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPNotificationAndNotdisturb extends SettingsPreferenceFragment implements Indexable {
    private static final String KEY_DO_NOT_DISTURB_SETTINGS = "do_not_disturb_settings";
    private static final String KEY_MODE_SETTINGS_CATEGORY = "mode_settings_category";
    private static final String KEY_RING_SETTINGS = "ring_settings";
    private static final String KEY_SILENT_SETTINGS = "silent_settings";
    private static final String KEY_VIBRATION_MODE = "vibration_settings";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new NotificationAndNotdisturbSearchIndexProvider();
    private static boolean isSupportSocTriState;
    private Preference mDonotdisturbSetings;
    private Preference mRingSettings;
    private Preference mSilentSettings;
    private Preference mVibrationSettings;

    private static class NotificationAndNotdisturbSearchIndexProvider extends BaseSearchIndexProvider {
        boolean mIsPrimary;

        public NotificationAndNotdisturbSearchIndexProvider() {
            this.mIsPrimary = UserHandle.myUserId() == 0;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (!this.mIsPrimary) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.op_notification_not_disturb;
            result.add(sir);
            return result;
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (OPUtils.isGuestMode()) {
                result.add(OPNotificationAndNotdisturb.KEY_RING_SETTINGS);
            }
            if (OPUtils.isSupportSocTriState()) {
                result.add(OPNotificationAndNotdisturb.KEY_DO_NOT_DISTURB_SETTINGS);
            } else {
                result.add(OPNotificationAndNotdisturb.KEY_VIBRATION_MODE);
            }
            return result;
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_notification_not_disturb);
        this.mRingSettings = findPreference(KEY_RING_SETTINGS);
        this.mSilentSettings = findPreference(KEY_SILENT_SETTINGS);
        this.mVibrationSettings = findPreference(KEY_VIBRATION_MODE);
        this.mDonotdisturbSetings = findPreference(KEY_DO_NOT_DISTURB_SETTINGS);
        PreferenceGroup modeSettingsCategory = (PreferenceGroup) findPreference(KEY_MODE_SETTINGS_CATEGORY);
        if (OPUtils.isGuestMode()) {
            modeSettingsCategory.removePreference(findPreference(KEY_RING_SETTINGS));
        }
        if (OPUtils.isSupportSocTriState()) {
            modeSettingsCategory.removePreference(this.mDonotdisturbSetings);
            return;
        }
        getActivity().getActionBar().setTitle(getResources().getString(R.string.alertslider_settings));
        modeSettingsCategory.removePreference(this.mVibrationSettings);
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
