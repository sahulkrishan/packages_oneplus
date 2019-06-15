package com.oneplus.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import java.util.Arrays;
import java.util.List;

public class OPRingPattern extends SettingsPreferenceFragment implements Indexable {
    private static final String KEY_VIBRATE_WHEN_RINGING = "vibrate_when_ringing";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            new SearchIndexableResource(context).xmlResId = R.xml.op_ring_pattern;
            return Arrays.asList(new SearchIndexableResource[]{sir});
        }
    };
    private static final String TAG = "OPRingPattern";
    private final SettingsObserver mSettingsObserver = new SettingsObserver();
    private TwoStatePreference mVibrateWhenRinging;
    private boolean mVoiceCapable;

    private final class SettingsObserver extends ContentObserver {
        private final Uri VIBRATE_WHEN_RINGING_URI = System.getUriFor(OPRingPattern.KEY_VIBRATE_WHEN_RINGING);

        public SettingsObserver() {
            super(new Handler());
        }

        public void register(boolean register) {
            ContentResolver cr = OPRingPattern.this.getContentResolver();
            if (register) {
                cr.registerContentObserver(this.VIBRATE_WHEN_RINGING_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.VIBRATE_WHEN_RINGING_URI.equals(uri)) {
                OPRingPattern.this.updateVibrateWhenRinging();
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.op_ring_pattern);
        this.mVoiceCapable = Utils.isVoiceCapable(getActivity());
        initVibrateWhenRinging();
        this.mSettingsObserver.register(true);
    }

    public void onResume() {
        super.onResume();
        if (this.mVibrateWhenRinging != null) {
            TwoStatePreference twoStatePreference = this.mVibrateWhenRinging;
            boolean z = false;
            if (System.getInt(getContentResolver(), KEY_VIBRATE_WHEN_RINGING, 0) != 0) {
                z = true;
            }
            twoStatePreference.setChecked(z);
        }
    }

    public void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        this.mSettingsObserver.register(false);
    }

    public int getMetricsCategory() {
        return 76;
    }

    private void initVibrateWhenRinging() {
        this.mVibrateWhenRinging = (TwoStatePreference) findPreference(KEY_VIBRATE_WHEN_RINGING);
        if (this.mVibrateWhenRinging == null) {
            Log.i(TAG, "Preference not found: vibrate_when_ringing");
        } else if (this.mVoiceCapable) {
            this.mVibrateWhenRinging.setPersistent(false);
            updateVibrateWhenRinging();
            this.mVibrateWhenRinging.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    return System.putInt(OPRingPattern.this.getContentResolver(), OPRingPattern.KEY_VIBRATE_WHEN_RINGING, ((Boolean) newValue).booleanValue());
                }
            });
        } else {
            getPreferenceScreen().removePreference(this.mVibrateWhenRinging);
            this.mVibrateWhenRinging = null;
        }
    }

    private void updateVibrateWhenRinging() {
        if (this.mVibrateWhenRinging != null) {
            TwoStatePreference twoStatePreference = this.mVibrateWhenRinging;
            boolean z = false;
            if (System.getInt(getContentResolver(), KEY_VIBRATE_WHEN_RINGING, 0) != 0) {
                z = true;
            }
            twoStatePreference.setChecked(z);
        }
    }
}
