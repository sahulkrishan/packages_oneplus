package com.android.settings.language;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.inputmethod.UserDictionaryList;
import com.android.settings.inputmethod.UserDictionarySettings;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.TreeSet;

public class UserDictionaryPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_USER_DICTIONARY_SETTINGS = "key_user_dictionary_settings";

    public UserDictionaryPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return KEY_USER_DICTIONARY_SETTINGS;
    }

    public void updateState(Preference preference) {
        if (isAvailable() && preference != null) {
            Class<? extends Fragment> targetFragment;
            TreeSet<String> localeSet = getDictionaryLocales();
            Bundle extras = preference.getExtras();
            if (localeSet.size() <= 1) {
                if (!localeSet.isEmpty()) {
                    extras.putString("locale", (String) localeSet.first());
                }
                targetFragment = UserDictionarySettings.class;
            } else {
                targetFragment = UserDictionaryList.class;
            }
            preference.setFragment(targetFragment.getCanonicalName());
        }
    }

    /* Access modifiers changed, original: protected */
    public TreeSet<String> getDictionaryLocales() {
        return UserDictionaryList.getUserDictionaryLocalesSet(this.mContext);
    }
}
