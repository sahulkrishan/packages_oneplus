package com.android.settings.overlay;

import android.accounts.Account;
import android.app.Activity;
import android.content.Context;
import com.android.settings.support.SupportPhone;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface SupportFeatureProvider {

    @Retention(RetentionPolicy.SOURCE)
    public @interface SupportType {
        public static final int CHAT = 3;
        public static final int EMAIL = 1;
        public static final int PHONE = 2;
    }

    String getCurrentCountryCodeIfHasConfig(int i);

    String getNewDeviceIntroUrl(Context context);

    Account[] getSupportEligibleAccounts(Context context);

    SupportPhone getSupportPhones(String str, boolean z);

    void refreshOperationRules();

    void startSupportV2(Activity activity);
}
