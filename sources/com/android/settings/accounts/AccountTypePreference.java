package com.android.settings.accounts;

import android.accounts.Account;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.Utils;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.widget.AppPreference;

public class AccountTypePreference extends AppPreference implements OnPreferenceClickListener {
    private final String mFragment;
    private final Bundle mFragmentArguments;
    private final int mMetricsCategory;
    private final CharSequence mSummary;
    private final CharSequence mTitle;
    private final int mTitleResId;
    private final String mTitleResPackageName;

    public AccountTypePreference(Context context, int metricsCategory, Account account, String titleResPackageName, int titleResId, CharSequence summary, String fragment, Bundle fragmentArguments, Drawable icon) {
        super(context);
        this.mTitle = account.name;
        this.mTitleResPackageName = titleResPackageName;
        this.mTitleResId = titleResId;
        this.mSummary = summary;
        this.mFragment = fragment;
        this.mFragmentArguments = fragmentArguments;
        this.mMetricsCategory = metricsCategory;
        setKey(buildKey(account));
        setTitle(this.mTitle);
        setSummary(summary);
        setIcon(icon);
        setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        if (this.mFragment == null) {
            return false;
        }
        UserManager userManager = (UserManager) getContext().getSystemService("user");
        UserHandle user = (UserHandle) this.mFragmentArguments.getParcelable("android.intent.extra.USER");
        if (user != null && Utils.startQuietModeDialogIfNecessary(getContext(), userManager, user.getIdentifier())) {
            return true;
        }
        if (user != null && Utils.unlockWorkProfileIfNecessary(getContext(), user.getIdentifier())) {
            return true;
        }
        new SubSettingLauncher(getContext()).setDestination(this.mFragment).setArguments(this.mFragmentArguments).setTitle(this.mTitleResPackageName, this.mTitleResId).setSourceMetricsCategory(this.mMetricsCategory).launch();
        return true;
    }

    public static String buildKey(Account account) {
        return String.valueOf(account.hashCode());
    }

    public CharSequence getTitle() {
        return this.mTitle;
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }
}
