package com.android.settings.security.trustagent;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import java.util.List;

public class TrustAgentSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private static final String SERVICE_INTERFACE = "android.service.trust.TrustAgentService";
    private final ArraySet<ComponentName> mActiveAgents = new ArraySet();
    private ArrayMap<ComponentName, AgentInfo> mAvailableAgents;
    private DevicePolicyManager mDpm;
    private LockPatternUtils mLockPatternUtils;
    private TrustAgentManager mTrustAgentManager;

    public static final class AgentInfo {
        ComponentName component;
        public Drawable icon;
        CharSequence label;
        SwitchPreference preference;

        public boolean equals(Object other) {
            if (other instanceof AgentInfo) {
                return this.component.equals(((AgentInfo) other).component);
            }
            return true;
        }

        public int compareTo(AgentInfo other) {
            return this.component.compareTo(other.component);
        }
    }

    public int getMetricsCategory() {
        return 91;
    }

    public int getHelpResource() {
        return R.string.help_url_trust_agent;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService(DevicePolicyManager.class);
        this.mTrustAgentManager = FeatureFactory.getFactory(getActivity()).getSecurityFeatureProvider().getTrustAgentManager();
        addPreferencesFromResource(R.xml.trust_agent_settings);
    }

    public void onResume() {
        super.onResume();
        removePreference("dummy_preference");
        updateAgents();
    }

    private void updateAgents() {
        Context context = getActivity();
        if (this.mAvailableAgents == null) {
            this.mAvailableAgents = findAvailableTrustAgents();
        }
        if (this.mLockPatternUtils == null) {
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
        }
        loadActiveAgents();
        PreferenceGroup category = (PreferenceGroup) getPreferenceScreen().findPreference("trust_agents");
        category.removeAll();
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(context, 16, UserHandle.myUserId());
        int count = this.mAvailableAgents.size();
        for (int i = 0; i < count; i++) {
            AgentInfo agent = (AgentInfo) this.mAvailableAgents.valueAt(i);
            RestrictedSwitchPreference preference = new RestrictedSwitchPreference(getPrefContext());
            preference.useAdminDisabledSummary(true);
            agent.preference = preference;
            preference.setPersistent(false);
            preference.setTitle(agent.label);
            preference.setIcon(agent.icon);
            preference.setPersistent(false);
            preference.setOnPreferenceChangeListener(this);
            preference.setChecked(this.mActiveAgents.contains(agent.component));
            if (admin != null && this.mDpm.getTrustAgentConfiguration(null, agent.component) == null) {
                preference.setChecked(false);
                preference.setDisabledByAdmin(admin);
            }
            category.addPreference(agent.preference);
        }
    }

    private void loadActiveAgents() {
        List<ComponentName> activeTrustAgents = this.mLockPatternUtils.getEnabledTrustAgents(UserHandle.myUserId());
        if (activeTrustAgents != null) {
            this.mActiveAgents.addAll(activeTrustAgents);
        }
    }

    private void saveActiveAgents() {
        this.mLockPatternUtils.setEnabledTrustAgents(this.mActiveAgents, UserHandle.myUserId());
    }

    private ArrayMap<ComponentName, AgentInfo> findAvailableTrustAgents() {
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent(SERVICE_INTERFACE), 128);
        ArrayMap<ComponentName, AgentInfo> agents = new ArrayMap();
        int count = resolveInfos.size();
        agents.ensureCapacity(count);
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (resolveInfo.serviceInfo != null && this.mTrustAgentManager.shouldProvideTrust(resolveInfo, pm)) {
                ComponentName name = this.mTrustAgentManager.getComponentName(resolveInfo);
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.label = resolveInfo.loadLabel(pm);
                agentInfo.icon = resolveInfo.loadIcon(pm);
                agentInfo.component = name;
                agents.put(name, agentInfo);
            }
        }
        return agents;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof SwitchPreference) {
            int count = this.mAvailableAgents.size();
            for (int i = 0; i < count; i++) {
                AgentInfo agent = (AgentInfo) this.mAvailableAgents.valueAt(i);
                if (agent.preference == preference) {
                    if (!((Boolean) newValue).booleanValue()) {
                        this.mActiveAgents.remove(agent.component);
                    } else if (!this.mActiveAgents.contains(agent.component)) {
                        this.mActiveAgents.add(agent.component);
                    }
                    saveActiveAgents();
                    return true;
                }
            }
        }
        return false;
    }
}
