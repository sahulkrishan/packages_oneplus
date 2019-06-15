package com.oneplus.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.widget.OPCustomFingeprintAnimVideoPreference;
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class OPCustomFingerprintAnimSettings extends InstrumentedPreferenceFragment implements OnPreferenceClickListener {
    private static final String KEY_PREFERENCE = "op_custom_fingerprint_anim";
    private Context mContext;
    private OPCustomFingeprintAnimVideoPreference mFingeprintAnimPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.op_custom_fingerprint_anim_settings);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.mContext = activity;
    }

    public void onResume() {
        super.onResume();
    }

    public boolean onPreferenceClick(Preference preference) {
        return false;
    }

    public int getMetricsCategory() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        this.mFingeprintAnimPreference = (OPCustomFingeprintAnimVideoPreference) getPreferenceScreen().findPreference(KEY_PREFERENCE);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (menu != null) {
            MenuItem searchItem = menu.add(0, 0, 0, R.string.oneplus_finger_print_anim_save);
            searchItem.setShowAsAction(2);
            searchItem.setOnMenuItemClickListener(new -$$Lambda$OPCustomFingerprintAnimSettings$sijG1CDCIS-z_FChWSJQB8d5ITY(this));
        }
    }

    public static /* synthetic */ boolean lambda$onCreateOptionsMenu$0(OPCustomFingerprintAnimSettings oPCustomFingerprintAnimSettings, MenuItem target) {
        oPCustomFingerprintAnimSettings.mFingeprintAnimPreference.saveSelectedAnim();
        oPCustomFingerprintAnimSettings.getActivity().onBackPressed();
        return true;
    }

    public void onDestroy() {
        super.onDestroy();
        int style = System.getIntForUser(this.mContext.getContentResolver(), OPConstants.OP_CUSTOM_UNLOCK_ANIMATION_STYLE_KEY, 0, -2);
        if (style == 0) {
            OPUtils.sendAnalytics("fod_effect", NotificationCompat.CATEGORY_STATUS, "1");
        } else if (style == 1) {
            OPUtils.sendAnalytics("fod_effect", NotificationCompat.CATEGORY_STATUS, "2");
        } else if (style == 2) {
            OPUtils.sendAnalytics("fod_effect", NotificationCompat.CATEGORY_STATUS, "3");
        } else if (style == 9) {
            OPUtils.sendAnalytics("fod_effect", NotificationCompat.CATEGORY_STATUS, "4");
        }
    }
}
