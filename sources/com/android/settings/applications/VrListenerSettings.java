package com.android.settings.applications;

import android.content.ComponentName;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.utils.ManagedServiceSettings;
import com.android.settings.utils.ManagedServiceSettings.Config;
import com.android.settings.utils.ManagedServiceSettings.Config.Builder;

public class VrListenerSettings extends ManagedServiceSettings {
    private static final Config CONFIG = new Builder().setTag(TAG).setSetting("enabled_vr_listeners").setIntentAction("android.service.vr.VrListenerService").setPermission("android.permission.BIND_VR_LISTENER_SERVICE").setNoun("vr listener").setWarningDialogTitle(R.string.vr_listener_security_warning_title).setWarningDialogSummary(R.string.vr_listener_security_warning_summary).setEmptyText(R.string.no_vr_listeners).build();
    private static final String TAG = VrListenerSettings.class.getSimpleName();

    /* Access modifiers changed, original: protected */
    public Config getConfig() {
        return CONFIG;
    }

    public int getMetricsCategory() {
        return 334;
    }

    /* Access modifiers changed, original: protected */
    public boolean setEnabled(ComponentName service, String title, boolean enable) {
        logSpecialPermissionChange(enable, service.getPackageName());
        return super.setEnabled(service, title, enable);
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.vr_listeners_settings;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logSpecialPermissionChange(boolean enable, String packageName) {
        int logCategory;
        if (enable) {
            logCategory = 772;
        } else {
            logCategory = 773;
        }
        FeatureFactory.getFactory(getContext()).getMetricsFeatureProvider().action(getContext(), logCategory, packageName, new Pair[0]);
    }
}
