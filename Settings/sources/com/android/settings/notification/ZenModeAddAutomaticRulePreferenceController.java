package com.android.settings.notification;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.notification.ZenRuleSelectionDialog.PositiveClickListener;
import com.android.settings.utils.ZenServiceListing;
import com.android.settingslib.core.lifecycle.Lifecycle;

public class ZenModeAddAutomaticRulePreferenceController extends AbstractZenModeAutomaticRulePreferenceController implements OnPreferenceClickListener {
    protected static final String KEY = "zen_mode_add_automatic_rule";
    private final ZenServiceListing mZenServiceListing;

    public class RuleSelectionListener implements PositiveClickListener {
        public void onSystemRuleSelected(ZenRuleInfo ri, Fragment parent) {
            ZenModeAddAutomaticRulePreferenceController.this.showNameRuleDialog(ri, parent);
        }

        public void onExternalRuleSelected(ZenRuleInfo ri, Fragment parent) {
            parent.startActivity(new Intent().setComponent(ri.configurationActivity));
        }
    }

    public ZenModeAddAutomaticRulePreferenceController(Context context, Fragment parent, ZenServiceListing serviceListing, Lifecycle lifecycle) {
        super(context, KEY, parent, lifecycle);
        this.mZenServiceListing = serviceListing;
    }

    public String getPreferenceKey() {
        return KEY;
    }

    public boolean isAvailable() {
        return true;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference pref = screen.findPreference(KEY);
        pref.setPersistent(false);
        pref.setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceClick(Preference preference) {
        ZenRuleSelectionDialog.show(this.mContext, this.mParent, new RuleSelectionListener(), this.mZenServiceListing);
        return true;
    }
}
