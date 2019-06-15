package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.TogglePreferenceController;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.InlineSwitchPayload;
import com.android.settings.search.ResultPayload;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnPause;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.oneplus.settings.utils.OPUtils;

public class BadgingNotificationPreferenceController extends TogglePreferenceController implements PreferenceControllerMixin, OnPreferenceChangeListener, LifecycleObserver, OnResume, OnPause {
    @VisibleForTesting
    static final int OFF = 0;
    @VisibleForTesting
    static final int ON = 1;
    private static final String TAG = "BadgeNotifPrefContr";
    private SettingObserver mSettingObserver;

    class SettingObserver extends ContentObserver {
        private final Uri NOTIFICATION_BADGING_URI = Secure.getUriFor("notification_badging");
        private final Preference mPreference;

        public SettingObserver(Preference preference) {
            super(new Handler());
            this.mPreference = preference;
        }

        public void register(ContentResolver cr, boolean register) {
            if (register) {
                cr.registerContentObserver(this.NOTIFICATION_BADGING_URI, false, this);
            } else {
                cr.unregisterContentObserver(this);
            }
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (this.NOTIFICATION_BADGING_URI.equals(uri)) {
                BadgingNotificationPreferenceController.this.updateState(this.mPreference);
            }
        }
    }

    public BadgingNotificationPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference("notification_badging");
        if (preference != null) {
            this.mSettingObserver = new SettingObserver(preference);
        }
    }

    public void onResume() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), true);
        }
    }

    public void onPause() {
        if (this.mSettingObserver != null) {
            this.mSettingObserver.register(this.mContext.getContentResolver(), false);
        }
    }

    public int getAvailabilityStatus() {
        return this.mContext.getResources().getBoolean(17957004) ? 0 : 2;
    }

    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), "notification_badging");
    }

    public boolean isChecked() {
        return Secure.getInt(this.mContext.getContentResolver(), "notification_badging", 1) == 1;
    }

    public boolean setChecked(boolean isChecked) {
        if (OPUtils.hasMultiAppProfiles(UserManager.get(this.mContext))) {
            Secure.putIntForUser(this.mContext.getContentResolver(), "notification_badging", isChecked, 999);
        }
        return Secure.putInt(this.mContext.getContentResolver(), "notification_badging", isChecked);
    }

    public ResultPayload getResultPayload() {
        return new InlineSwitchPayload("notification_badging", 2, 1, DatabaseIndexingUtils.buildSearchResultPageIntent(this.mContext, ConfigureNotificationSettings.class.getName(), getPreferenceKey(), this.mContext.getString(R.string.configure_notification_settings)), isAvailable(), 1);
    }
}
