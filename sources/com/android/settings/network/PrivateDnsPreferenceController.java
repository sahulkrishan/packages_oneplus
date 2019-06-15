package com.android.settings.network;

import android.content.Context;
import android.database.ContentObserver;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.LinkProperties;
import android.net.Network;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings.Global;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

public class PrivateDnsPreferenceController extends BasePreferenceController implements PreferenceControllerMixin, LifecycleObserver, OnStart, OnStop {
    private static final String KEY_PRIVATE_DNS_SETTINGS = "private_dns_settings";
    private static final Uri[] SETTINGS_URIS = new Uri[]{Global.getUriFor("private_dns_mode"), Global.getUriFor("private_dns_default_mode"), Global.getUriFor("private_dns_specifier")};
    private final ConnectivityManager mConnectivityManager;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private LinkProperties mLatestLinkProperties;
    private final NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onLinkPropertiesChanged(Network network, LinkProperties lp) {
            PrivateDnsPreferenceController.this.mLatestLinkProperties = lp;
            if (PrivateDnsPreferenceController.this.mPreference != null) {
                PrivateDnsPreferenceController.this.updateState(PrivateDnsPreferenceController.this.mPreference);
            }
        }

        public void onLost(Network network) {
            PrivateDnsPreferenceController.this.mLatestLinkProperties = null;
            if (PrivateDnsPreferenceController.this.mPreference != null) {
                PrivateDnsPreferenceController.this.updateState(PrivateDnsPreferenceController.this.mPreference);
            }
        }
    };
    private Preference mPreference;
    private final ContentObserver mSettingsObserver = new PrivateDnsSettingsObserver(this.mHandler);

    private class PrivateDnsSettingsObserver extends ContentObserver {
        public PrivateDnsSettingsObserver(Handler h) {
            super(h);
        }

        public void onChange(boolean selfChange) {
            if (PrivateDnsPreferenceController.this.mPreference != null) {
                PrivateDnsPreferenceController.this.updateState(PrivateDnsPreferenceController.this.mPreference);
            }
        }
    }

    public PrivateDnsPreferenceController(Context context) {
        super(context, KEY_PRIVATE_DNS_SETTINGS);
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
    }

    public String getPreferenceKey() {
        return KEY_PRIVATE_DNS_SETTINGS;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = screen.findPreference(getPreferenceKey());
    }

    public void onStart() {
        for (Uri uri : SETTINGS_URIS) {
            this.mContext.getContentResolver().registerContentObserver(uri, false, this.mSettingsObserver);
        }
        Network defaultNetwork = this.mConnectivityManager.getActiveNetwork();
        if (defaultNetwork != null) {
            this.mLatestLinkProperties = this.mConnectivityManager.getLinkProperties(defaultNetwork);
        }
        this.mConnectivityManager.registerDefaultNetworkCallback(this.mNetworkCallback, this.mHandler);
    }

    public void onStop() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingsObserver);
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
    }

    /* JADX WARNING: Removed duplicated region for block: B:20:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0059  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x007a  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0068  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x0059  */
    /* JADX WARNING: Missing block: B:17:0x004f, code skipped:
            if (r2.equals("opportunistic") != false) goto L_0x0053;
     */
    public java.lang.CharSequence getSummary() {
        /*
        r10 = this;
        r0 = r10.mContext;
        r0 = r0.getResources();
        r1 = r10.mContext;
        r1 = r1.getContentResolver();
        r2 = com.android.settings.network.PrivateDnsModeDialogPreference.getModeFromSettings(r1);
        r3 = r10.mLatestLinkProperties;
        if (r3 != 0) goto L_0x0016;
    L_0x0014:
        r4 = 0;
        goto L_0x001a;
    L_0x0016:
        r4 = r3.getValidatedPrivateDnsServers();
    L_0x001a:
        r5 = com.android.internal.util.ArrayUtils.isEmpty(r4);
        r6 = 1;
        r5 = r5 ^ r6;
        r7 = -1;
        r8 = r2.hashCode();
        r9 = -539229175; // 0xffffffffdfdc0409 float:-3.1707613E19 double:NaN;
        if (r8 == r9) goto L_0x0049;
    L_0x002a:
        r6 = -299803597; // 0xffffffffee215c33 float:-1.2484637E28 double:NaN;
        if (r8 == r6) goto L_0x003f;
    L_0x002f:
        r6 = 109935; // 0x1ad6f float:1.54052E-40 double:5.4315E-319;
        if (r8 == r6) goto L_0x0035;
    L_0x0034:
        goto L_0x0052;
    L_0x0035:
        r6 = "off";
        r6 = r2.equals(r6);
        if (r6 == 0) goto L_0x0052;
    L_0x003d:
        r6 = 0;
        goto L_0x0053;
    L_0x003f:
        r6 = "hostname";
        r6 = r2.equals(r6);
        if (r6 == 0) goto L_0x0052;
    L_0x0047:
        r6 = 2;
        goto L_0x0053;
    L_0x0049:
        r8 = "opportunistic";
        r8 = r2.equals(r8);
        if (r8 == 0) goto L_0x0052;
    L_0x0051:
        goto L_0x0053;
    L_0x0052:
        r6 = r7;
    L_0x0053:
        switch(r6) {
            case 0: goto L_0x007a;
            case 1: goto L_0x0068;
            case 2: goto L_0x0059;
            default: goto L_0x0056;
        };
    L_0x0056:
        r6 = "";
        return r6;
    L_0x0059:
        if (r5 == 0) goto L_0x0060;
    L_0x005b:
        r6 = com.android.settings.network.PrivateDnsModeDialogPreference.getHostnameFromSettings(r1);
        goto L_0x0067;
    L_0x0060:
        r6 = 2131889951; // 0x7f120f1f float:1.941458E38 double:1.0532935855E-314;
        r6 = r0.getString(r6);
    L_0x0067:
        return r6;
    L_0x0068:
        if (r5 == 0) goto L_0x0072;
    L_0x006a:
        r6 = 2131890778; // 0x7f12125a float:1.9416257E38 double:1.053293994E-314;
        r6 = r0.getString(r6);
        goto L_0x0079;
    L_0x0072:
        r6 = 2131889949; // 0x7f120f1d float:1.9414576E38 double:1.0532935845E-314;
        r6 = r0.getString(r6);
    L_0x0079:
        return r6;
    L_0x007a:
        r6 = 2131889948; // 0x7f120f1c float:1.9414574E38 double:1.053293584E-314;
        r6 = r0.getString(r6);
        return r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.network.PrivateDnsPreferenceController.getSummary():java.lang.CharSequence");
    }
}
