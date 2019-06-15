package com.android.settings.display;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Pair;
import com.android.internal.hardware.AmbientDisplayConfiguration;
import com.android.settings.R;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;
import com.android.settingslib.core.instrumentation.MetricsFeatureProvider;

public class AmbientDisplayNotificationsPreferenceController extends TogglePreferenceController implements OnPreferenceChangeListener {
    @VisibleForTesting
    static final String KEY_AMBIENT_DISPLAY_NOTIFICATIONS = "ambient_display_notification";
    private static final int MY_USER = UserHandle.myUserId();
    private final int OFF = 0;
    private final int ON = 1;
    private AmbientDisplayConfiguration mConfig;
    private final MetricsFeatureProvider mMetricsFeatureProvider;

    public AmbientDisplayNotificationsPreferenceController(Context context, String key) {
        super(context, key);
        this.mMetricsFeatureProvider = FeatureFactory.getFactory(context).getMetricsFeatureProvider();
    }

    public AmbientDisplayNotificationsPreferenceController setConfig(AmbientDisplayConfiguration config) {
        this.mConfig = config;
        return this;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (KEY_AMBIENT_DISPLAY_NOTIFICATIONS.equals(preference.getKey())) {
            this.mMetricsFeatureProvider.action(this.mContext, 495, new Pair[0]);
        }
        return false;
    }

    public boolean isChecked() {
        return this.mConfig.pulseOnNotificationEnabled(MY_USER);
    }

    public boolean setChecked(boolean isChecked) {
        Secure.putInt(this.mContext.getContentResolver(), "doze_enabled", isChecked);
        return true;
    }

    public int getAvailabilityStatus() {
        if (this.mConfig == null) {
            this.mConfig = new AmbientDisplayConfiguration(this.mContext);
        }
        return this.mConfig.pulseOnNotificationAvailable() ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), KEY_AMBIENT_DISPLAY_NOTIFICATIONS);
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("doze_enabled", 2, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, AmbientDisplaySettings.class.getName(), KEY_AMBIENT_DISPLAY_NOTIFICATIONS, this.mContext.getString(R.string.ambient_display_screen_title)), isAvailable(), 1);
    }
}
