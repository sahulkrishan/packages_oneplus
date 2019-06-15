package com.android.settings.deletionhelper;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.format.DateUtils;
import android.text.format.Formatter;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.Utils;
import com.android.settingslib.core.AbstractPreferenceController;

public class AutomaticStorageManagerDescriptionPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_FREED = "freed_bytes";

    public AutomaticStorageManagerDescriptionPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_FREED;
    }

    public void displayPreference(PreferenceScreen screen) {
        Preference preference = screen.findPreference(getPreferenceKey());
        Context context = preference.getContext();
        ContentResolver cr = context.getContentResolver();
        long freedBytes = Secure.getLong(cr, "automatic_storage_manager_bytes_cleared", 0);
        long lastRunMillis = Secure.getLong(cr, "automatic_storage_manager_last_run", 0);
        if (freedBytes == 0 || lastRunMillis == 0 || !Utils.isStorageManagerEnabled(context)) {
            preference.setSummary((int) R.string.automatic_storage_manager_text);
            return;
        }
        preference.setSummary(context.getString(R.string.automatic_storage_manager_freed_bytes, new Object[]{Formatter.formatFileSize(context, freedBytes), DateUtils.formatDateTime(context, lastRunMillis, 16)}));
    }
}
