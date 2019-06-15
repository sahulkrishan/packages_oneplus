package com.android.settings.notification;

import android.content.Context;
import android.content.IntentFilter;
import android.os.UserHandle;
import android.support.v7.preference.Preference;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.accounts.AccountRestrictionHelper;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.core.SliderPreferenceController;
import com.android.settingslib.RestrictedPreference;

public abstract class AdjustVolumeRestrictedPreferenceController extends SliderPreferenceController implements PreferenceControllerMixin {
    private AccountRestrictionHelper mHelper;

    public AdjustVolumeRestrictedPreferenceController(Context context, String key) {
        this(context, new AccountRestrictionHelper(context), key);
    }

    @VisibleForTesting
    AdjustVolumeRestrictedPreferenceController(Context context, AccountRestrictionHelper helper, String key) {
        super(context, key);
        this.mHelper = helper;
    }

    public void updateState(Preference preference) {
        if (preference instanceof RestrictedPreference) {
            this.mHelper.enforceRestrictionOnPreference((RestrictedPreference) preference, "no_adjust_volume", UserHandle.myUserId());
        }
    }

    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        filter.addAction("android.media.STREAM_MUTE_CHANGED_ACTION");
        filter.addAction("android.media.MASTER_MUTE_CHANGED_ACTION");
        return filter;
    }
}
