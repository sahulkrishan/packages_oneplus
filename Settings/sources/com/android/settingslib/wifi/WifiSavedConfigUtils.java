package com.android.settingslib.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.hotspot2.PasspointConfiguration;
import java.util.ArrayList;
import java.util.List;

public class WifiSavedConfigUtils {
    public static List<AccessPoint> getAllConfigs(Context context, WifiManager wifiManager) {
        List<AccessPoint> savedConfigs = new ArrayList();
        for (WifiConfiguration network : wifiManager.getConfiguredNetworks()) {
            if (!network.isPasspoint()) {
                if (!network.isEphemeral()) {
                    savedConfigs.add(new AccessPoint(context, network));
                }
            }
        }
        try {
            for (PasspointConfiguration config : wifiManager.getPasspointConfigurations()) {
                savedConfigs.add(new AccessPoint(context, config));
            }
        } catch (UnsupportedOperationException e) {
        }
        return savedConfigs;
    }
}
