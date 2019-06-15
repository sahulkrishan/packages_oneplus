package com.android.settings.inputmethod;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.TextServicesManager;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.inputmethod.InputMethodAndSubtypeUtil;

public class SpellCheckerPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    public static final String KEY_SPELL_CHECKERS = "spellcheckers_settings";
    private final TextServicesManager mTextServicesManager;

    public SpellCheckerPreferenceController(Context context) {
        super(context);
        this.mTextServicesManager = (TextServicesManager) context.getSystemService("textservices");
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(KEY_SPELL_CHECKERS);
        if (preference != null) {
            InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(preference);
        }
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_spellcheckers_settings);
    }

    public String getPreferenceKey() {
        return KEY_SPELL_CHECKERS;
    }

    public void updateState(Preference preference) {
        if (preference != null) {
            if (this.mTextServicesManager.isSpellCheckerEnabled()) {
                SpellCheckerInfo sci = this.mTextServicesManager.getCurrentSpellChecker();
                if (sci != null) {
                    preference.setSummary(sci.loadLabel(this.mContext.getPackageManager()));
                } else {
                    preference.setSummary((int) R.string.spell_checker_not_selected);
                }
            } else {
                preference.setSummary((int) R.string.switch_off_text);
            }
        }
    }
}
