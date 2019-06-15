package com.oneplus.settings;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.widget.MasterSwitchController;
import com.android.settings.widget.MasterSwitchPreference;
import com.android.settings.widget.SwitchWidgetController.OnSwitchChangeListener;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;

public class OPSecurityDetectionSwitchPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnSwitchChangeListener {
    private static final String EVENT_TRACKER = "sec_recommend";
    private static final String KEY_OP_APP_SECURITY_RECOMMEND = "op_app_security_recommend_setting";
    private static final String KEY_SECURITY_DETECTION = "security_detection";
    public static final String PREF_KEY_OP_APP_SECURITY_RECOMMEND = "op_app_security_recommend";
    private MasterSwitchPreference mSwitch;
    private MasterSwitchController mSwitchController;

    public OPSecurityDetectionSwitchPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, KEY_SECURITY_DETECTION);
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitch = (MasterSwitchPreference) screen.findPreference(KEY_SECURITY_DETECTION);
    }

    public int getAvailabilityStatus() {
        boolean state = OPUtils.isO2() || !OPUtils.isSupportAppSecureRecommd();
        if (state) {
            return 3;
        }
        return 0;
    }

    public String getPreferenceKey() {
        return KEY_SECURITY_DETECTION;
    }

    public void onResume() {
        if (isAvailable() && this.mSwitch != null) {
            boolean z = true;
            int state = Secure.getIntForUser(this.mContext.getContentResolver(), KEY_OP_APP_SECURITY_RECOMMEND, 1, -2);
            MasterSwitchPreference masterSwitchPreference = this.mSwitch;
            if (state != 1) {
                z = false;
            }
            masterSwitchPreference.setChecked(z);
            this.mSwitchController = new MasterSwitchController(this.mSwitch);
            this.mSwitchController.setListener(this);
            this.mSwitchController.startListening();
        }
    }

    public boolean onSwitchToggled(boolean isChecked) {
        int val = isChecked;
        Secure.putIntForUser(this.mContext.getContentResolver(), KEY_OP_APP_SECURITY_RECOMMEND, val, -2);
        OPUtils.sendAppTracker(EVENT_TRACKER, val);
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_SECURITY_DETECTION.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent();
        intent.setAction("com.oneplus.action.APP_SECURITY_RECOMMEND_SETTINGS");
        this.mContext.startActivity(intent);
        return true;
    }
}
