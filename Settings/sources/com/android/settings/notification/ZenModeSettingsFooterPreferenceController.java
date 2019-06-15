package com.android.settings.notification;

import android.content.Context;
import android.net.Uri;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeSettingsFooterPreferenceController extends AbstractZenModePreferenceController {
    protected static final String KEY = "footer_preference";

    public ZenModeSettingsFooterPreferenceController(Context context, Lifecycle lifecycle) {
        super(context, "footer_preference", lifecycle);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return "footer_preference";
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setTitle((int) R.string.oneplus_zen_mode_footer_info);
    }

    /* Access modifiers changed, original: protected */
    public String getFooterText() {
        ZenModeConfig config = getZenModeConfig();
        String footerText = "";
        long latestEndTime = -1;
        if (config.manualRule != null) {
            Uri id = config.manualRule.conditionId;
            if (config.manualRule.enabler != null) {
                if (!mZenModeConfigWrapper.getOwnerCaption(config.manualRule.enabler).isEmpty()) {
                    footerText = this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule_app, new Object[]{appOwner});
                }
            } else if (id == null) {
                return this.mContext.getString(R.string.zen_mode_settings_dnd_manual_indefinite);
            } else {
                latestEndTime = mZenModeConfigWrapper.parseManualRuleTime(id);
                if (latestEndTime > 0) {
                    CharSequence formattedTime = mZenModeConfigWrapper.getFormattedTime(latestEndTime, this.mContext.getUserId());
                    footerText = this.mContext.getString(R.string.zen_mode_settings_dnd_manual_end_time, new Object[]{formattedTime});
                }
            }
        }
        for (ZenRule automaticRule : config.automaticRules.values()) {
            if (automaticRule.isAutomaticActive()) {
                if (mZenModeConfigWrapper.isTimeRule(automaticRule.conditionId)) {
                    long endTime = mZenModeConfigWrapper.parseAutomaticRuleEndTime(automaticRule.conditionId);
                    if (endTime > latestEndTime) {
                        latestEndTime = endTime;
                        footerText = this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule, new Object[]{automaticRule.name});
                    }
                } else {
                    return this.mContext.getString(R.string.zen_mode_settings_dnd_automatic_rule, new Object[]{automaticRule.name});
                }
            }
        }
        return footerText;
    }
}
