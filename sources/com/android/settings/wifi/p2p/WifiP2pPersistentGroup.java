package com.android.settings.wifi.p2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pGroup;
import android.support.v7.preference.Preference;

public class WifiP2pPersistentGroup extends Preference {
    public WifiP2pGroup mGroup;

    public WifiP2pPersistentGroup(Context context, WifiP2pGroup group) {
        super(context);
        this.mGroup = group;
        setTitle((CharSequence) this.mGroup.getNetworkName());
    }

    /* Access modifiers changed, original: 0000 */
    public int getNetworkId() {
        return this.mGroup.getNetworkId();
    }

    /* Access modifiers changed, original: 0000 */
    public String getGroupName() {
        return this.mGroup.getNetworkName();
    }
}
