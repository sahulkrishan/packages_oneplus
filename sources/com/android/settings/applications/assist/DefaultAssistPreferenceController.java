package com.android.settings.applications.assist;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.service.voice.VoiceInteractionServiceInfo;
import android.support.annotation.VisibleForTesting;
import com.android.internal.app.AssistUtils;
import com.android.settings.R;
import com.android.settings.applications.defaultapps.DefaultAppPreferenceController;
import com.android.settingslib.applications.DefaultAppInfo;
import com.oneplus.settings.utils.OPUtils;
import java.util.List;

public class DefaultAssistPreferenceController extends DefaultAppPreferenceController {
    private final AssistUtils mAssistUtils;
    private final String mPrefKey;
    private final boolean mShowSetting;

    public DefaultAssistPreferenceController(Context context, String prefKey, boolean showSetting) {
        super(context);
        this.mPrefKey = prefKey;
        this.mShowSetting = showSetting;
        this.mAssistUtils = new AssistUtils(context);
    }

    /* Access modifiers changed, original: protected */
    public Intent getSettingIntent(DefaultAppInfo info) {
        if (!this.mShowSetting) {
            return null;
        }
        ComponentName cn = this.mAssistUtils.getAssistComponentForUser(this.mUserId);
        if (cn == null) {
            return null;
        }
        Intent probe = new Intent("android.service.voice.VoiceInteractionService").setPackage(cn.getPackageName());
        PackageManager pm = this.mPackageManager.getPackageManager();
        List<ResolveInfo> services = pm.queryIntentServices(probe, 128);
        if (services == null || services.isEmpty()) {
            return null;
        }
        String activity = getAssistSettingsActivity(cn, (ResolveInfo) services.get(0), pm);
        if (activity == null) {
            return null;
        }
        return new Intent("android.intent.action.MAIN").setComponent(new ComponentName(cn.getPackageName(), activity));
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_assist_and_voice_input);
    }

    public String getPreferenceKey() {
        return this.mPrefKey;
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        ComponentName cn = this.mAssistUtils.getAssistComponentForUser(this.mUserId);
        if (cn == null) {
            return null;
        }
        if (cn != null) {
            OPUtils.sendAppTrackerForAssistAppByComponentName(cn.toString());
        }
        return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, cn);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getAssistSettingsActivity(ComponentName cn, ResolveInfo resolveInfo, PackageManager pm) {
        VoiceInteractionServiceInfo voiceInfo = new VoiceInteractionServiceInfo(pm, resolveInfo.serviceInfo);
        if (voiceInfo.getSupportsAssist()) {
            return voiceInfo.getSettingsActivity();
        }
        return null;
    }
}
