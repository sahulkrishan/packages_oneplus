package com.android.settings.inputmethod;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.textservice.SpellCheckerInfo;
import android.view.textservice.SpellCheckerSubtype;
import android.view.textservice.TextServicesManager;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;

public class SpellCheckersSettings extends SettingsPreferenceFragment implements OnSwitchChangeListener, OnPreferenceClickListener, OnPreferenceChangeListener {
    private static final boolean DBG = false;
    private static final int ITEM_ID_USE_SYSTEM_LANGUAGE = 0;
    private static final String KEY_DEFAULT_SPELL_CHECKER = "default_spellchecker";
    private static final String KEY_SPELL_CHECKER_LANGUAGE = "spellchecker_language";
    private static final String TAG = SpellCheckersSettings.class.getSimpleName();
    private SpellCheckerInfo mCurrentSci;
    private AlertDialog mDialog = null;
    private SpellCheckerInfo[] mEnabledScis;
    private Preference mSpellCheckerLanaguagePref;
    private SwitchBar mSwitchBar;
    private TextServicesManager mTsm;

    public int getMetricsCategory() {
        return 59;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.spellchecker_prefs);
        this.mSpellCheckerLanaguagePref = findPreference(KEY_SPELL_CHECKER_LANGUAGE);
        this.mSpellCheckerLanaguagePref.setOnPreferenceClickListener(this);
        this.mTsm = (TextServicesManager) getSystemService("textservices");
        this.mCurrentSci = this.mTsm.getCurrentSpellChecker();
        this.mEnabledScis = this.mTsm.getEnabledSpellCheckers();
        populatePreferenceScreen();
    }

    private void populatePreferenceScreen() {
        SpellCheckerPreference pref = new SpellCheckerPreference(getPrefContext(), this.mEnabledScis);
        pref.setTitle((int) R.string.default_spell_checker);
        if ((this.mEnabledScis == null ? 0 : this.mEnabledScis.length) > 0) {
            pref.setSummary("%s");
        } else {
            pref.setSummary((int) R.string.spell_checker_not_selected);
        }
        pref.setKey(KEY_DEFAULT_SPELL_CHECKER);
        pref.setOnPreferenceChangeListener(this);
        getPreferenceScreen().addPreference(pref);
    }

    public void onResume() {
        super.onResume();
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        this.mSwitchBar.setSwitchBarText(R.string.spell_checker_master_switch_title, R.string.spell_checker_master_switch_title);
        this.mSwitchBar.show();
        this.mSwitchBar.addOnSwitchChangeListener(this);
        updatePreferenceScreen();
    }

    public void onPause() {
        super.onPause();
        this.mSwitchBar.removeOnSwitchChangeListener(this);
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        Secure.putInt(getContentResolver(), "spell_checker_enabled", isChecked);
        updatePreferenceScreen();
    }

    private void updatePreferenceScreen() {
        SpellCheckerSubtype currentScs;
        this.mCurrentSci = this.mTsm.getCurrentSpellChecker();
        boolean isSpellCheckerEnabled = this.mTsm.isSpellCheckerEnabled();
        this.mSwitchBar.setChecked(isSpellCheckerEnabled);
        boolean z = false;
        if (this.mCurrentSci != null) {
            currentScs = this.mTsm.getCurrentSpellCheckerSubtype(false);
        } else {
            currentScs = null;
        }
        this.mSpellCheckerLanaguagePref.setSummary(getSpellCheckerSubtypeLabel(this.mCurrentSci, currentScs));
        PreferenceScreen screen = getPreferenceScreen();
        int count = screen.getPreferenceCount();
        for (int index = 0; index < count; index++) {
            Preference preference = screen.getPreference(index);
            preference.setEnabled(isSpellCheckerEnabled);
            if (preference instanceof SpellCheckerPreference) {
                ((SpellCheckerPreference) preference).setSelected(this.mCurrentSci);
            }
        }
        Preference preference2 = this.mSpellCheckerLanaguagePref;
        if (isSpellCheckerEnabled && this.mCurrentSci != null) {
            z = true;
        }
        preference2.setEnabled(z);
    }

    private CharSequence getSpellCheckerSubtypeLabel(SpellCheckerInfo sci, SpellCheckerSubtype subtype) {
        if (sci == null) {
            return getString(R.string.spell_checker_not_selected);
        }
        if (subtype == null) {
            return getString(R.string.use_system_language_to_select_input_method_subtypes);
        }
        return subtype.getDisplayName(getActivity(), sci.getPackageName(), sci.getServiceInfo().applicationInfo);
    }

    public boolean onPreferenceClick(Preference pref) {
        if (pref != this.mSpellCheckerLanaguagePref) {
            return false;
        }
        showChooseLanguageDialog();
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        SpellCheckerInfo sci = (SpellCheckerInfo) newValue;
        if ((sci.getServiceInfo().applicationInfo.flags & 1) != 0) {
            changeCurrentSpellChecker(sci);
            return true;
        }
        showSecurityWarnDialog(sci);
        return false;
    }

    private static int convertSubtypeIndexToDialogItemId(int index) {
        return index + 1;
    }

    private static int convertDialogItemIdToSubtypeIndex(int item) {
        return item - 1;
    }

    private void showChooseLanguageDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        final SpellCheckerInfo currentSci = this.mTsm.getCurrentSpellChecker();
        if (currentSci != null) {
            int index = 0;
            SpellCheckerSubtype currentScs = this.mTsm.getCurrentSpellCheckerSubtype(false);
            Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.phone_language);
            int subtypeCount = currentSci.getSubtypeCount();
            CharSequence[] items = new CharSequence[(subtypeCount + 1)];
            items[0] = getSpellCheckerSubtypeLabel(currentSci, null);
            int checkedItemId = 0;
            while (index < subtypeCount) {
                SpellCheckerSubtype subtype = currentSci.getSubtypeAt(index);
                int itemId = convertSubtypeIndexToDialogItemId(index);
                items[itemId] = getSpellCheckerSubtypeLabel(currentSci, subtype);
                if (subtype.equals(currentScs)) {
                    checkedItemId = itemId;
                }
                index++;
            }
            builder.setSingleChoiceItems(items, checkedItemId, new OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    int subtypeId;
                    if (item == 0) {
                        subtypeId = 0;
                    } else {
                        subtypeId = currentSci.getSubtypeAt(SpellCheckersSettings.convertDialogItemIdToSubtypeIndex(item)).hashCode();
                    }
                    Secure.putInt(SpellCheckersSettings.this.getContentResolver(), "selected_spell_checker_subtype", subtypeId);
                    dialog.dismiss();
                    SpellCheckersSettings.this.updatePreferenceScreen();
                }
            });
            this.mDialog = builder.create();
            this.mDialog.show();
        }
    }

    private void showSecurityWarnDialog(final SpellCheckerInfo sci) {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        Builder builder = new Builder(getActivity());
        builder.setTitle(17039380);
        builder.setMessage(getString(R.string.spellchecker_security_warning, new Object[]{sci.loadLabel(getPackageManager())}));
        builder.setCancelable(true);
        builder.setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SpellCheckersSettings.this.changeCurrentSpellChecker(sci);
            }
        });
        builder.setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        this.mDialog = builder.create();
        this.mDialog.show();
    }

    private void changeCurrentSpellChecker(SpellCheckerInfo sci) {
        Secure.putString(getContentResolver(), "selected_spell_checker", sci.getId());
        Secure.putInt(getContentResolver(), "selected_spell_checker_subtype", 0);
        updatePreferenceScreen();
    }
}
