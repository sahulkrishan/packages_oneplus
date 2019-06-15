package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.provider.Settings.System;
import android.util.Log;
import com.android.settings.R;
import com.oneplus.settings.SettingsBaseApplication;
import com.oneplus.settings.utils.OPUtils;

public class OPOTACondition extends Condition {
    private static final String BOOT_BROADCAST = "android.intent.action.BOOT_COMPLETED";
    private static final String HAS_NEW_VERSION_TO_UPDATE = "has_new_version_to_update";
    private static final String OEM_BOOT_BROADCAST = "com.oem.intent.action.BOOT_COMPLETED";
    private static final String STRONG_PROMPT_OTA = "strong_prompt_ota";
    private static final String TAG = "OPOTACondition";

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ((OPOTACondition.BOOT_BROADCAST.equals(intent.getAction()) || OPOTACondition.OEM_BOOT_BROADCAST.equals(intent.getAction())) && OPOTACondition.activeRefresh() && ConditionManager.get(context).getCondition(OPOTACondition.class) != null) {
                ((OPOTACondition) ConditionManager.get(context).getCondition(OPOTACondition.class)).setSilenced();
            }
        }
    }

    public OPOTACondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        setActive(activeRefresh());
    }

    private static boolean activeRefresh() {
        boolean strongPromptOTA = false;
        if (SettingsBaseApplication.mApplication == null) {
            return false;
        }
        boolean systemHasUpdate = System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), "has_new_version_to_update", 0) == 1;
        if (System.getInt(SettingsBaseApplication.mApplication.getContentResolver(), STRONG_PROMPT_OTA, 0) == 100) {
            strongPromptOTA = true;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("systemHasUpdate = ");
        stringBuilder.append(systemHasUpdate);
        stringBuilder.append("    strongPromptOTA = ");
        stringBuilder.append(strongPromptOTA);
        Log.v(str, stringBuilder.toString());
        return systemHasUpdate;
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.oneplus_ota_available);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.oneplus_ota_available);
    }

    public CharSequence[] getActions() {
        return new CharSequence[0];
    }

    public void onPrimaryClick() {
        this.mManager.getContext().startActivity(new Intent("oneplus.intent.action.CheckUpdate").addFlags(268435456));
    }

    public void onActionClick(int index) {
    }

    public int getMetricsConstant() {
        return OPUtils.ONEPLUS_METRICSLOGGER;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_system_update);
    }
}
