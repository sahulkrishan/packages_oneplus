package com.oneplus.settings.others;

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
import com.oneplus.settings.utils.OPConstants;
import com.oneplus.settings.utils.OPUtils;

public class OPReceiveNotificationsSwitchPreferenceController extends BasePreferenceController implements LifecycleObserver, OnResume, OnSwitchChangeListener {
    private static final String KEY_FROM_SETTINGS = "key_from_settings";
    private static final String KEY_NOTICES_TYPE = "op_legal_notices_type";
    private static final String KEY_RECEIVE_NOTIFICATIONS = "receive_notifications";
    private static final int KEY_RECEIVE_NOTIFICATIONS_TYPE = 7;
    private static final String OPLEGAL_NOTICES_ACTION = "android.oem.intent.action.OP_LEGAL";
    private static final String PUSH_SWITCH_ACTION = "net.oneplus.push.action.SWITCH_CHANGED";
    private MasterSwitchPreference mSwitch;
    private MasterSwitchController mSwitchController;

    public OPReceiveNotificationsSwitchPreferenceController(Context context) {
        super(context, KEY_RECEIVE_NOTIFICATIONS);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitch = (MasterSwitchPreference) screen.findPreference(KEY_RECEIVE_NOTIFICATIONS);
    }

    public int getAvailabilityStatus() {
        return OPUtils.isAppExist(this.mContext, OPConstants.PACKAGENAME_OP_PUSH) ^ 1;
    }

    public String getPreferenceKey() {
        return KEY_RECEIVE_NOTIFICATIONS;
    }

    public void onResume() {
        if (isAvailable()) {
            try {
                MasterSwitchPreference masterSwitchPreference = this.mSwitch;
                boolean z = true;
                if (System.getInt(this.mContext.getContentResolver(), "oem_receive_notifications") != 1) {
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
        System.putInt(this.mContext.getContentResolver(), "oem_receive_notifications", isChecked);
        OPUtils.sendAppTracker("push.noti", isChecked ? "agree_click" : "refuse_click");
        Intent intent = new Intent(PUSH_SWITCH_ACTION);
        intent.setPackage(OPConstants.PACKAGENAME_OP_PUSH);
        intent.putExtra("oem_receive_notifications", isChecked);
        intent.addFlags(268435456);
        this.mContext.sendBroadcast(intent);
        return true;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_RECEIVE_NOTIFICATIONS.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent(OPLEGAL_NOTICES_ACTION);
        intent.putExtra(KEY_NOTICES_TYPE, 7);
        intent.putExtra(KEY_FROM_SETTINGS, true);
        this.mContext.startActivity(intent);
        return true;
    }
}
