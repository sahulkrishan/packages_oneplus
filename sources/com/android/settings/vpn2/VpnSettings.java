package com.android.settings.vpn2;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.widget.GearPreference;
import com.android.settings.widget.GearPreference.OnGearClickListener;
import com.android.settingslib.RestrictedLockUtils;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VpnSettings extends RestrictedSettingsFragment implements Callback, OnPreferenceClickListener {
    private static final String LOG_TAG = "VpnSettings";
    private static final int RESCAN_INTERVAL_MS = 1000;
    private static final int RESCAN_MESSAGE = 0;
    private static final NetworkRequest VPN_REQUEST = new Builder().removeCapability(15).removeCapability(13).removeCapability(14).build();
    private Map<AppVpnInfo, AppPreference> mAppPreferences = new ArrayMap();
    private LegacyVpnInfo mConnectedLegacyVpn;
    private ConnectivityManager mConnectivityManager;
    private final IConnectivityManager mConnectivityService = Stub.asInterface(ServiceManager.getService("connectivity"));
    private OnGearClickListener mGearListener = new OnGearClickListener() {
        public void onGearClick(GearPreference p) {
            if (p instanceof LegacyVpnPreference) {
                ConfigDialogFragment.show(VpnSettings.this, ((LegacyVpnPreference) p).getProfile(), true, true);
            } else if (p instanceof AppPreference) {
                AppManagementFragment.show(VpnSettings.this.getPrefContext(), (AppPreference) p, VpnSettings.this.getMetricsCategory());
            }
        }
    };
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private Map<String, LegacyVpnPreference> mLegacyVpnPreferences = new ArrayMap();
    private NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onAvailable(Network network) {
            if (VpnSettings.this.mUpdater != null) {
                VpnSettings.this.mUpdater.sendEmptyMessage(0);
            }
        }

        public void onLost(Network network) {
            if (VpnSettings.this.mUpdater != null) {
                VpnSettings.this.mUpdater.sendEmptyMessage(0);
            }
        }
    };
    private boolean mUnavailable;
    @GuardedBy("this")
    private Handler mUpdater;
    private HandlerThread mUpdaterThread;
    private UserManager mUserManager;

    @VisibleForTesting
    static class UpdatePreferences implements Runnable {
        private Set<AppVpnInfo> alwaysOnAppVpnInfos = Collections.emptySet();
        private Set<AppVpnInfo> connectedAppVpns = Collections.emptySet();
        private Map<String, LegacyVpnInfo> connectedLegacyVpns = Collections.emptyMap();
        private String lockdownVpnKey = null;
        private final VpnSettings mSettings;
        private List<AppVpnInfo> vpnApps = Collections.emptyList();
        private List<VpnProfile> vpnProfiles = Collections.emptyList();

        public UpdatePreferences(VpnSettings settings) {
            this.mSettings = settings;
        }

        public final UpdatePreferences legacyVpns(List<VpnProfile> vpnProfiles, Map<String, LegacyVpnInfo> connectedLegacyVpns, String lockdownVpnKey) {
            this.vpnProfiles = vpnProfiles;
            this.connectedLegacyVpns = connectedLegacyVpns;
            this.lockdownVpnKey = lockdownVpnKey;
            return this;
        }

        public final UpdatePreferences appVpns(List<AppVpnInfo> vpnApps, Set<AppVpnInfo> connectedAppVpns, Set<AppVpnInfo> alwaysOnAppVpnInfos) {
            this.vpnApps = vpnApps;
            this.connectedAppVpns = connectedAppVpns;
            this.alwaysOnAppVpnInfos = alwaysOnAppVpnInfos;
            return this;
        }

        public void run() {
            if (this.mSettings.canAddPreferences()) {
                Set<Preference> updates = new ArraySet();
                Iterator it = this.vpnProfiles.iterator();
                while (true) {
                    boolean z = false;
                    if (!it.hasNext()) {
                        break;
                    }
                    VpnProfile profile = (VpnProfile) it.next();
                    LegacyVpnPreference p = this.mSettings.findOrCreatePreference(profile, true);
                    if (this.connectedLegacyVpns.containsKey(profile.key)) {
                        p.setState(((LegacyVpnInfo) this.connectedLegacyVpns.get(profile.key)).state);
                    } else {
                        p.setState(LegacyVpnPreference.STATE_NONE);
                    }
                    if (this.lockdownVpnKey != null && this.lockdownVpnKey.equals(profile.key)) {
                        z = true;
                    }
                    p.setAlwaysOn(z);
                    updates.add(p);
                }
                for (LegacyVpnInfo vpn : this.connectedLegacyVpns.values()) {
                    LegacyVpnPreference p2 = this.mSettings.findOrCreatePreference(new VpnProfile(vpn.key), false);
                    p2.setState(vpn.state);
                    boolean z2 = this.lockdownVpnKey != null && this.lockdownVpnKey.equals(vpn.key);
                    p2.setAlwaysOn(z2);
                    updates.add(p2);
                }
                for (AppVpnInfo app : this.vpnApps) {
                    AppPreference p3 = this.mSettings.findOrCreatePreference(app);
                    if (this.connectedAppVpns.contains(app)) {
                        p3.setState(3);
                    } else {
                        p3.setState(AppPreference.STATE_DISCONNECTED);
                    }
                    p3.setAlwaysOn(this.alwaysOnAppVpnInfos.contains(app));
                    updates.add(p3);
                }
                this.mSettings.setShownPreferences(updates);
            }
        }
    }

    public VpnSettings() {
        super("no_config_vpn");
    }

    public int getMetricsCategory() {
        return 100;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mUserManager = (UserManager) getSystemService("user");
        this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
        this.mUnavailable = isUiRestricted();
        setHasOptionsMenu(this.mUnavailable ^ 1);
        addPreferencesFromResource(R.xml.vpn_settings2);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.vpn, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            if (isUiRestrictedByOnlyAdmin()) {
                RestrictedLockUtils.setMenuItemAsDisabledByAdmin(getPrefContext(), menu.getItem(i), getRestrictionEnforcedAdmin());
            } else {
                menu.getItem(i).setEnabled(this.mUnavailable ^ 1);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != R.id.vpn_create) {
            return super.onOptionsItemSelected(item);
        }
        long millis = System.currentTimeMillis();
        while (this.mLegacyVpnPreferences.containsKey(Long.toHexString(millis))) {
            millis++;
        }
        ConfigDialogFragment.show(this, new VpnProfile(Long.toHexString(millis)), true, false);
        return true;
    }

    public void onResume() {
        super.onResume();
        this.mUnavailable = this.mUserManager.hasUserRestriction("no_config_vpn");
        if (this.mUnavailable) {
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(R.string.vpn_settings_not_available);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        setEmptyView(getEmptyTextView());
        getEmptyTextView().setText(R.string.vpn_no_vpns_added);
        this.mConnectivityManager.registerNetworkCallback(VPN_REQUEST, this.mNetworkCallback);
        this.mUpdaterThread = new HandlerThread("Refresh VPN list in background");
        this.mUpdaterThread.start();
        this.mUpdater = new Handler(this.mUpdaterThread.getLooper(), this);
        this.mUpdater.sendEmptyMessage(0);
    }

    public void onPause() {
        if (this.mUnavailable) {
            super.onPause();
            return;
        }
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
        synchronized (this) {
            this.mUpdater.removeCallbacksAndMessages(null);
            this.mUpdater = null;
            this.mUpdaterThread.quit();
            this.mUpdaterThread = null;
        }
        super.onPause();
    }

    public boolean handleMessage(Message message) {
        Activity activity = getActivity();
        if (activity == null) {
            return true;
        }
        Context context = activity.getApplicationContext();
        List<VpnProfile> vpnProfiles = loadVpnProfiles(this.mKeyStore, new int[0]);
        List<AppVpnInfo> vpnApps = getVpnApps(context, true);
        Map<String, LegacyVpnInfo> connectedLegacyVpns = getConnectedLegacyVpns();
        activity.runOnUiThread(new UpdatePreferences(this).legacyVpns(vpnProfiles, connectedLegacyVpns, VpnUtils.getLockdownVpn()).appVpns(vpnApps, getConnectedAppVpns(), getAlwaysOnAppVpnInfos()));
        synchronized (this) {
            if (this.mUpdater != null) {
                this.mUpdater.removeMessages(0);
                this.mUpdater.sendEmptyMessageDelayed(0, 1000);
            }
        }
        return true;
    }

    @VisibleForTesting
    public boolean canAddPreferences() {
        return isAdded();
    }

    @VisibleForTesting
    public void setShownPreferences(Collection<Preference> updates) {
        Preference p;
        this.mLegacyVpnPreferences.values().retainAll(updates);
        this.mAppPreferences.values().retainAll(updates);
        PreferenceGroup vpnGroup = getPreferenceScreen();
        for (int i = vpnGroup.getPreferenceCount() - 1; i >= 0; i--) {
            p = vpnGroup.getPreference(i);
            if (updates.contains(p)) {
                updates.remove(p);
            } else {
                vpnGroup.removePreference(p);
            }
        }
        for (Preference p2 : updates) {
            vpnGroup.addPreference(p2);
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof LegacyVpnPreference) {
            VpnProfile profile = ((LegacyVpnPreference) preference).getProfile();
            if (this.mConnectedLegacyVpn != null && profile.key.equals(this.mConnectedLegacyVpn.key) && this.mConnectedLegacyVpn.state == 3) {
                try {
                    this.mConnectedLegacyVpn.intent.send();
                    return true;
                } catch (Exception e) {
                    Log.w(LOG_TAG, "Starting config intent failed", e);
                }
            }
            ConfigDialogFragment.show(this, profile, false, true);
            return true;
        } else if (!(preference instanceof AppPreference)) {
            return false;
        } else {
            AppPreference pref = (AppPreference) preference;
            boolean connected = pref.getState() == 3;
            if (!connected) {
                try {
                    UserHandle user = UserHandle.of(pref.getUserId());
                    Context userContext = getActivity().createPackageContextAsUser(getActivity().getPackageName(), 0, user);
                    Intent appIntent = userContext.getPackageManager().getLaunchIntentForPackage(pref.getPackageName());
                    if (appIntent != null) {
                        userContext.startActivityAsUser(appIntent, user);
                        return true;
                    }
                } catch (NameNotFoundException nnfe) {
                    String str = LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("VPN provider does not exist: ");
                    stringBuilder.append(pref.getPackageName());
                    Log.w(str, stringBuilder.toString(), nnfe);
                }
            }
            AppDialogFragment.show(this, pref.getPackageInfo(), pref.getLabel(), false, connected);
            return true;
        }
    }

    public int getHelpResource() {
        return R.string.help_url_vpn;
    }

    @VisibleForTesting
    public LegacyVpnPreference findOrCreatePreference(VpnProfile profile, boolean update) {
        LegacyVpnPreference pref = (LegacyVpnPreference) this.mLegacyVpnPreferences.get(profile.key);
        boolean created = false;
        if (pref == null) {
            pref = new LegacyVpnPreference(getPrefContext());
            pref.setOnGearClickListener(this.mGearListener);
            pref.setOnPreferenceClickListener(this);
            this.mLegacyVpnPreferences.put(profile.key, pref);
            created = true;
        }
        if (created || update) {
            pref.setProfile(profile);
        }
        return pref;
    }

    @VisibleForTesting
    public AppPreference findOrCreatePreference(AppVpnInfo app) {
        AppPreference pref = (AppPreference) this.mAppPreferences.get(app);
        if (pref != null) {
            return pref;
        }
        pref = new AppPreference(getPrefContext(), app.userId, app.packageName);
        pref.setOnGearClickListener(this.mGearListener);
        pref.setOnPreferenceClickListener(this);
        this.mAppPreferences.put(app, pref);
        return pref;
    }

    private Map<String, LegacyVpnInfo> getConnectedLegacyVpns() {
        try {
            this.mConnectedLegacyVpn = this.mConnectivityService.getLegacyVpnInfo(UserHandle.myUserId());
            if (this.mConnectedLegacyVpn != null) {
                return Collections.singletonMap(this.mConnectedLegacyVpn.key, this.mConnectedLegacyVpn);
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failure updating VPN list with connected legacy VPNs", e);
        }
        return Collections.emptyMap();
    }

    private Set<AppVpnInfo> getConnectedAppVpns() {
        Set<AppVpnInfo> connections = new ArraySet();
        try {
            for (UserHandle profile : this.mUserManager.getUserProfiles()) {
                VpnConfig config = this.mConnectivityService.getVpnConfig(profile.getIdentifier());
                if (!(config == null || config.legacy)) {
                    connections.add(new AppVpnInfo(profile.getIdentifier(), config.user));
                }
            }
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failure updating VPN list with connected app VPNs", e);
        }
        return connections;
    }

    private Set<AppVpnInfo> getAlwaysOnAppVpnInfos() {
        Set<AppVpnInfo> result = new ArraySet();
        for (UserHandle profile : this.mUserManager.getUserProfiles()) {
            int profileId = profile.getIdentifier();
            String packageName = this.mConnectivityManager.getAlwaysOnVpnPackageForUser(profileId);
            if (packageName != null) {
                result.add(new AppVpnInfo(profileId, packageName));
            }
        }
        return result;
    }

    static List<AppVpnInfo> getVpnApps(Context context, boolean includeProfiles) {
        Set<Integer> profileIds;
        List<AppVpnInfo> result = Lists.newArrayList();
        if (includeProfiles) {
            profileIds = new ArraySet();
            for (UserHandle profile : UserManager.get(context).getUserProfiles()) {
                profileIds.add(Integer.valueOf(profile.getIdentifier()));
            }
        } else {
            profileIds = Collections.singleton(Integer.valueOf(UserHandle.myUserId()));
        }
        List<PackageOps> apps = ((AppOpsManager) context.getSystemService("appops")).getPackagesForOps(new int[]{47});
        if (apps != null) {
            for (PackageOps pkg : apps) {
                int userId = UserHandle.getUserId(pkg.getUid());
                if (profileIds.contains(Integer.valueOf(userId))) {
                    boolean allowed = false;
                    for (OpEntry op : pkg.getOps()) {
                        if (op.getOp() == 47 && op.getMode() == 0) {
                            allowed = true;
                        }
                    }
                    if (allowed) {
                        result.add(new AppVpnInfo(userId, pkg.getPackageName()));
                    }
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    static List<VpnProfile> loadVpnProfiles(KeyStore keyStore, int... excludeTypes) {
        ArrayList<VpnProfile> result = Lists.newArrayList();
        for (String key : keyStore.list("VPN_")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("VPN_");
            stringBuilder.append(key);
            VpnProfile profile = VpnProfile.decode(key, keyStore.get(stringBuilder.toString()));
            if (!(profile == null || ArrayUtils.contains(excludeTypes, profile.type))) {
                result.add(profile);
            }
        }
        return result;
    }
}
