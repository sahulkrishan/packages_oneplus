package com.android.settings.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.LayoutRes;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.InstrumentedPreferenceFragment;
import com.android.settings.widget.RadioButtonPreference.OnClickListener;
import com.android.settingslib.widget.CandidateInfo;
import java.util.List;
import java.util.Map;

public abstract class RadioButtonPickerFragment extends InstrumentedPreferenceFragment implements OnClickListener {
    @VisibleForTesting(otherwise = 2)
    static final String EXTRA_FOR_WORK = "for_work";
    private final Map<String, CandidateInfo> mCandidates = new ArrayMap();
    protected int mUserId;
    protected UserManager mUserManager;

    public abstract List<? extends CandidateInfo> getCandidates();

    public abstract String getDefaultKey();

    public abstract int getPreferenceScreenResId();

    public abstract boolean setDefaultKey(String str);

    public void onAttach(Context context) {
        int myUserId;
        super.onAttach(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        Bundle arguments = getArguments();
        boolean mForWork = false;
        if (arguments != null) {
            mForWork = arguments.getBoolean(EXTRA_FOR_WORK);
        }
        UserHandle managedProfile = Utils.getManagedProfile(this.mUserManager);
        if (!mForWork || managedProfile == null) {
            myUserId = UserHandle.myUserId();
        } else {
            myUserId = managedProfile.getIdentifier();
        }
        this.mUserId = myUserId;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        updateCandidates();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        return view;
    }

    public void onRadioButtonClicked(RadioButtonPreference selected) {
        onRadioButtonConfirmed(selected.getKey());
    }

    /* Access modifiers changed, original: protected */
    public void onSelectionPerformed(boolean success) {
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldShowItemNone() {
        return false;
    }

    /* Access modifiers changed, original: protected */
    public void addStaticPreferences(PreferenceScreen screen) {
    }

    /* Access modifiers changed, original: protected */
    public CandidateInfo getCandidate(String key) {
        return (CandidateInfo) this.mCandidates.get(key);
    }

    /* Access modifiers changed, original: protected */
    public void onRadioButtonConfirmed(String selectedKey) {
        boolean success = setDefaultKey(selectedKey);
        if (success) {
            updateCheckedState(selectedKey);
        }
        onSelectionPerformed(success);
    }

    @VisibleForTesting(otherwise = 4)
    public void bindPreferenceExtra(RadioButtonPreference pref, String key, CandidateInfo info, String defaultKey, String systemDefaultKey) {
    }

    @VisibleForTesting
    public void updateCandidates() {
        this.mCandidates.clear();
        List<? extends CandidateInfo> candidateList = getCandidates();
        if (candidateList != null) {
            for (CandidateInfo info : candidateList) {
                this.mCandidates.put(info.getKey(), info);
            }
        }
        String defaultKey = getDefaultKey();
        String systemDefaultKey = getSystemDefaultKey();
        PreferenceScreen screen = getPreferenceScreen();
        screen.removeAll();
        addStaticPreferences(screen);
        int customLayoutResId = getRadioButtonPreferenceCustomLayoutResId();
        if (shouldShowItemNone()) {
            RadioButtonPreference nonePref = new RadioButtonPreference(getPrefContext());
            if (customLayoutResId > 0) {
                nonePref.setLayoutResource(customLayoutResId);
            }
            nonePref.setIcon((int) R.drawable.ic_remove_circle);
            nonePref.setTitle((int) R.string.app_list_preference_none);
            nonePref.setChecked(TextUtils.isEmpty(defaultKey));
            nonePref.setOnClickListener(this);
            screen.addPreference(nonePref);
        }
        if (candidateList != null) {
            for (CandidateInfo info2 : candidateList) {
                RadioButtonPreference pref = new RadioButtonPreference(getPrefContext());
                if (customLayoutResId > 0) {
                    pref.setLayoutResource(customLayoutResId);
                }
                bindPreference(pref, info2.getKey(), info2, defaultKey);
                bindPreferenceExtra(pref, info2.getKey(), info2, defaultKey, systemDefaultKey);
                screen.addPreference(pref);
            }
        }
        mayCheckOnlyRadioButton();
    }

    @VisibleForTesting
    public RadioButtonPreference bindPreference(RadioButtonPreference pref, String key, CandidateInfo info, String defaultKey) {
        pref.setTitle(info.loadLabel());
        Utils.setSafeIcon(pref, info.loadIcon());
        pref.setKey(key);
        if (TextUtils.equals(defaultKey, key)) {
            pref.setChecked(true);
        }
        pref.setEnabled(info.enabled);
        pref.setOnClickListener(this);
        return pref;
    }

    @VisibleForTesting
    public void updateCheckedState(String selectedKey) {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null) {
            int count = screen.getPreferenceCount();
            for (int i = 0; i < count; i++) {
                Preference pref = screen.getPreference(i);
                if (pref instanceof RadioButtonPreference) {
                    RadioButtonPreference radioPref = (RadioButtonPreference) pref;
                    if (radioPref.isChecked() != TextUtils.equals(pref.getKey(), selectedKey)) {
                        radioPref.setChecked(TextUtils.equals(pref.getKey(), selectedKey));
                    }
                }
            }
        }
    }

    @VisibleForTesting
    public void mayCheckOnlyRadioButton() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen != null && screen.getPreferenceCount() == 1) {
            Preference onlyPref = screen.getPreference(null);
            if (onlyPref instanceof RadioButtonPreference) {
                ((RadioButtonPreference) onlyPref).setChecked(true);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public String getSystemDefaultKey() {
        return null;
    }

    /* Access modifiers changed, original: protected */
    @LayoutRes
    public int getRadioButtonPreferenceCustomLayoutResId() {
        return 0;
    }
}
