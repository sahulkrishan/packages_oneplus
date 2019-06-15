package com.android.settingslib.inputmethod;

import android.content.Context;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.settingslib.R;
import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class InputMethodAndSubtypeEnablerManager implements OnPreferenceChangeListener {
    private final HashMap<String, TwoStatePreference> mAutoSelectionPrefsMap = new HashMap();
    private final Collator mCollator = Collator.getInstance();
    private final PreferenceFragment mFragment;
    private boolean mHaveHardKeyboard;
    private InputMethodManager mImm;
    private final HashMap<String, List<Preference>> mInputMethodAndSubtypePrefsMap = new HashMap();
    private List<InputMethodInfo> mInputMethodInfoList;

    public InputMethodAndSubtypeEnablerManager(PreferenceFragment fragment) {
        this.mFragment = fragment;
        this.mImm = (InputMethodManager) fragment.getContext().getSystemService(InputMethodManager.class);
        this.mInputMethodInfoList = this.mImm.getInputMethodList();
    }

    public void init(PreferenceFragment fragment, String targetImi, PreferenceScreen root) {
        this.mHaveHardKeyboard = fragment.getResources().getConfiguration().keyboard == 2;
        for (InputMethodInfo imi : this.mInputMethodInfoList) {
            if (imi.getId().equals(targetImi) || TextUtils.isEmpty(targetImi)) {
                addInputMethodSubtypePreferences(fragment, imi, root);
            }
        }
    }

    public void refresh(Context context, PreferenceFragment fragment) {
        InputMethodSettingValuesWrapper.getInstance(context).refreshAllInputMethodAndSubtypes();
        InputMethodAndSubtypeUtil.loadInputMethodSubtypeList(fragment, context.getContentResolver(), this.mInputMethodInfoList, this.mInputMethodAndSubtypePrefsMap);
        updateAutoSelectionPreferences();
    }

    public void save(Context context, PreferenceFragment fragment) {
        InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(fragment, context.getContentResolver(), this.mInputMethodInfoList, this.mHaveHardKeyboard);
    }

    public boolean onPreferenceChange(Preference pref, Object newValue) {
        if (!(newValue instanceof Boolean)) {
            return true;
        }
        boolean isChecking = ((Boolean) newValue).booleanValue();
        for (String imiId : this.mAutoSelectionPrefsMap.keySet()) {
            if (this.mAutoSelectionPrefsMap.get(imiId) == pref) {
                TwoStatePreference autoSelectionPref = (TwoStatePreference) pref;
                autoSelectionPref.setChecked(isChecking);
                setAutoSelectionSubtypesEnabled(imiId, autoSelectionPref.isChecked());
                return false;
            }
        }
        if (!(pref instanceof InputMethodSubtypePreference)) {
            return true;
        }
        InputMethodSubtypePreference subtypePref = (InputMethodSubtypePreference) pref;
        subtypePref.setChecked(isChecking);
        if (!subtypePref.isChecked()) {
            updateAutoSelectionPreferences();
        }
        return false;
    }

    private void addInputMethodSubtypePreferences(PreferenceFragment fragment, InputMethodInfo imi, PreferenceScreen root) {
        InputMethodInfo inputMethodInfo = imi;
        PreferenceScreen preferenceScreen = root;
        Context prefContext = fragment.getPreferenceManager().getContext();
        int subtypeCount = imi.getSubtypeCount();
        if (subtypeCount > 1) {
            String imiId = imi.getId();
            PreferenceCategory keyboardSettingsCategory = new PreferenceCategory(prefContext);
            preferenceScreen.addPreference(keyboardSettingsCategory);
            keyboardSettingsCategory.setTitle(inputMethodInfo.loadLabel(prefContext.getPackageManager()));
            keyboardSettingsCategory.setKey(imiId);
            TwoStatePreference autoSelectionPref = new SwitchWithNoTextPreference(prefContext);
            this.mAutoSelectionPrefsMap.put(imiId, autoSelectionPref);
            keyboardSettingsCategory.addPreference(autoSelectionPref);
            autoSelectionPref.setOnPreferenceChangeListener(this);
            PreferenceCategory activeInputMethodsCategory = new PreferenceCategory(prefContext);
            activeInputMethodsCategory.setTitle(R.string.active_input_method_subtypes);
            preferenceScreen.addPreference(activeInputMethodsCategory);
            CharSequence autoSubtypeLabel = null;
            ArrayList<Preference> subtypePreferences = new ArrayList();
            for (int index = 0; index < subtypeCount; index++) {
                InputMethodSubtype subtype = inputMethodInfo.getSubtypeAt(index);
                if (!subtype.overridesImplicitlyEnabledSubtype()) {
                    subtypePreferences.add(new InputMethodSubtypePreference(prefContext, subtype, inputMethodInfo));
                } else if (autoSubtypeLabel == null) {
                    autoSubtypeLabel = InputMethodAndSubtypeUtil.getSubtypeLocaleNameAsSentence(subtype, prefContext, inputMethodInfo);
                }
            }
            subtypePreferences.sort(new -$$Lambda$InputMethodAndSubtypeEnablerManager$dNefE8o88NKQTk3_894EfBqAP3w(this));
            Iterator it = subtypePreferences.iterator();
            while (it.hasNext()) {
                Preference pref = (Preference) it.next();
                activeInputMethodsCategory.addPreference(pref);
                pref.setOnPreferenceChangeListener(this);
                InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(pref);
            }
            this.mInputMethodAndSubtypePrefsMap.put(imiId, subtypePreferences);
            if (TextUtils.isEmpty(autoSubtypeLabel)) {
                autoSelectionPref.setTitle(R.string.use_system_language_to_select_input_method_subtypes);
            } else {
                autoSelectionPref.setTitle(autoSubtypeLabel);
            }
        }
    }

    public static /* synthetic */ int lambda$addInputMethodSubtypePreferences$0(InputMethodAndSubtypeEnablerManager inputMethodAndSubtypeEnablerManager, Preference lhs, Preference rhs) {
        if (lhs instanceof InputMethodSubtypePreference) {
            return ((InputMethodSubtypePreference) lhs).compareTo(rhs, inputMethodAndSubtypeEnablerManager.mCollator);
        }
        return lhs.compareTo(rhs);
    }

    private boolean isNoSubtypesExplicitlySelected(String imiId) {
        for (Preference pref : (List) this.mInputMethodAndSubtypePrefsMap.get(imiId)) {
            if ((pref instanceof TwoStatePreference) && ((TwoStatePreference) pref).isChecked()) {
                return false;
            }
        }
        return true;
    }

    private void setAutoSelectionSubtypesEnabled(String imiId, boolean autoSelectionEnabled) {
        TwoStatePreference autoSelectionPref = (TwoStatePreference) this.mAutoSelectionPrefsMap.get(imiId);
        if (autoSelectionPref != null) {
            autoSelectionPref.setChecked(autoSelectionEnabled);
            for (Preference pref : (List) this.mInputMethodAndSubtypePrefsMap.get(imiId)) {
                if (pref instanceof TwoStatePreference) {
                    pref.setEnabled(autoSelectionEnabled ^ 1);
                    if (autoSelectionEnabled) {
                        ((TwoStatePreference) pref).setChecked(false);
                    }
                }
            }
            if (autoSelectionEnabled) {
                InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this.mFragment, this.mFragment.getContext().getContentResolver(), this.mInputMethodInfoList, this.mHaveHardKeyboard);
                updateImplicitlyEnabledSubtypes(imiId);
            }
        }
    }

    private void updateImplicitlyEnabledSubtypes(String targetImiId) {
        for (InputMethodInfo imi : this.mInputMethodInfoList) {
            String imiId = imi.getId();
            TwoStatePreference autoSelectionPref = (TwoStatePreference) this.mAutoSelectionPrefsMap.get(imiId);
            if (autoSelectionPref != null) {
                if (autoSelectionPref.isChecked()) {
                    if (imiId.equals(targetImiId) || targetImiId == null) {
                        updateImplicitlyEnabledSubtypesOf(imi);
                    }
                }
            }
        }
    }

    private void updateImplicitlyEnabledSubtypesOf(InputMethodInfo imi) {
        String imiId = imi.getId();
        List<Preference> subtypePrefs = (List) this.mInputMethodAndSubtypePrefsMap.get(imiId);
        List<InputMethodSubtype> implicitlyEnabledSubtypes = this.mImm.getEnabledInputMethodSubtypeList(imi, true);
        if (subtypePrefs != null && implicitlyEnabledSubtypes != null) {
            for (Preference pref : subtypePrefs) {
                if (pref instanceof TwoStatePreference) {
                    TwoStatePreference subtypePref = (TwoStatePreference) pref;
                    subtypePref.setChecked(false);
                    for (InputMethodSubtype subtype : implicitlyEnabledSubtypes) {
                        String implicitlyEnabledSubtypePrefKey = new StringBuilder();
                        implicitlyEnabledSubtypePrefKey.append(imiId);
                        implicitlyEnabledSubtypePrefKey.append(subtype.hashCode());
                        if (subtypePref.getKey().equals(implicitlyEnabledSubtypePrefKey.toString())) {
                            subtypePref.setChecked(true);
                            break;
                        }
                    }
                }
            }
        }
    }

    private void updateAutoSelectionPreferences() {
        for (String imiId : this.mInputMethodAndSubtypePrefsMap.keySet()) {
            setAutoSelectionSubtypesEnabled(imiId, isNoSubtypesExplicitlySelected(imiId));
        }
        updateImplicitlyEnabledSubtypes(null);
    }
}
