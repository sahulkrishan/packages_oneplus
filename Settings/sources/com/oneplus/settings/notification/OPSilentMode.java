package com.oneplus.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchIndexableResource;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.notification.SettingPref;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OPSilentMode extends SettingsPreferenceFragment implements Indexable {
    private static final String KEY_MEDIA_RING = "media_ring";
    private static final String KEY_NOISE_TIPS = "noise_tips";
    private static final SettingPref[] PREFS = new SettingPref[]{PREF_MEDIA_RING_SETTING, PREF_NOISE_TIPS_SETTING};
    private static final SettingPref PREF_MEDIA_RING_SETTING = new SettingPref(2, KEY_MEDIA_RING, "oem_zen_media_switch", 0, new int[0]);
    private static final SettingPref PREF_NOISE_TIPS_SETTING = new SettingPref(2, KEY_NOISE_TIPS, "oem_vibrate_under_silent", 0, new int[0]);
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_silent_mode;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }

        public List<String> getNonIndexableKeys(Context context) {
            List<String> result = new ArrayList();
            if (OPUtils.isSupportSocTriState()) {
                result.add(OPSilentMode.KEY_NOISE_TIPS);
            }
            return result;
        }
    };
    private PrefSettingsObserver mPrefSettingsObserver = new PrefSettingsObserver();

    private final class PrefSettingsObserver extends ContentObserver {
        public PrefSettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            ContentResolver cr = OPSilentMode.this.getContentResolver();
            if (register) {
                for (SettingPref pref : OPSilentMode.PREFS) {
                    cr.registerContentObserver(pref.getUri(), false, this);
                }
                return;
            }
            cr.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            for (SettingPref pref : OPSilentMode.PREFS) {
                if (pref.getUri().equals(uri)) {
                    pref.update(OPSilentMode.this.getActivity());
                    return;
                }
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_silent_mode);
        if (OPUtils.isSupportSocTriState()) {
            removePreference(KEY_NOISE_TIPS);
        }
    }

    public void onResume() {
        super.onResume();
        for (SettingPref pref : PREFS) {
            pref.init(this);
        }
        this.mPrefSettingsObserver.register(true);
    }

    public void onPause() {
        this.mPrefSettingsObserver.register(false);
        super.onPause();
    }

    public int getMetricsCategory() {
        return 76;
    }
}
