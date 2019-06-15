package com.android.settings.applications.appinfo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import com.android.settings.R;
import com.android.settings.applications.AppStoreUtil;
import com.android.settings.applications.LayoutPreference;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settingslib.applications.AppUtils;
import com.android.settingslib.core.lifecycle.Lifecycle;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;
import com.android.settingslib.core.lifecycle.events.OnOptionsItemSelected;
import com.android.settingslib.core.lifecycle.events.OnPrepareOptionsMenu;
import com.android.settingslib.wrapper.PackageManagerWrapper;

public class InstantAppButtonsPreferenceController extends BasePreferenceController implements LifecycleObserver, OnCreateOptionsMenu, OnPrepareOptionsMenu, OnOptionsItemSelected, OnClickListener {
    private static final String KEY_INSTANT_APP_BUTTONS = "instant_app_buttons";
    private static final String META_DATA_DEFAULT_URI = "default-url";
    private MenuItem mInstallMenu;
    private String mLaunchUri = getDefaultLaunchUri();
    private final PackageManagerWrapper mPackageManagerWrapper;
    private final String mPackageName;
    private final AppInfoDashboardFragment mParent;
    private LayoutPreference mPreference;

    public InstantAppButtonsPreferenceController(Context context, AppInfoDashboardFragment parent, String packageName, Lifecycle lifecycle) {
        super(context, KEY_INSTANT_APP_BUTTONS);
        this.mParent = parent;
        this.mPackageName = packageName;
        this.mPackageManagerWrapper = new PackageManagerWrapper(context.getPackageManager());
        if (lifecycle != null) {
            lifecycle.addObserver(this);
        }
    }

    public int getAvailabilityStatus() {
        return AppUtils.isInstant(this.mParent.getPackageInfo().applicationInfo) ? 0 : 3;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.mPreference = (LayoutPreference) screen.findPreference(KEY_INSTANT_APP_BUTTONS);
        initButtons(this.mPreference.findViewById(R.id.instant_app_button_container));
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (!TextUtils.isEmpty(this.mLaunchUri)) {
            menu.add(0, 3, 2, R.string.install_text).setShowAsAction(0);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() != 3) {
            return false;
        }
        Intent appStoreIntent = AppStoreUtil.getAppStoreLink(this.mContext, this.mPackageName);
        if (appStoreIntent != null) {
            this.mParent.startActivity(appStoreIntent);
        }
        return true;
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mInstallMenu = menu.findItem(3);
        if (this.mInstallMenu != null && AppStoreUtil.getAppStoreLink(this.mContext, this.mPackageName) == null) {
            this.mInstallMenu.setEnabled(false);
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider().action(this.mContext, 923, this.mPackageName, new Pair[0]);
        this.mPackageManagerWrapper.deletePackageAsUser(this.mPackageName, null, 0, UserHandle.myUserId());
    }

    /* Access modifiers changed, original: 0000 */
    public AlertDialog createDialog(int id) {
        if (id == 4) {
            return new Builder(this.mContext).setPositiveButton(R.string.clear_instant_app_data, this).setNegativeButton(R.string.cancel, null).setTitle(R.string.clear_instant_app_data).setMessage(this.mContext.getString(R.string.clear_instant_app_confirmation)).create();
        }
        return null;
    }

    private void initButtons(View view) {
        Button installButton = (Button) view.findViewById(R.id.install);
        Button clearDataButton = (Button) view.findViewById(R.id.clear_data);
        Button launchButton = (Button) view.findViewById(R.id.launch);
        Intent appStoreIntent;
        if (TextUtils.isEmpty(this.mLaunchUri)) {
            launchButton.setVisibility(8);
            appStoreIntent = AppStoreUtil.getAppStoreLink(this.mContext, this.mPackageName);
            if (appStoreIntent != null) {
                installButton.setOnClickListener(new -$$Lambda$InstantAppButtonsPreferenceController$oBWjqqdf33bi3sDY5lE6TGLlFJM(this, appStoreIntent));
            } else {
                installButton.setEnabled(false);
            }
        } else {
            installButton.setVisibility(8);
            appStoreIntent = new Intent("android.intent.action.VIEW");
            appStoreIntent.addCategory("android.intent.category.BROWSABLE");
            appStoreIntent.setPackage(this.mPackageName);
            appStoreIntent.setData(Uri.parse(this.mLaunchUri));
            appStoreIntent.addFlags(268435456);
            launchButton.setOnClickListener(new -$$Lambda$InstantAppButtonsPreferenceController$2vM5nla3CEsaIUNVk7alr9UEbBA(this, appStoreIntent));
        }
        clearDataButton.setOnClickListener(new -$$Lambda$InstantAppButtonsPreferenceController$f8slAx9lBDdGAmwfjMjp59JCarA(this));
    }

    public static /* synthetic */ void lambda$initButtons$2(InstantAppButtonsPreferenceController instantAppButtonsPreferenceController, View v) {
        AppInfoDashboardFragment appInfoDashboardFragment = instantAppButtonsPreferenceController.mParent;
        AppInfoDashboardFragment appInfoDashboardFragment2 = instantAppButtonsPreferenceController.mParent;
        appInfoDashboardFragment.showDialogInner(4, 0);
    }

    private String getDefaultLaunchUri() {
        PackageManager manager = this.mContext.getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        intent.setPackage(this.mPackageName);
        for (ResolveInfo info : manager.queryIntentActivities(intent, 8388736)) {
            Bundle metaData = info.activityInfo.metaData;
            if (metaData != null) {
                String launchUri = metaData.getString(META_DATA_DEFAULT_URI);
                if (!TextUtils.isEmpty(launchUri)) {
                    return launchUri;
                }
            }
        }
        return null;
    }
}
