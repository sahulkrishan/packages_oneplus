package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutInfo.Builder;
import android.content.pm.ShortcutManager;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.android.settings.shortcut.CreateShortcut;
import com.oneplus.settings.utils.OPUtils;
import java.util.ArrayList;
import java.util.List;

public class SettingsInitialize extends BroadcastReceiver {
    private static final String PRIMARY_PROFILE_SETTING = "com.android.settings.PRIMARY_PROFILE_CONTROLLED";
    private static final String SETTINGS_PACKAGE = "com.android.settings";
    private static final String TAG = "Settings";
    private static final String WEBVIEW_IMPLEMENTATION_ACTIVITY = ".WebViewImplementation";

    public void onReceive(Context context, Intent broadcast) {
        UserInfo userInfo = ((UserManager) context.getSystemService("user")).getUserInfo(UserHandle.myUserId());
        PackageManager pm = context.getPackageManager();
        managedProfileSetup(context, pm, broadcast, userInfo);
        webviewSettingSetup(context, pm, userInfo);
        refreshExistingShortcuts(context);
    }

    private void managedProfileSetup(Context context, PackageManager pm, Intent broadcast, UserInfo userInfo) {
        if (userInfo != null && userInfo.isManagedProfile()) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Received broadcast: ");
            stringBuilder.append(broadcast.getAction());
            stringBuilder.append(". Setting up intent forwarding for managed profile.");
            Log.i(str, stringBuilder.toString());
            pm.clearCrossProfileIntentFilters(userInfo.id);
            Intent intent = new Intent();
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setPackage(context.getPackageName());
            List<ResolveInfo> resolvedIntents = pm.queryIntentActivities(intent, 705);
            int count = resolvedIntents.size();
            for (int i = 0; i < count; i++) {
                ResolveInfo info = (ResolveInfo) resolvedIntents.get(i);
                if (!(info.filter == null || info.activityInfo == null || info.activityInfo.metaData == null || !info.activityInfo.metaData.getBoolean(PRIMARY_PROFILE_SETTING))) {
                    pm.addCrossProfileIntentFilter(info.filter, userInfo.id, userInfo.profileGroupId, 2);
                }
            }
            pm.setComponentEnabledSetting(new ComponentName(context, Settings.class), 2, 1);
            pm.setComponentEnabledSetting(new ComponentName(context, CreateShortcut.class), 2, 1);
            try {
                if (userInfo.id == 999) {
                    pm.setComponentEnabledSetting(new ComponentName("com.android.documentsui", "com.android.documentsui.LauncherActivity"), 2, 1);
                    if (!OPUtils.isO2()) {
                        pm.setComponentEnabledSetting(new ComponentName("com.oneplus.provision", "com.oneplus.provision.WelcomePage"), 2, 1);
                    }
                    pm.setComponentEnabledSetting(new ComponentName(context, FallbackHome.class), 2, 1);
                }
            } catch (Exception e) {
            }
        }
    }

    private void webviewSettingSetup(Context context, PackageManager pm, UserInfo userInfo) {
        if (userInfo != null) {
            pm.setComponentEnabledSetting(new ComponentName("com.android.settings", "com.android.settings.WebViewImplementation"), userInfo.isAdmin() ? 1 : 2, 1);
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void refreshExistingShortcuts(Context context) {
        ShortcutManager shortcutManager = (ShortcutManager) context.getSystemService(ShortcutManager.class);
        List<ShortcutInfo> pinnedShortcuts = shortcutManager.getPinnedShortcuts();
        List<ShortcutInfo> updates = new ArrayList();
        for (ShortcutInfo info : pinnedShortcuts) {
            Intent shortcutIntent = info.getIntent();
            shortcutIntent.setFlags(335544320);
            updates.add(new Builder(context, info.getId()).setIntent(shortcutIntent).build());
        }
        try {
            shortcutManager.updateShortcuts(updates);
        } catch (Exception e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("refreshExistingShortcuts failed: ");
            stringBuilder.append(e.getMessage());
            Log.e(str, stringBuilder.toString());
        }
    }
}
