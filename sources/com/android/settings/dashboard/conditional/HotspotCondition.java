package com.android.settings.dashboard.conditional;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.UserHandle;
import com.android.settings.R;
import com.android.settings.TetherSettings;
import com.android.settings.core.SubSettingLauncher;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class HotspotCondition extends Condition {
    private static final IntentFilter WIFI_AP_STATE_FILTER = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
    private final Receiver mReceiver = new Receiver();
    private final WifiManager mWifiManager = ((WifiManager) this.mManager.getContext().getSystemService(WifiManager.class));

    public static class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(intent.getAction())) {
                ((HotspotCondition) ConditionManager.get(context).getCondition(HotspotCondition.class)).refreshState();
            }
        }
    }

    public HotspotCondition(ConditionManager manager) {
        super(manager);
    }

    public void refreshState() {
        setActive(this.mWifiManager.isWifiApEnabled());
    }

    /* Access modifiers changed, original: protected */
    public BroadcastReceiver getReceiver() {
        return this.mReceiver;
    }

    /* Access modifiers changed, original: protected */
    public IntentFilter getIntentFilter() {
        return WIFI_AP_STATE_FILTER;
    }

    public Drawable getIcon() {
        return this.mManager.getContext().getDrawable(R.drawable.ic_hotspot);
    }

    private String getSsid() {
        WifiConfiguration wifiConfig = this.mWifiManager.getWifiApConfiguration();
        if (wifiConfig == null) {
            return this.mManager.getContext().getString(17041179);
        }
        return wifiConfig.SSID;
    }

    public CharSequence getTitle() {
        return this.mManager.getContext().getString(R.string.condition_hotspot_title);
    }

    public CharSequence getSummary() {
        return this.mManager.getContext().getString(R.string.condition_hotspot_summary, new Object[]{getSsid()});
    }

    public CharSequence[] getActions() {
        if (RestrictedLockUtils.hasBaseUserRestriction(this.mManager.getContext(), "no_config_tethering", UserHandle.myUserId())) {
            return new CharSequence[0];
        }
        return new CharSequence[]{this.mManager.getContext().getString(R.string.condition_turn_off)};
    }

    public void onPrimaryClick() {
        new SubSettingLauncher(this.mManager.getContext()).setDestination(TetherSettings.class.getName()).setSourceMetricsCategory(35).setTitle((int) R.string.tether_settings_title_all).addFlags(268435456).launch();
    }

    public void onActionClick(int index) {
        if (index == 0) {
            Context context = this.mManager.getContext();
            EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(context, "no_config_tethering", UserHandle.myUserId());
            if (admin != null) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(context, admin);
                return;
            }
            try {
                ((ConnectivityManager) context.getSystemService("connectivity")).stopTethering(0);
                setActive(false);
                return;
            } catch (SecurityException e) {
                e.printStackTrace();
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unexpected index ");
        stringBuilder.append(index);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public int getMetricsConstant() {
        return 382;
    }
}
