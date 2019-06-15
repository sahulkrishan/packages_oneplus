package com.android.settings.accounts;

import android.accounts.Account;
import android.content.Context;
import android.util.FeatureFlagUtils;
import com.android.settings.core.FeatureFlags;

public interface AccountFeatureProvider {
    String getAccountType();

    Account[] getAccounts(Context context);

    boolean isAboutPhoneV2Enabled(Context context) {
        return FeatureFlagUtils.isEnabled(context, FeatureFlags.ABOUT_PHONE_V2);
    }
}
