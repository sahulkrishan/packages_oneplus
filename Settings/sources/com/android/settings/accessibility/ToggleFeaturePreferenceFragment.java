package com.android.settings.accessibility;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.View;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.ToggleSwitch;

public abstract class ToggleFeaturePreferenceFragment extends SettingsPreferenceFragment {
    protected String mPreferenceKey;
    protected Intent mSettingsIntent;
    protected CharSequence mSettingsTitle;
    protected SwitchBar mSwitchBar;
    protected ToggleSwitch mToggleSwitch;

    public abstract void onPreferenceToggled(String str, boolean z);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getPreferenceScreenResId() <= 0) {
            setPreferenceScreen(getPreferenceManager().createPreferenceScreen(getActivity()));
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mSwitchBar = ((SettingsActivity) getActivity()).getSwitchBar();
        updateSwitchBarText(this.mSwitchBar);
        this.mToggleSwitch = this.mSwitchBar.getSwitch();
        onProcessArguments(getArguments());
        if (this.mSettingsTitle != null && this.mSettingsIntent != null) {
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            Preference settingsPref = new Preference(preferenceScreen.getContext());
            settingsPref.setTitle(this.mSettingsTitle);
            settingsPref.setIconSpaceReserved(true);
            settingsPref.setIntent(this.mSettingsIntent);
            preferenceScreen.addPreference(settingsPref);
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        installActionBarToggleSwitch();
    }

    public void onDestroyView() {
        super.onDestroyView();
        removeActionBarToggleSwitch();
    }

    /* Access modifiers changed, original: protected */
    public void updateSwitchBarText(SwitchBar switchBar) {
        switchBar.setSwitchBarText(R.string.accessibility_service_master_switch_title, R.string.accessibility_service_master_switch_title);
    }

    /* Access modifiers changed, original: protected */
    public void onInstallSwitchBarToggleSwitch() {
    }

    /* Access modifiers changed, original: protected */
    public void onRemoveSwitchBarToggleSwitch() {
    }

    private void installActionBarToggleSwitch() {
        this.mSwitchBar.show();
        onInstallSwitchBarToggleSwitch();
    }

    private void removeActionBarToggleSwitch() {
        this.mToggleSwitch.setOnBeforeCheckedChangeListener(null);
        onRemoveSwitchBarToggleSwitch();
        this.mSwitchBar.hide();
    }

    public void setTitle(String title) {
        getActivity().setTitle(title);
    }

    /* Access modifiers changed, original: protected */
    public void onProcessArguments(Bundle arguments) {
        this.mPreferenceKey = arguments.getString("preference_key");
        if (arguments.containsKey("checked")) {
            this.mSwitchBar.setCheckedInternal(arguments.getBoolean("checked"));
        }
        if (arguments.containsKey("resolve_info")) {
            getActivity().setTitle(((ResolveInfo) arguments.getParcelable("resolve_info")).loadLabel(getPackageManager()).toString());
        } else if (arguments.containsKey("title")) {
            setTitle(arguments.getString("title"));
        }
        if (arguments.containsKey("summary_res")) {
            this.mFooterPreferenceMixin.createFooterPreference().setTitle(arguments.getInt("summary_res"));
        } else if (arguments.containsKey("summary")) {
            this.mFooterPreferenceMixin.createFooterPreference().setTitle(arguments.getCharSequence("summary"));
        }
    }
}
