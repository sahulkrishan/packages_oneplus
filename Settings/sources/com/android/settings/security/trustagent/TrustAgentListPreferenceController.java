package com.android.settings.security.trustagent;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settings.security.SecurityFeatureProvider;
import com.android.settings.security.SecuritySettings;
import com.android.settings.security.trustagent.TrustAgentManager.TrustAgentComponentInfo;
import com.android.settingslib.RestrictedPreference;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreate;
import com.android.settingslib.core.lifecycle.events.OnResume;
import com.android.settingslib.core.lifecycle.events.OnSaveInstanceState;
import java.util.List;

public class TrustAgentListPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnSaveInstanceState, OnCreate, OnResume {
    private static final int MY_USER_ID = UserHandle.myUserId();
    @VisibleForTesting
    static final String PREF_KEY_SECURITY_CATEGORY = "security_category";
    @VisibleForTesting
    static final String PREF_KEY_TRUST_AGENT = "trust_agent";
    private static final String TRUST_AGENT_CLICK_INTENT = "trust_agent_click_intent";
    private final SecuritySettings mHost;
    private final LockPatternUtils mLockPatternUtils;
    private PreferenceCategory mSecurityCategory;
    private Intent mTrustAgentClickIntent;
    private final TrustAgentManager mTrustAgentManager;

    public TrustAgentListPreferenceController(Context context, SecuritySettings host, Lifecycle lifecycle) {
        super(context);
        SecurityFeatureProvider provider = FeatureFactory.getFactory(context).getSecurityFeatureProvider();
        this.mHost = host;
        this.mLockPatternUtils = provider.getLockPatternUtils(context);
        this.mTrustAgentManager = provider.getTrustAgentManager();
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public boolean isAvailable() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_trust_agent_click_intent);
    }

    public String getPreferenceKey() {
        return PREF_KEY_TRUST_AGENT;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mSecurityCategory = (PreferenceCategory) screen.findPreference(PREF_KEY_SECURITY_CATEGORY);
        updateTrustAgents();
    }

    public void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey(TRUST_AGENT_CLICK_INTENT)) {
            this.mTrustAgentClickIntent = (Intent) savedInstanceState.getParcelable(TRUST_AGENT_CLICK_INTENT);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (this.mTrustAgentClickIntent != null) {
            outState.putParcelable(TRUST_AGENT_CLICK_INTENT, this.mTrustAgentClickIntent);
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return super.handlePreferenceTreeClick(preference);
        }
        ChooseLockSettingsHelper helper = new ChooseLockSettingsHelper(this.mHost.getActivity(), this.mHost);
        this.mTrustAgentClickIntent = preference.getIntent();
        if (!(helper.launchConfirmationActivity(true, preference.getTitle()) || this.mTrustAgentClickIntent == null)) {
            this.mHost.startActivity(this.mTrustAgentClickIntent);
            this.mTrustAgentClickIntent = null;
        }
        return true;
    }

    public void onResume() {
        updateTrustAgents();
    }

    private void updateTrustAgents() {
        if (this.mSecurityCategory != null) {
            Preference oldAgent;
            while (true) {
                oldAgent = this.mSecurityCategory.findPreference(PREF_KEY_TRUST_AGENT);
                if (oldAgent == null) {
                    break;
                }
                this.mSecurityCategory.removePreference(oldAgent);
            }
            if (isAvailable()) {
                oldAgent = this.mLockPatternUtils.isSecure(MY_USER_ID);
                List<TrustAgentComponentInfo> agents = this.mTrustAgentManager.getActiveTrustAgents(this.mContext, this.mLockPatternUtils);
                if (agents != null) {
                    for (TrustAgentComponentInfo agent : agents) {
                        RestrictedPreference trustAgentPreference = new RestrictedPreference(this.mSecurityCategory.getContext());
                        trustAgentPreference.setKey(PREF_KEY_TRUST_AGENT);
                        trustAgentPreference.setTitle((CharSequence) agent.title);
                        trustAgentPreference.setSummary((CharSequence) agent.summary);
                        trustAgentPreference.setIntent(new Intent("android.intent.action.MAIN").setComponent(agent.componentName));
                        trustAgentPreference.setDisabledByAdmin(agent.admin);
                        if (!trustAgentPreference.isDisabledByAdmin() && oldAgent == null) {
                            trustAgentPreference.setEnabled(false);
                            trustAgentPreference.setSummary((int) R.string.disabled_because_no_backup_security);
                        }
                        this.mSecurityCategory.addPreference(trustAgentPreference);
                    }
                }
            }
        }
    }

    public boolean handleActivityResult(int requestCode, int resultCode) {
        if (requestCode != SecuritySettings.CHANGE_TRUST_AGENT_SETTINGS || resultCode != -1) {
            return false;
        }
        if (this.mTrustAgentClickIntent != null) {
            this.mHost.startActivity(this.mTrustAgentClickIntent);
            this.mTrustAgentClickIntent = null;
        }
        return true;
    }
}
