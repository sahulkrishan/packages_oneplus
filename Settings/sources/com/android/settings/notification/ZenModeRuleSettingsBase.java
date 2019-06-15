package com.android.settings.notification;

import android.app.AutomaticZenRule;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settingslib.core.AbstractPreferenceController;

public abstract class ZenModeRuleSettingsBase extends ZenModeSettingsBase {
    protected static final boolean DEBUG = ZenModeSettingsBase.DEBUG;
    protected static final String TAG = "ZenModeSettings";
    protected Context mContext;
    protected boolean mDisableListeners;
    protected ZenAutomaticRuleHeaderPreferenceController mHeader;
    protected String mId;
    protected AutomaticZenRule mRule;
    protected ZenAutomaticRuleSwitchPreferenceController mSwitch;

    public abstract void onCreateInternal();

    public abstract boolean setRule(AutomaticZenRule automaticZenRule);

    public abstract void updateControlsInternal();

    public void onCreate(Bundle icicle) {
        String str;
        StringBuilder stringBuilder;
        this.mContext = getActivity();
        Intent intent = getActivity().getIntent();
        if (DEBUG) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("onCreate getIntent()=");
            stringBuilder.append(intent);
            Log.d(str, stringBuilder.toString());
        }
        if (intent == null) {
            Log.w(TAG, "No intent");
            toastAndFinish();
            return;
        }
        this.mId = intent.getStringExtra("android.service.notification.extra.RULE_ID");
        if (this.mId == null) {
            Log.w(TAG, "rule id is null");
            toastAndFinish();
            return;
        }
        if (DEBUG) {
            str = TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("mId=");
            stringBuilder.append(this.mId);
            Log.d(str, stringBuilder.toString());
        }
        if (!refreshRuleOrFinish()) {
            super.onCreate(icicle);
            onCreateInternal();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        getActivity().finish();
        return true;
    }

    public void onResume() {
        super.onResume();
        if (!isUiRestricted()) {
            updateControls();
        }
    }

    public int getHelpResource() {
        return R.string.help_uri_interruptions;
    }

    /* Access modifiers changed, original: protected */
    public void updateHeader() {
        PreferenceScreen screen = getPreferenceScreen();
        this.mSwitch.onResume(this.mRule, this.mId);
        this.mSwitch.displayPreference(screen);
        updatePreference(this.mSwitch);
        this.mHeader.onResume(this.mRule, this.mId);
        this.mHeader.displayPreference(screen);
        updatePreference(this.mHeader);
    }

    private void updatePreference(AbstractPreferenceController controller) {
        PreferenceScreen screen = getPreferenceScreen();
        if (controller.isAvailable()) {
            Preference preference = screen.findPreference(controller.getPreferenceKey());
            if (preference == null) {
                Log.d(TAG, String.format("Cannot find preference with key %s in Controller %s", new Object[]{key, controller.getClass().getSimpleName()}));
                return;
            }
            controller.updateState(preference);
        }
    }

    /* Access modifiers changed, original: protected */
    public void updateRule(Uri newConditionId) {
        this.mRule.setConditionId(newConditionId);
        this.mBackend.setZenRule(this.mId, this.mRule);
    }

    /* Access modifiers changed, original: protected */
    public void onZenModeConfigChanged() {
        super.onZenModeConfigChanged();
        if (!refreshRuleOrFinish()) {
            updateControls();
        }
    }

    private boolean refreshRuleOrFinish() {
        this.mRule = getZenRule();
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mRule=");
            stringBuilder.append(this.mRule);
            Log.d(str, stringBuilder.toString());
        }
        if (setRule(this.mRule)) {
            return false;
        }
        toastAndFinish();
        return true;
    }

    private void toastAndFinish() {
        Toast.makeText(this.mContext, R.string.zen_mode_rule_not_found_text, 0).show();
        getActivity().finish();
    }

    private AutomaticZenRule getZenRule() {
        return NotificationManager.from(this.mContext).getAutomaticZenRule(this.mId);
    }

    private void updateControls() {
        this.mDisableListeners = true;
        updateControlsInternal();
        updateHeader();
        this.mDisableListeners = false;
    }
}
