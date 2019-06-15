package com.oneplus.settings;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.util.OpFeatures;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class OPStatusBarCustomizeIconSettings extends SettingsPreferenceFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new StatusBarCustomizeIndexProvider();
    private static final String TAG = "OPStatusBarCustomizeSettings";
    private Context mContext;

    private static class StatusBarCustomizeIndexProvider extends BaseSearchIndexProvider {
        boolean mIsPrimary;

        public StatusBarCustomizeIndexProvider() {
            this.mIsPrimary = UserHandle.myUserId() == 0;
        }

        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            List<SearchIndexableResource> result = new ArrayList();
            if (!this.mIsPrimary) {
                return result;
            }
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.op_statusbar_customize_icon_settings;
            result.add(sir);
            return result;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_statusbar_customize_icon_settings);
        this.mContext = getActivity();
        ((SettingsActivity) getActivity()).setTitle(this.mContext.getResources().getString(R.string.statusbar_icon_manager));
        customizePreferences();
    }

    private void customizePreferences() {
        boolean hideVowifiiconOnStatusbar = OpFeatures.isSupport(new int[]{85});
        boolean hideVoLteiconOnStatusbar = OpFeatures.isSupport(new int[]{85});
        boolean hideVolumeIconOnStatusbar = true ^ OpFeatures.isSupport(new int[]{42});
        if (hideVowifiiconOnStatusbar) {
            removePreference("vowifi");
        }
        if (hideVoLteiconOnStatusbar) {
            removePreference("volte");
        }
        if (hideVolumeIconOnStatusbar) {
            removePreference("volume");
        }
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }
}
