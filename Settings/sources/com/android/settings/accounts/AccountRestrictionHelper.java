package com.android.settings.accounts;

import android.content.Context;
import com.android.settings.AccessiblePreferenceCategory;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
import java.util.ArrayList;

public class AccountRestrictionHelper {
    private final Context mContext;

    public AccountRestrictionHelper(Context context) {
        this.mContext = context;
    }

    public void enforceRestrictionOnPreference(RestrictedPreference preference, String userRestriction, int userId) {
        if (preference != null) {
            if (hasBaseUserRestriction(userRestriction, userId)) {
                preference.setEnabled(false);
            } else {
                preference.checkRestrictionAndSetDisabled(userRestriction, userId);
            }
        }
    }

    public boolean hasBaseUserRestriction(String userRestriction, int userId) {
        return RestrictedLockUtils.hasBaseUserRestriction(this.mContext, userRestriction, userId);
    }

    public AccessiblePreferenceCategory createAccessiblePreferenceCategory(Context context) {
        return new AccessiblePreferenceCategory(context);
    }

    public static boolean showAccount(String[] authorities, ArrayList<String> auths) {
        boolean showAccount = true;
        if (!(authorities == null || auths == null)) {
            showAccount = false;
            for (String requestedAuthority : authorities) {
                if (auths.contains(requestedAuthority)) {
                    return true;
                }
            }
        }
        return showAccount;
    }
}
