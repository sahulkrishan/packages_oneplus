package com.android.settings.wifi.p2p;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.widget.ImageView;
import com.android.settings.R;

public class WifiP2pPeer extends Preference {
    private static final int SIGNAL_LEVELS = 4;
    private static final int[] STATE_SECURED = new int[]{R.attr.state_encrypted};
    public WifiP2pDevice device;
    private final int mRssi = 60;
    private ImageView mSignal;

    public WifiP2pPeer(Context context, WifiP2pDevice dev) {
        super(context);
        this.device = dev;
        setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
        if (TextUtils.isEmpty(this.device.deviceName)) {
            setTitle((CharSequence) this.device.deviceAddress);
        } else {
            setTitle((CharSequence) this.device.deviceName);
        }
        setSummary(context.getResources().getStringArray(R.array.wifi_p2p_status)[this.device.status]);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSignal = (ImageView) view.findViewById(R.id.signal);
        if (this.mRssi == Integer.MAX_VALUE) {
            this.mSignal.setImageDrawable(null);
        } else {
            this.mSignal.setImageResource(R.drawable.wifi_signal_dark);
            this.mSignal.setImageState(STATE_SECURED, true);
        }
        this.mSignal.setImageLevel(getLevel());
    }

    public int compareTo(Preference preference) {
        int i = 1;
        if (!(preference instanceof WifiP2pPeer)) {
            return 1;
        }
        WifiP2pPeer other = (WifiP2pPeer) preference;
        if (this.device.status != other.device.status) {
            if (this.device.status < other.device.status) {
                i = -1;
            }
            return i;
        } else if (this.device.deviceName != null) {
            return this.device.deviceName.compareToIgnoreCase(other.device.deviceName);
        } else {
            return this.device.deviceAddress.compareToIgnoreCase(other.device.deviceAddress);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public int getLevel() {
        if (this.mRssi == Integer.MAX_VALUE) {
            return -1;
        }
        return WifiManager.calculateSignalLevel(this.mRssi, 4);
    }
}
