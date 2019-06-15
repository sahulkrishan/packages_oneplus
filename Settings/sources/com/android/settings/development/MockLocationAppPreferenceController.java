package com.android.settings.development;

import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;
import com.android.settingslib.wrapper.PackageManagerWrapper;
import java.util.List;

public class MockLocationAppPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin, OnActivityResultListener {
    private static final String MOCK_LOCATION_APP_KEY = "mock_location_app";
    private static final int[] MOCK_LOCATION_APP_OPS = new int[]{58};
    private final AppOpsManager mAppsOpsManager;
    private final DevelopmentSettingsDashboardFragment mFragment;
    private final PackageManagerWrapper mPackageManager;

    public MockLocationAppPreferenceController(Context context, DevelopmentSettingsDashboardFragment fragment) {
        super(context);
        this.mFragment = fragment;
        this.mAppsOpsManager = (AppOpsManager) context.getSystemService("appops");
        this.mPackageManager = new PackageManagerWrapper(context.getPackageManager());
    }

    public String getPreferenceKey() {
        return MOCK_LOCATION_APP_KEY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        Intent intent = new Intent(this.mContext, AppPicker.class);
        intent.putExtra(AppPicker.EXTRA_REQUESTIING_PERMISSION, "android.permission.ACCESS_MOCK_LOCATION");
        this.mFragment.startActivityForResult(intent, 2);
        return true;
    }

    public void updateState(Preference preference) {
        updateMockLocation();
    }

    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 2 || resultCode != -1) {
            return false;
        }
        writeMockLocation(data.getAction());
        updateMockLocation();
        return true;
    }

    private void updateMockLocation() {
        if (TextUtils.isEmpty(getCurrentMockLocationApp())) {
            this.mPreference.setSummary(this.mContext.getResources().getString(R.string.mock_location_app_not_set));
            return;
        }
        this.mPreference.setSummary(this.mContext.getResources().getString(R.string.mock_location_app_set, new Object[]{getAppLabel(mockLocationApp)}));
    }

    private void writeMockLocation(String mockLocationAppName) {
        removeAllMockLocations();
        if (!TextUtils.isEmpty(mockLocationAppName)) {
            try {
                this.mAppsOpsManager.setMode(58, this.mPackageManager.getApplicationInfo(mockLocationAppName, 512).uid, mockLocationAppName, 0);
            } catch (NameNotFoundException e) {
            }
        }
    }

    private String getAppLabel(String mockLocationApp) {
        try {
            CharSequence appLabel = this.mPackageManager.getApplicationLabel(this.mPackageManager.getApplicationInfo(mockLocationApp, 512));
            return appLabel != null ? appLabel.toString() : mockLocationApp;
        } catch (NameNotFoundException e) {
            return mockLocationApp;
        }
    }

    private void removeAllMockLocations() {
        List<PackageOps> packageOps = this.mAppsOpsManager.getPackagesForOps(MOCK_LOCATION_APP_OPS);
        if (packageOps != null) {
            for (PackageOps packageOp : packageOps) {
                if (((OpEntry) packageOp.getOps().get(0)).getMode() != 2) {
                    removeMockLocationForApp(packageOp.getPackageName());
                }
            }
        }
    }

    private void removeMockLocationForApp(String appName) {
        try {
            this.mAppsOpsManager.setMode(58, this.mPackageManager.getApplicationInfo(appName, 512).uid, appName, 2);
        } catch (NameNotFoundException e) {
        }
    }

    private String getCurrentMockLocationApp() {
        List<PackageOps> packageOps = this.mAppsOpsManager.getPackagesForOps(MOCK_LOCATION_APP_OPS);
        if (packageOps != null) {
            for (PackageOps packageOp : packageOps) {
                if (((OpEntry) packageOp.getOps().get(0)).getMode() == 0) {
                    return ((PackageOps) packageOps.get(0)).getPackageName();
                }
            }
        }
        return null;
    }
}
