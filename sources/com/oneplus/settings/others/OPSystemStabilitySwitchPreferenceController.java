package com.oneplus.settings.others;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.MasterSwitchController;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;

public class OPSystemStabilitySwitchPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnSwitchChangeListener {
    private static final String KEY_FROM_SETTINGS = "key_from_settings";
    private static final String KEY_NOTICES_TYPE = "op_legal_notices_type";
    private static final String KEY_SYSTEM_STABILITY = "system_stability";
    private static final int KEY_SYSTEM_STABILITY_TYPE = 6;
    private static final String OPLEGAL_NOTICES_ACTION = "android.oem.intent.action.OP_LEGAL";
    private MasterSwitchPreference mSwitch;
    private MasterSwitchController mSwitchController;

    public OPSystemStabilitySwitchPreferenceController(Context context) {
        super(context, KEY_SYSTEM_STABILITY);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitch = (MasterSwitchPreference) screen.findPreference(KEY_SYSTEM_STABILITY);
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public String getPreferenceKey() {
        return KEY_SYSTEM_STABILITY;
    }

    public void onResume() {
        if (isAvailable()) {
            try {
                MasterSwitchPreference masterSwitchPreference = this.mSwitch;
                boolean z = true;
                if (System.getInt(this.mContext.getContentResolver(), "oem_join_stability_plan_settings") != 1) {
                    z = false;
                }
                masterSwitchPreference.setChecked(z);
            } catch (SettingNotFoundException e) {
            }
            if (this.mSwitch != null) {
                this.mSwitchController = new MasterSwitchController(this.mSwitch);
                this.mSwitchController.setListener(this);
                this.mSwitchController.startListening();
            }
        }
    }

    public boolean onSwitchToggled(boolean isChecked) {
        int i;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        String str = "oem_join_stability_plan_settings";
        if (isChecked) {
            i = 1;
        } else {
            i = 0;
        }
        System.putInt(contentResolver, str, i);
        OPUtils.sendAppTracker("sys.stab", isChecked ? "agree_click" : "refuse_click");
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_SYSTEM_STABILITY.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent(OPLEGAL_NOTICES_ACTION);
        intent.putExtra(KEY_NOTICES_TYPE, 6);
        intent.putExtra(KEY_FROM_SETTINGS, true);
        this.mContext.startActivity(intent);
        return true;
    }
}
