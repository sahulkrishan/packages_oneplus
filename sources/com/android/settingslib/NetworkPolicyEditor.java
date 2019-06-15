package com.android.settingslib;

import android.net.NetworkPolicy;
import android.net.NetworkPolicyManager;
import android.net.NetworkTemplate;
import android.net.wifi.WifiInfo;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.RecurrenceRule;
import com.android.internal.util.Preconditions;
import com.google.android.collect.Lists;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;

public class NetworkPolicyEditor {
    public static final boolean ENABLE_SPLIT_POLICIES = false;
    private ArrayList<NetworkPolicy> mPolicies = Lists.newArrayList();
    private NetworkPolicyManager mPolicyManager;

    public NetworkPolicyEditor(NetworkPolicyManager policyManager) {
        this.mPolicyManager = (NetworkPolicyManager) Preconditions.checkNotNull(policyManager);
    }

    public void read() {
        NetworkPolicy[] policies = this.mPolicyManager.getNetworkPolicies();
        boolean modified = false;
        this.mPolicies.clear();
        for (NetworkPolicy policy : policies) {
            if (policy.limitBytes < -1) {
                policy.limitBytes = -1;
                modified = true;
            }
            if (policy.warningBytes < -1) {
                policy.warningBytes = -1;
                modified = true;
            }
            this.mPolicies.add(policy);
        }
        if (modified) {
            writeAsync();
        }
    }

    public void writeAsync() {
        final NetworkPolicy[] policies = (NetworkPolicy[]) this.mPolicies.toArray(new NetworkPolicy[this.mPolicies.size()]);
        new AsyncTask<Void, Void, Void>() {
            /* Access modifiers changed, original: protected|varargs */
            public Void doInBackground(Void... params) {
                NetworkPolicyEditor.this.write(policies);
                return null;
            }
        }.execute(new Void[0]);
    }

    public void write(NetworkPolicy[] policies) {
        this.mPolicyManager.setNetworkPolicies(policies);
    }

    public boolean hasLimitedPolicy(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        return (policy == null || policy.limitBytes == -1) ? false : true;
    }

    public NetworkPolicy getOrCreatePolicy(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        if (policy != null) {
            return policy;
        }
        policy = buildDefaultPolicy(template);
        this.mPolicies.add(policy);
        return policy;
    }

    public NetworkPolicy getPolicy(NetworkTemplate template) {
        Iterator it = this.mPolicies.iterator();
        while (it.hasNext()) {
            NetworkPolicy policy = (NetworkPolicy) it.next();
            if (policy.template.equals(template)) {
                return policy;
            }
        }
        return null;
    }

    public NetworkPolicy getPolicyMaybeUnquoted(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        if (policy != null) {
            return policy;
        }
        return getPolicy(buildUnquotedNetworkTemplate(template));
    }

    @Deprecated
    private static NetworkPolicy buildDefaultPolicy(NetworkTemplate template) {
        RecurrenceRule cycleRule;
        boolean z;
        if (template.getMatchRule() == 4) {
            cycleRule = RecurrenceRule.buildNever();
            z = false;
        } else {
            cycleRule = RecurrenceRule.buildRecurringMonthly(ZonedDateTime.now().getDayOfMonth(), ZoneId.systemDefault());
            z = true;
        }
        return new NetworkPolicy(template, cycleRule, -1, -1, -1, -1, z, true);
    }

    @Deprecated
    public int getPolicyCycleDay(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        if (policy == null || !policy.cycleRule.isMonthly()) {
            return -1;
        }
        return policy.cycleRule.start.getDayOfMonth();
    }

    @Deprecated
    public void setPolicyCycleDay(NetworkTemplate template, int cycleDay, String cycleTimezone) {
        NetworkPolicy policy = getOrCreatePolicy(template);
        policy.cycleRule = NetworkPolicy.buildRule(cycleDay, ZoneId.of(cycleTimezone));
        policy.inferred = false;
        policy.clearSnooze();
        writeAsync();
    }

    public long getPolicyWarningBytes(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        return policy != null ? policy.warningBytes : -1;
    }

    private void setPolicyWarningBytesInner(NetworkTemplate template, long warningBytes) {
        NetworkPolicy policy = getOrCreatePolicy(template);
        policy.warningBytes = warningBytes;
        policy.inferred = false;
        policy.clearSnooze();
        writeAsync();
    }

    public void setPolicyWarningBytes(NetworkTemplate template, long warningBytes) {
        long limitBytes = getPolicyLimitBytes(template);
        setPolicyWarningBytesInner(template, limitBytes == -1 ? warningBytes : Math.min(warningBytes, limitBytes));
    }

    public long getPolicyLimitBytes(NetworkTemplate template) {
        NetworkPolicy policy = getPolicy(template);
        return policy != null ? policy.limitBytes : -1;
    }

    public void setPolicyLimitBytes(NetworkTemplate template, long limitBytes) {
        if (getPolicyWarningBytes(template) > limitBytes && limitBytes != -1) {
            setPolicyWarningBytesInner(template, limitBytes);
        }
        NetworkPolicy policy = getOrCreatePolicy(template);
        policy.limitBytes = limitBytes;
        policy.inferred = false;
        policy.clearSnooze();
        writeAsync();
    }

    private static NetworkTemplate buildUnquotedNetworkTemplate(NetworkTemplate template) {
        if (template == null) {
            return null;
        }
        String networkId = template.getNetworkId();
        String strippedNetworkId = WifiInfo.removeDoubleQuotes(networkId);
        if (TextUtils.equals(strippedNetworkId, networkId)) {
            return null;
        }
        return new NetworkTemplate(template.getMatchRule(), template.getSubscriberId(), strippedNetworkId);
    }
}
