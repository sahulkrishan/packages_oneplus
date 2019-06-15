package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.Fragment;
import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.widget.Switch;
import com.android.settings.R;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.widget.SwitchBar;
import com.android.settings.widget.SwitchBar.OnSwitchChangeListener;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenAutomaticRuleSwitchPreferenceController extends AbstractZenModeAutomaticRulePreferenceController implements OnSwitchChangeListener {
    private static final String KEY = "zen_automatic_rule_switch";
    private String mId;
    private AutomaticZenRule mRule;
    private SwitchBar mSwitchBar;

    public ZenAutomaticRuleSwitchPreferenceController(Context context, Fragment parent, Lifecycle lifecycle) {
        super(context, KEY, parent, lifecycle);
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return (this.mRule == null || this.mId == null) ? false : true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSwitchBar = (SwitchBar) ((LayoutPreference) screen.findPreference(KEY)).findViewById(R.id.switch_bar);
        if (this.mSwitchBar != null) {
            this.mSwitchBar.setSwitchBarText(R.string.zen_mode_use_automatic_rule, R.string.zen_mode_use_automatic_rule);
            try {
                this.mSwitchBar.addOnSwitchChangeListener(this);
            } catch (IllegalStateException e) {
            }
            this.mSwitchBar.show();
        }
    }

    public void onResume(AutomaticZenRule rule, String id) {
        this.mRule = rule;
        this.mId = id;
    }

    public void updateState(Preference preference) {
        if (this.mRule != null) {
            this.mSwitchBar.setChecked(this.mRule.isEnabled());
        }
    }

    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        boolean enabled = isChecked;
        if (enabled != this.mRule.isEnabled()) {
            this.mRule.setEnabled(enabled);
            this.mBackend.setZenRule(this.mId, this.mRule);
        }
    }
}
