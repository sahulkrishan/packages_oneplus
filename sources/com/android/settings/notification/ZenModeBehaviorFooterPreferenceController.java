package com.android.settings.notification;

import android.content.Context;
import android.net.Uri;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeBehaviorFooterPreferenceController extends AbstractZenModePreferenceController {
    protected static final String KEY = "footer_preference";
    private final int mTitleRes;

    public ZenModeBehaviorFooterPreferenceController(Context context, Lifecycle lifecycle, int titleRes) {
        super(context, "footer_preference", lifecycle);
        this.mTitleRes = titleRes;
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return "footer_preference";
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        preference.setTitle(getFooterText());
    }

    /* Access modifiers changed, original: protected */
    public String getFooterText() {
        if (!isDeprecatedZenMode(getZenMode())) {
            return this.mContext.getString(this.mTitleRes);
        }
        ZenModeConfig config = getZenModeConfig();
        if (config.manualRule != null && isDeprecatedZenMode(config.manualRule.zenMode)) {
            Uri id = config.manualRule.conditionId;
            if (config.manualRule.enabler == null) {
                return this.mContext.getString(R.string.zen_mode_qs_set_behavior);
            }
            if (!mZenModeConfigWrapper.getOwnerCaption(config.manualRule.enabler).isEmpty()) {
                return this.mContext.getString(R.string.zen_mode_app_set_behavior, new Object[]{appOwner});
            }
        }
        for (ZenRule automaticRule : config.automaticRules.values()) {
            if (automaticRule.isAutomaticActive() && isDeprecatedZenMode(automaticRule.zenMode) && automaticRule.component != null) {
                return this.mContext.getString(R.string.zen_mode_app_set_behavior, new Object[]{component.getPackageName()});
            }
        }
        return this.mContext.getString(R.string.zen_mode_unknown_app_set_behavior);
    }

    private boolean isDeprecatedZenMode(int zenMode) {
        switch (zenMode) {
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }
}
