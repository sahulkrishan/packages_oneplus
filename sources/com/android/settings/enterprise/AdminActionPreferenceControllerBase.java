package com.android.settings.enterprise;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.text.format.DateUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.Date;

public abstract class AdminActionPreferenceControllerBase extends AbstractPreferenceController implements PreferenceControllerMixin {
    protected final EnterprisePrivacyFeatureProvider mFeatureProvider;

    public abstract Date getAdminActionTimestamp();

    public AdminActionPreferenceControllerBase(Context context) {
        super(context);
        this.mFeatureProvider = FeatureFactory.getFactory(context).getEnterprisePrivacyFeatureProvider(context);
    }

    public void updateState(Preference preference) {
        CharSequence string;
        Date timestamp = getAdminActionTimestamp();
        if (timestamp == null) {
            string = this.mContext.getString(R.string.enterprise_privacy_none);
        } else {
            string = DateUtils.formatDateTime(this.mContext, timestamp.getTime(), 17);
        }
        preference.setSummary(string);
    }

    public boolean isAvailable() {
        return true;
    }
}
