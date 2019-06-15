package com.android.settings.wifi;

import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkCapabilities;
import com.android.internal.util.Preconditions;

final class CaptivePortalNetworkCallback extends NetworkCallback {
    private final ConnectedAccessPointPreference mConnectedApPreference;
    private boolean mIsCaptivePortal;
    private final Network mNetwork;

    CaptivePortalNetworkCallback(Network network, ConnectedAccessPointPreference connectedApPreference) {
        this.mNetwork = (Network) Preconditions.checkNotNull(network);
        this.mConnectedApPreference = (ConnectedAccessPointPreference) Preconditions.checkNotNull(connectedApPreference);
    }

    public void onLost(Network network) {
        if (this.mNetwork.equals(network)) {
            this.mIsCaptivePortal = false;
        }
    }

    public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
        if (this.mNetwork.equals(network)) {
            this.mIsCaptivePortal = WifiUtils.canSignIntoNetwork(networkCapabilities);
            this.mConnectedApPreference.setCaptivePortal(this.mIsCaptivePortal);
        }
    }

    public boolean isSameNetworkAndPreference(Network network, ConnectedAccessPointPreference connectedApPreference) {
        return this.mNetwork.equals(network) && this.mConnectedApPreference == connectedApPreference;
    }

    public boolean isCaptivePortal() {
        return this.mIsCaptivePortal;
    }

    public Network getNetwork() {
        return this.mNetwork;
    }
}
