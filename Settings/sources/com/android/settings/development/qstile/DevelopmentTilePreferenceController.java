package com.android.settings.development.qstile;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.annotation.VisibleForTesting;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.IStatusBarService.Stub;
import com.android.settingslib.core.AbstractPreferenceController;

public class DevelopmentTilePreferenceController extends AbstractPreferenceController {
    private static final String TAG = "DevTilePrefController";
    private final OnChangeHandler mOnChangeHandler;
    private final PackageManager mPackageManager;

    @VisibleForTesting
    static class OnChangeHandler implements OnPreferenceChangeListener {
        private final Context mContext;
        private final PackageManager mPackageManager;
        private IStatusBarService mStatusBarService = Stub.asInterface(ServiceManager.checkService("statusbar"));

        public OnChangeHandler(Context context) {
            this.mContext = context;
            this.mPackageManager = context.getPackageManager();
        }

        public boolean onPreferenceChange(Preference preference, Object newValue) {
            boolean enabled = ((Boolean) newValue).booleanValue();
            ComponentName componentName = new ComponentName(this.mContext.getPackageName(), preference.getKey());
            this.mPackageManager.setComponentEnabledSetting(componentName, enabled ? 1 : 2, 1);
            try {
                if (this.mStatusBarService != null) {
                    if (enabled) {
                        this.mStatusBarService.addTile(componentName);
                    } else {
                        this.mStatusBarService.remTile(componentName);
                    }
                }
            } catch (RemoteException e) {
                String str = DevelopmentTilePreferenceController.TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to modify QS tile for component ");
                stringBuilder.append(componentName.toString());
                Log.e(str, stringBuilder.toString(), e);
            }
            return true;
        }
    }

    public DevelopmentTilePreferenceController(Context context) {
        super(context);
        this.mOnChangeHandler = new OnChangeHandler(context);
        this.mPackageManager = context.getPackageManager();
    }

    public boolean isAvailable() {
        return true;
    }

    public String getPreferenceKey() {
        return null;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Context context = screen.getContext();
        for (ResolveInfo info : this.mPackageManager.queryIntentServices(new Intent("android.service.quicksettings.action.QS_TILE").setPackage(context.getPackageName()), 512)) {
            ServiceInfo sInfo = info.serviceInfo;
            int enabledSetting = this.mPackageManager.getComponentEnabledSetting(new ComponentName(sInfo.packageName, sInfo.name));
            boolean checked = true;
            if (!(enabledSetting == 1 || (enabledSetting == 0 && sInfo.enabled))) {
                checked = false;
            }
            SwitchPreference preference = new SwitchPreference(context);
            preference.setTitle(sInfo.loadLabel(this.mPackageManager));
            preference.setIcon(sInfo.icon);
            preference.setKey(sInfo.name);
            preference.setChecked(checked);
            preference.setOnPreferenceChangeListener(this.mOnChangeHandler);
            screen.addPreference(preference);
        }
    }
}
