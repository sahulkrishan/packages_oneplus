package com.oneplus.settings.carcharger;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPCarChargerAutoOpenSpecifiedAppPreferenceController extends BasePreferenceController {
    private static final String KEY = "auto_open_specified_app";
    private Context mContext;
    private Preference mPreference;

    public OPCarChargerAutoOpenSpecifiedAppPreferenceController(Context context) {
        super(context, KEY);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        return OPUtils.isO2() ^ 1;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public CharSequence getSummary() {
        String selectPackageName = System.getString(this.mContext.getContentResolver(), "op_care_charger_auto_open_app");
        if (TextUtils.isEmpty(selectPackageName)) {
            return this.mContext.getString(R.string.oneplus_auto_open_app_none);
        }
        return OPUtils.getAppLabel(this.mContext, selectPackageName);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY.equals(preference.getKey())) {
            return false;
        }
        try {
            this.mContext.startActivity(new Intent("oneplus.intent.action.OP_CARCHARGER_OPEN_APP"));
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }
}
