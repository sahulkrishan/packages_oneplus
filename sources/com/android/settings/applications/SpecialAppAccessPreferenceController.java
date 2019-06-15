package com.android.settings.applications;

import android.content.Context;
import android.support.v7.preference.Preference;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.datausage.DataSaverBackend;
import com.android.settingslib.core.AbstractPreferenceController;

public class SpecialAppAccessPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_SPECIAL_ACCESS = "special_access";
    private DataSaverBackend mDataSaverBackend;

    public SpecialAppAccessPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_SPECIAL_ACCESS;
    }

    public void updateState(Preference preference) {
        if (this.mDataSaverBackend == null) {
            this.mDataSaverBackend = new DataSaverBackend(this.mContext);
        }
        int count = this.mDataSaverBackend.getWhitelistedCount();
        preference.setSummary(this.mContext.getResources().getQuantityString(R.plurals.special_access_summary, count, new Object[]{Integer.valueOf(count)}));
    }
}
