package com.android.settings.system;

import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class OPCollectDiagnosticsPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_COLLECT_DIAGNOSTICS = "reset_collect_diagnostics";
    private final UserManager mUm;

    public OPCollectDiagnosticsPreferenceController(Context context) {
        super(context);
        this.mUm = (UserManager) context.getSystemService("user");
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_collect_diagnostics) && (this.mUm.isAdminUser() || Utils.isDemoUser(this.mContext));
    }

    public String getPreferenceKey() {
        return KEY_COLLECT_DIAGNOSTICS;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            Preference pref = screen.findPreference(KEY_COLLECT_DIAGNOSTICS);
            if (pref != null) {
                String action = this.mContext.getString(R.string.config_collect_diag);
                if (!TextUtils.isEmpty(action)) {
                    pref.setIntent(new Intent(action));
                }
            }
        }
    }

    public void updateState(Preference preference) {
        super.updateState(preference);
        if (isAvailable()) {
            String action = this.mContext.getString(R.string.config_collect_diag);
            if (!TextUtils.isEmpty(action)) {
                preference.setIntent(new Intent(action));
            }
        }
    }
}
