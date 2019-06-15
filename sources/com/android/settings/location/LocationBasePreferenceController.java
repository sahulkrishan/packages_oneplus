package com.android.settings.location;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.PreferenceCategory;
import android.util.Log;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.location.LocationEnabler.LocationModeChangeListener;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.core.AbstractPreferenceController;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.oneplus.settings.ui.OPButtonPreference;
import java.util.List;

public abstract class LocationBasePreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, LocationModeChangeListener {
    protected static final String EXTRA_PREF_KEY = "pref_key";
    protected PreferenceCategory mCategoryRecentLocationRequests;
    protected final BroadcastReceiver mCheckKillProcessesReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            boolean flag = getResultCode() != 0;
            try {
                OPButtonPreference pref = (OPButtonPreference) LocationBasePreferenceController.this.mCategoryRecentLocationRequests.findPreference(intent.getStringExtra(LocationBasePreferenceController.EXTRA_PREF_KEY));
                pref.setButtonEnable(flag);
                pref.setButtonVisible(flag);
                String str = PreferenceControllerMixin.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("mCheckKillProcessesReceiver flag:");
                stringBuilder.append(flag);
                Log.d(str, stringBuilder.toString());
                if (!flag) {
                    pref.setSummary("");
                }
            } catch (Exception e) {
                String str2 = PreferenceControllerMixin.TAG;
                StringBuilder stringBuilder2 = new StringBuilder();
                stringBuilder2.append("mCheckKillProcessesReceiver error:");
                stringBuilder2.append(e.getMessage());
                Log.e(str2, stringBuilder2.toString());
                e.printStackTrace();
            }
        }
    };
    protected Context mContext;
    protected DevicePolicyManager mDpm;
    protected final BroadcastReceiver mHighPowerChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("mHighPowerChangeReceiver receive action=");
            stringBuilder.append(action);
            Log.i(str, stringBuilder.toString());
            if ("android.location.HIGH_POWER_REQUEST_CHANGE".equals(action)) {
                LocationBasePreferenceController.this.updateState(null);
            }
        }
    };
    protected IntentFilter mIntentFilter;
    protected final LocationEnabler mLocationEnabler;
    protected LocationManager mLocationManager;
    protected ApplicationsState mState;
    protected final UserManager mUserManager;

    public LocationBasePreferenceController(Context context, Lifecycle lifecycle) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mLocationEnabler = new LocationEnabler(context, this, lifecycle);
        this.mContext = context;
        this.mState = ApplicationsState.getInstance(((Activity) context).getApplication());
        this.mDpm = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mLocationManager = (LocationManager) context.getSystemService("location");
        this.mIntentFilter = new IntentFilter("android.location.HIGH_POWER_REQUEST_CHANGE");
    }

    public boolean isAvailable() {
        return true;
    }

    /* Access modifiers changed, original: protected */
    public List<String> getCurrentUsingGpsList() {
        try {
            return (List) LocationManager.class.getDeclaredMethod("getCurrentProviderPackageList", new Class[]{String.class}).invoke(this.mLocationManager, new Object[]{"gps"});
        } catch (Exception e) {
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getCurrentUsingGpsList Exception:");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            e.printStackTrace();
            return null;
        }
    }

    /* Access modifiers changed, original: protected */
    public List<String> getCurrentUsingGpsListForUid() {
        try {
            return (List) LocationManager.class.getDeclaredMethod("getCurrentProviderPackageListsForInteger", new Class[]{String.class}).invoke(this.mLocationManager, new Object[]{"gps"});
        } catch (Exception e) {
            String str = PreferenceControllerMixin.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("getCurrentUsingGpsListForUid Exception:");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
            e.printStackTrace();
            return null;
        }
    }

    /* Access modifiers changed, original: protected */
    public void forceStopPackage(AppEntry mAppEntry, OPButtonPreference pref) {
        String pkgName = mAppEntry.info.packageName;
        ActivityManager am = (ActivityManager) this.mContext.getSystemService("activity");
        int userId = UserHandle.getUserId(mAppEntry.info.uid);
        String str = PreferenceControllerMixin.TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("forceStopPackage app userId=");
        stringBuilder.append(userId);
        Log.e(str, stringBuilder.toString());
        am.forceStopPackageAsUser(pkgName, userId);
        this.mState.invalidatePackage(pkgName, userId);
        checkForceStop(this.mState.getEntry(pkgName, userId), pref);
    }

    public void checkForceStop(AppEntry mAppEntry, OPButtonPreference pref) {
        Intent intent = new Intent("android.intent.action.QUERY_PACKAGE_RESTART", Uri.fromParts("package", mAppEntry.info.packageName, null));
        intent.putExtra("android.intent.extra.PACKAGES", new String[]{mAppEntry.info.packageName});
        intent.putExtra("android.intent.extra.UID", mAppEntry.info.uid);
        intent.putExtra("android.intent.extra.user_handle", UserHandle.getUserId(mAppEntry.info.uid));
        intent.putExtra(EXTRA_PREF_KEY, pref.getKey());
        if (UserHandle.getUserId(mAppEntry.info.uid) == 999) {
            this.mContext.sendOrderedBroadcastAsUser(intent, new UserHandle(999), null, this.mCheckKillProcessesReceiver, null, 0, null, null);
            return;
        }
        this.mContext.sendOrderedBroadcastAsUser(intent, UserHandle.CURRENT, null, this.mCheckKillProcessesReceiver, null, 0, null, null);
    }
}
