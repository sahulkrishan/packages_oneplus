package com.android.settings.security.trustagent;

import android.content.Context;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.security.SecurityFeatureProvider;

public class ManageTrustAgentsPreferenceController extends BasePreferenceController {
    @VisibleForTesting
    static final String KEY_MANAGE_TRUST_AGENTS = "manage_trust_agents";
    private static final int MY_USER_ID = UserHandle.myUserId();
    private final LockPatternUtils mLockPatternUtils;
    private TrustAgentManager mTrustAgentManager;

    public ManageTrustAgentsPreferenceController(Context context) {
        super(context, KEY_MANAGE_TRUST_AGENTS);
        SecurityFeatureProvider securityFeatureProvider = FeatureFactory.getFactory(context).getSecurityFeatureProvider();
        this.mLockPatternUtils = securityFeatureProvider.getLockPatternUtils(context);
        this.mTrustAgentManager = securityFeatureProvider.getTrustAgentManager();
    }

    public int getAvailabilityStatus() {
        return this.mContext.getResources().getBoolean(R.bool.config_show_manage_trust_agents) ? 0 : 2;
    }

    public void updateState(Preference preference) {
        int numberOfTrustAgent = getTrustAgentCount();
        if (!this.mLockPatternUtils.isSecure(MY_USER_ID)) {
            preference.setEnabled(false);
            preference.setSummary((int) R.string.disabled_because_no_backup_security);
        } else if (numberOfTrustAgent > 0) {
            preference.setEnabled(true);
            preference.setSummary(this.mContext.getResources().getQuantityString(R.plurals.manage_trust_agents_summary_on, numberOfTrustAgent, new Object[]{Integer.valueOf(numberOfTrustAgent)}));
        } else {
            preference.setEnabled(true);
            preference.setSummary((int) R.string.manage_trust_agents_summary);
        }
    }

    private int getTrustAgentCount() {
        return this.mTrustAgentManager.getActiveTrustAgents(this.mContext, this.mLockPatternUtils).size();
    }
}
