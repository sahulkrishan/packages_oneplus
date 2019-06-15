package com.android.settingslib.drawer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.IContentProvider;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import com.android.setupwizardlib.util.WizardManagerHelper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TileUtils {
    private static final Comparator<DashboardCategory> CATEGORY_COMPARATOR = new Comparator<DashboardCategory>() {
        public int compare(DashboardCategory lhs, DashboardCategory rhs) {
            return rhs.priority - lhs.priority;
        }
    };
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_TIMING = false;
    private static final String EXTRA_CATEGORY_KEY = "com.android.settings.category";
    private static final String EXTRA_PREFERENCE_ICON_PACKAGE = "com.android.settings.icon_package";
    public static final String EXTRA_SETTINGS_ACTION = "com.android.settings.action.EXTRA_SETTINGS";
    private static final String IA_SETTINGS_ACTION = "com.android.settings.action.IA_SETTINGS";
    private static final String LOG_TAG = "TileUtils";
    private static final String MANUFACTURER_DEFAULT_CATEGORY = "com.android.settings.category.device";
    private static final String MANUFACTURER_SETTINGS = "com.android.settings.MANUFACTURER_APPLICATION_SETTING";
    public static final String META_DATA_PREFERENCE_CUSTOM_VIEW = "com.android.settings.custom_view";
    public static final String META_DATA_PREFERENCE_ICON = "com.android.settings.icon";
    public static final String META_DATA_PREFERENCE_ICON_BACKGROUND_HINT = "com.android.settings.bg.hint";
    public static final String META_DATA_PREFERENCE_ICON_TINTABLE = "com.android.settings.icon_tintable";
    public static final String META_DATA_PREFERENCE_ICON_URI = "com.android.settings.icon_uri";
    public static final String META_DATA_PREFERENCE_KEYHINT = "com.android.settings.keyhint";
    public static final String META_DATA_PREFERENCE_SUMMARY = "com.android.settings.summary";
    public static final String META_DATA_PREFERENCE_SUMMARY_URI = "com.android.settings.summary_uri";
    public static final String META_DATA_PREFERENCE_TITLE = "com.android.settings.title";
    private static final String OPERATOR_DEFAULT_CATEGORY = "com.android.settings.category.wireless";
    private static final String OPERATOR_SETTINGS = "com.android.settings.OPERATOR_APPLICATION_SETTING";
    private static final String SETTINGS_ACTION = "com.android.settings.action.SETTINGS";
    public static final String SETTING_PKG = "com.android.settings";

    @Deprecated
    public static List<DashboardCategory> getCategories(Context context, Map<Pair<String, String>, Tile> cache) {
        return getCategories(context, cache, true);
    }

    public static List<DashboardCategory> getCategories(Context context, Map<Pair<String, String>, Tile> cache, boolean categoryDefinedInManifest) {
        return getCategories(context, cache, categoryDefinedInManifest, null, "com.android.settings");
    }

    public static List<DashboardCategory> getCategories(Context context, Map<Pair<String, String>, Tile> cache, boolean categoryDefinedInManifest, String extraAction, String settingPkg) {
        Context context2 = context;
        boolean z = categoryDefinedInManifest;
        long startTime = System.currentTimeMillis();
        boolean z2 = false;
        if (Global.getInt(context.getContentResolver(), WizardManagerHelper.SETTINGS_GLOBAL_DEVICE_PROVISIONED, 0) != 0) {
            z2 = true;
        }
        boolean setup = z2;
        ArrayList<Tile> tiles = new ArrayList();
        Iterator it = ((UserManager) context2.getSystemService("user")).getUserProfiles().iterator();
        while (it.hasNext()) {
            UserHandle user;
            Iterator it2;
            UserHandle user2 = (UserHandle) it.next();
            if (user2.getIdentifier() == ActivityManager.getCurrentUser()) {
                Context context3 = context2;
                Map<Pair<String, String>, Tile> map = cache;
                ArrayList<Tile> arrayList = tiles;
                user = user2;
                getTilesForAction(context3, user2, SETTINGS_ACTION, map, null, arrayList, true, settingPkg);
                UserHandle userHandle = user;
                it2 = it;
                String str = settingPkg;
                getTilesForAction(context3, userHandle, OPERATOR_SETTINGS, map, OPERATOR_DEFAULT_CATEGORY, arrayList, false, true, str);
                getTilesForAction(context3, userHandle, MANUFACTURER_SETTINGS, map, MANUFACTURER_DEFAULT_CATEGORY, arrayList, false, true, str);
            } else {
                user = user2;
                it2 = it;
            }
            if (setup) {
                getTilesForAction(context2, user, EXTRA_SETTINGS_ACTION, cache, null, tiles, false, settingPkg);
                if (!z) {
                    getTilesForAction(context2, user, IA_SETTINGS_ACTION, cache, null, tiles, false, settingPkg);
                    if (extraAction != null) {
                        getTilesForAction(context2, user, extraAction, cache, null, tiles, false, settingPkg);
                    }
                }
            }
            it = it2;
        }
        HashMap<String, DashboardCategory> categoryMap = new HashMap();
        Iterator it3 = tiles.iterator();
        while (it3.hasNext()) {
            Tile tile = (Tile) it3.next();
            DashboardCategory category = (DashboardCategory) categoryMap.get(tile.category);
            if (category == null) {
                category = createCategory(context2, tile.category, z);
                if (category == null) {
                    String str2 = LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Couldn't find category ");
                    stringBuilder.append(tile.category);
                    Log.w(str2, stringBuilder.toString());
                } else {
                    categoryMap.put(category.key, category);
                }
            }
            category.addTile(tile);
        }
        ArrayList<DashboardCategory> categories = new ArrayList(categoryMap.values());
        Iterator it4 = categories.iterator();
        while (it4.hasNext()) {
            ((DashboardCategory) it4.next()).sortTiles();
        }
        Collections.sort(categories, CATEGORY_COMPARATOR);
        return categories;
    }

    private static DashboardCategory createCategory(Context context, String categoryKey, boolean categoryDefinedInManifest) {
        DashboardCategory category = new DashboardCategory();
        category.key = categoryKey;
        if (!categoryDefinedInManifest) {
            return category;
        }
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> results = pm.queryIntentActivities(new Intent(categoryKey), 0);
        if (results.size() == 0) {
            return null;
        }
        for (ResolveInfo resolved : results) {
            if (resolved.system) {
                category.title = resolved.activityInfo.loadLabel(pm);
                category.priority = "com.android.settings".equals(resolved.activityInfo.applicationInfo.packageName) ? resolved.priority : 0;
            }
        }
        return category;
    }

    private static void getTilesForAction(Context context, UserHandle user, String action, Map<Pair<String, String>, Tile> addedCache, String defaultCategory, ArrayList<Tile> outTiles, boolean requireSettings, String settingPkg) {
        getTilesForAction(context, user, action, addedCache, defaultCategory, outTiles, requireSettings, requireSettings, settingPkg);
    }

    private static void getTilesForAction(Context context, UserHandle user, String action, Map<Pair<String, String>, Tile> addedCache, String defaultCategory, ArrayList<Tile> outTiles, boolean requireSettings, boolean usePriority, String settingPkg) {
        Intent intent = new Intent(action);
        if (requireSettings) {
            intent.setPackage(settingPkg);
        } else {
            String str = settingPkg;
        }
        getTilesForIntent(context, user, intent, addedCache, defaultCategory, outTiles, usePriority, true, true);
    }

    public static void getTilesForIntent(Context context, UserHandle user, Intent intent, Map<Pair<String, String>, Tile> addedCache, String defaultCategory, List<Tile> outTiles, boolean usePriority, boolean checkCategory, boolean forceTintExternalIcon) {
        getTilesForIntent(context, user, intent, addedCache, defaultCategory, outTiles, usePriority, checkCategory, forceTintExternalIcon, false);
    }

    public static void getTilesForIntent(Context context, UserHandle user, Intent intent, Map<Pair<String, String>, Tile> addedCache, String defaultCategory, List<Tile> outTiles, boolean usePriority, boolean checkCategory, boolean forceTintExternalIcon, boolean shouldUpdateTiles) {
        List<ResolveInfo> results;
        Context context2;
        UserHandle userHandle = user;
        Object obj = intent;
        Map<Pair<String, String>, Tile> map = addedCache;
        List<Tile> list = outTiles;
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> results2 = pm.queryIntentActivitiesAsUser(obj, 128, user.getIdentifier());
        Map providerMap = new HashMap();
        for (ResolveInfo resolved : results2) {
            if (resolved.system) {
                ActivityInfo activityInfo = resolved.activityInfo;
                Bundle metaData = activityInfo.metaData;
                String categoryKey = defaultCategory;
                if (!checkCategory || ((metaData != null && metaData.containsKey(EXTRA_CATEGORY_KEY)) || categoryKey != null)) {
                    String categoryKey2 = metaData.getString(EXTRA_CATEGORY_KEY);
                    Pair<String, String> key = new Pair(activityInfo.packageName, activityInfo.name);
                    Tile tile = (Tile) map.get(key);
                    if (tile == null) {
                        Tile tile2 = new Tile();
                        tile2.intent = new Intent().setClassName(activityInfo.packageName, activityInfo.name);
                        tile2.category = categoryKey2;
                        tile2.priority = usePriority ? resolved.priority : 0;
                        tile2.metaData = activityInfo.metaData;
                        Tile tile3 = tile2;
                        results = results2;
                        Pair<String, String> results3 = key;
                        updateTileData(context, tile2, activityInfo, activityInfo.applicationInfo, pm, providerMap, forceTintExternalIcon);
                        map.put(results3, tile3);
                        context2 = context;
                        tile = tile3;
                    } else {
                        Bundle bundle = metaData;
                        ActivityInfo activityInfo2 = activityInfo;
                        results = results2;
                        if (shouldUpdateTiles) {
                            updateSummaryAndTitle(context, providerMap, tile);
                        } else {
                            context2 = context;
                        }
                    }
                    if (!tile.userHandle.contains(userHandle)) {
                        tile.userHandle.add(userHandle);
                    }
                    if (!list.contains(tile)) {
                        list.add(tile);
                    }
                    results2 = results;
                    obj = intent;
                } else {
                    String str = LOG_TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Found ");
                    stringBuilder.append(resolved.activityInfo.name);
                    stringBuilder.append(" for intent ");
                    stringBuilder.append(obj);
                    stringBuilder.append(" missing metadata ");
                    stringBuilder.append(metaData == null ? "" : EXTRA_CATEGORY_KEY);
                    Log.w(str, stringBuilder.toString());
                }
            }
        }
        context2 = context;
        results = results2;
    }

    /* JADX WARNING: Removed duplicated region for block: B:85:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0088 A:{SYNTHETIC, Splitter:B:40:0x0088} */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x00ad A:{SYNTHETIC, Splitter:B:49:0x00ad} */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x00d2 A:{SYNTHETIC, Splitter:B:58:0x00d2} */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x0116  */
    /* JADX WARNING: Removed duplicated region for block: B:67:0x00f7 A:{Catch:{ NameNotFoundException | NotFoundException -> 0x011f }} */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x015c  */
    /* JADX WARNING: Removed duplicated region for block: B:85:0x0144  */
    /* JADX WARNING: Removed duplicated region for block: B:91:0x015c  */
    private static boolean updateTileData(android.content.Context r19, com.android.settingslib.drawer.Tile r20, android.content.pm.ActivityInfo r21, android.content.pm.ApplicationInfo r22, android.content.pm.PackageManager r23, java.util.Map<java.lang.String, android.content.IContentProvider> r24, boolean r25) {
        /*
        r1 = r20;
        r2 = r21;
        r3 = r22;
        r4 = r23;
        r0 = r22.isSystemApp();
        r5 = 0;
        if (r0 == 0) goto L_0x017d;
    L_0x000f:
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r9 = 0;
        r10 = 0;
        r11 = 0;
        r0 = r3.packageName;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0136, NameNotFoundException | NotFoundException -> 0x0136 }
        r0 = r4.getResourcesForApplication(r0);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0136, NameNotFoundException | NotFoundException -> 0x0136 }
        r12 = r2.metaData;	 Catch:{ NameNotFoundException | NotFoundException -> 0x0136, NameNotFoundException | NotFoundException -> 0x0136 }
        if (r25 == 0) goto L_0x0037;
    L_0x0020:
        r13 = r19.getPackageName();	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r14 = r3.packageName;	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r13.equals(r14);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        if (r13 != 0) goto L_0x0037;
    L_0x002c:
        r5 = 1;
        r6 = 1;
        goto L_0x0037;
    L_0x002f:
        r0 = move-exception;
        r14 = r19;
        r15 = r5;
    L_0x0033:
        r5 = r24;
        goto L_0x013d;
    L_0x0037:
        if (r0 == 0) goto L_0x0130;
    L_0x0039:
        if (r12 == 0) goto L_0x0130;
    L_0x003b:
        r13 = "com.android.settings.icon";
        r13 = r12.containsKey(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0129, NameNotFoundException | NotFoundException -> 0x0129 }
        if (r13 == 0) goto L_0x004a;
    L_0x0043:
        r13 = "com.android.settings.icon";
        r13 = r12.getInt(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r7 = r13;
    L_0x004a:
        r13 = "com.android.settings.icon_tintable";
        r13 = r12.containsKey(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0129, NameNotFoundException | NotFoundException -> 0x0129 }
        if (r13 == 0) goto L_0x007e;
    L_0x0052:
        if (r6 == 0) goto L_0x0072;
    L_0x0054:
        r13 = "TileUtils";
        r14 = new java.lang.StringBuilder;	 Catch:{ NameNotFoundException | NotFoundException -> 0x006c, NameNotFoundException | NotFoundException -> 0x006c }
        r14.<init>();	 Catch:{ NameNotFoundException | NotFoundException -> 0x006c, NameNotFoundException | NotFoundException -> 0x006c }
        r15 = r5;
        r5 = "Ignoring icon tintable for ";
        r14.append(r5);	 Catch:{ NameNotFoundException | NotFoundException -> 0x007a, NameNotFoundException | NotFoundException -> 0x007a }
        r14.append(r2);	 Catch:{ NameNotFoundException | NotFoundException -> 0x007a, NameNotFoundException | NotFoundException -> 0x007a }
        r5 = r14.toString();	 Catch:{ NameNotFoundException | NotFoundException -> 0x007a, NameNotFoundException | NotFoundException -> 0x007a }
        android.util.Log.w(r13, r5);	 Catch:{ NameNotFoundException | NotFoundException -> 0x007a, NameNotFoundException | NotFoundException -> 0x007a }
        goto L_0x007f;
    L_0x006c:
        r0 = move-exception;
        r15 = r5;
        r14 = r19;
        goto L_0x012d;
    L_0x0072:
        r15 = r5;
        r5 = "com.android.settings.icon_tintable";
        r5 = r12.getBoolean(r5);	 Catch:{ NameNotFoundException | NotFoundException -> 0x007a, NameNotFoundException | NotFoundException -> 0x007a }
        goto L_0x0080;
    L_0x007a:
        r0 = move-exception;
        r14 = r19;
        goto L_0x0033;
    L_0x007e:
        r15 = r5;
    L_0x007f:
        r5 = r15;
    L_0x0080:
        r13 = "com.android.settings.title";
        r13 = r12.containsKey(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        if (r13 == 0) goto L_0x00a5;
    L_0x0088:
        r13 = "com.android.settings.title";
        r13 = r12.get(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r13 instanceof java.lang.Integer;	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        if (r13 == 0) goto L_0x009e;
    L_0x0092:
        r13 = "com.android.settings.title";
        r13 = r12.getInt(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r0.getString(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r9 = r13;
        goto L_0x00a5;
    L_0x009e:
        r13 = "com.android.settings.title";
        r13 = r12.getString(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r9 = r13;
    L_0x00a5:
        r13 = "com.android.settings.summary";
        r13 = r12.containsKey(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        if (r13 == 0) goto L_0x00ca;
    L_0x00ad:
        r13 = "com.android.settings.summary";
        r13 = r12.get(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r13 instanceof java.lang.Integer;	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        if (r13 == 0) goto L_0x00c3;
    L_0x00b7:
        r13 = "com.android.settings.summary";
        r13 = r12.getInt(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r0.getString(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r10 = r13;
        goto L_0x00ca;
    L_0x00c3:
        r13 = "com.android.settings.summary";
        r13 = r12.getString(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r10 = r13;
    L_0x00ca:
        r13 = "com.android.settings.keyhint";
        r13 = r12.containsKey(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        if (r13 == 0) goto L_0x00ef;
    L_0x00d2:
        r13 = "com.android.settings.keyhint";
        r13 = r12.get(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r13 instanceof java.lang.Integer;	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        if (r13 == 0) goto L_0x00e8;
    L_0x00dc:
        r13 = "com.android.settings.keyhint";
        r13 = r12.getInt(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r13 = r0.getString(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r11 = r13;
        goto L_0x00ef;
    L_0x00e8:
        r13 = "com.android.settings.keyhint";
        r13 = r12.getString(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x002f, NameNotFoundException | NotFoundException -> 0x002f }
        r11 = r13;
    L_0x00ef:
        r13 = "com.android.settings.custom_view";
        r13 = r12.containsKey(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        if (r13 == 0) goto L_0x0116;
    L_0x00f7:
        r13 = "com.android.settings.custom_view";
        r13 = r12.getInt(r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        r14 = new android.widget.RemoteViews;	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        r16 = r0;
        r0 = r3.packageName;	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        r14.<init>(r0, r13);	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        r1.remoteViews = r14;	 Catch:{ NameNotFoundException | NotFoundException -> 0x011f, NameNotFoundException | NotFoundException -> 0x011f }
        r14 = r19;
        r17 = r5;
        r5 = r24;
        updateSummaryAndTitle(r14, r5, r1);	 Catch:{ NameNotFoundException | NotFoundException -> 0x0112, NameNotFoundException | NotFoundException -> 0x0112 }
        goto L_0x011c;
    L_0x0112:
        r0 = move-exception;
        r15 = r17;
        goto L_0x013d;
    L_0x0116:
        r14 = r19;
        r17 = r5;
        r5 = r24;
    L_0x011c:
        r15 = r17;
        goto L_0x0135;
    L_0x011f:
        r0 = move-exception;
        r14 = r19;
        r17 = r5;
        r5 = r24;
        r15 = r17;
        goto L_0x013d;
    L_0x0129:
        r0 = move-exception;
        r14 = r19;
        r15 = r5;
    L_0x012d:
        r5 = r24;
        goto L_0x013d;
    L_0x0130:
        r14 = r19;
        r15 = r5;
        r5 = r24;
    L_0x0135:
        goto L_0x013d;
    L_0x0136:
        r0 = move-exception;
        r14 = r19;
        r12 = r5;
        r5 = r24;
        r15 = r12;
    L_0x013d:
        r0 = r15;
        r12 = android.text.TextUtils.isEmpty(r9);
        if (r12 == 0) goto L_0x014c;
    L_0x0144:
        r12 = r2.loadLabel(r4);
        r9 = r12.toString();
    L_0x014c:
        if (r7 != 0) goto L_0x015a;
    L_0x014e:
        r12 = r1.metaData;
        r13 = "com.android.settings.icon_uri";
        r12 = r12.containsKey(r13);
        if (r12 != 0) goto L_0x015a;
    L_0x0158:
        r7 = r2.icon;
    L_0x015a:
        if (r7 == 0) goto L_0x0164;
    L_0x015c:
        r12 = r2.packageName;
        r12 = android.graphics.drawable.Icon.createWithResource(r12, r7);
        r1.icon = r12;
    L_0x0164:
        r1.title = r9;
        r1.summary = r10;
        r12 = new android.content.Intent;
        r12.<init>();
        r13 = r2.packageName;
        r3 = r2.name;
        r3 = r12.setClassName(r13, r3);
        r1.intent = r3;
        r1.key = r11;
        r1.isIconTintable = r0;
        r3 = 1;
        return r3;
    L_0x017d:
        r14 = r19;
        r0 = r5;
        r5 = r24;
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.drawer.TileUtils.updateTileData(android.content.Context, com.android.settingslib.drawer.Tile, android.content.pm.ActivityInfo, android.content.pm.ApplicationInfo, android.content.pm.PackageManager, java.util.Map, boolean):boolean");
    }

    private static void updateSummaryAndTitle(Context context, Map<String, IContentProvider> providerMap, Tile tile) {
        if (tile != null && tile.metaData != null && tile.metaData.containsKey(META_DATA_PREFERENCE_SUMMARY_URI)) {
            Bundle bundle = getBundleFromUri(context, tile.metaData.getString(META_DATA_PREFERENCE_SUMMARY_URI), providerMap);
            String overrideSummary = getString(bundle, META_DATA_PREFERENCE_SUMMARY);
            String overrideTitle = getString(bundle, META_DATA_PREFERENCE_TITLE);
            if (overrideSummary != null) {
                tile.remoteViews.setTextViewText(16908304, overrideSummary);
            }
            if (overrideTitle != null) {
                tile.remoteViews.setTextViewText(16908310, overrideTitle);
            }
        }
    }

    public static Pair<String, Integer> getIconFromUri(Context context, String packageName, String uriString, Map<String, IContentProvider> providerMap) {
        Bundle bundle = getBundleFromUri(context, uriString, providerMap);
        if (bundle == null) {
            return null;
        }
        String iconPackageName = bundle.getString(EXTRA_PREFERENCE_ICON_PACKAGE);
        if (TextUtils.isEmpty(iconPackageName) || bundle.getInt(META_DATA_PREFERENCE_ICON, 0) == 0) {
            return null;
        }
        if (iconPackageName.equals(packageName) || iconPackageName.equals(context.getPackageName())) {
            return Pair.create(iconPackageName, Integer.valueOf(bundle.getInt(META_DATA_PREFERENCE_ICON, 0)));
        }
        return null;
    }

    public static String getTextFromUri(Context context, String uriString, Map<String, IContentProvider> providerMap, String key) {
        Bundle bundle = getBundleFromUri(context, uriString, providerMap);
        return bundle != null ? bundle.getString(key) : null;
    }

    private static Bundle getBundleFromUri(Context context, String uriString, Map<String, IContentProvider> providerMap) {
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }
        Uri uri = Uri.parse(uriString);
        String method = getMethodFromUri(uri);
        if (TextUtils.isEmpty(method)) {
            return null;
        }
        IContentProvider provider = getProviderFromUri(context, uri, providerMap);
        if (provider == null) {
            return null;
        }
        try {
            return provider.call(context.getPackageName(), method, uriString, null);
        } catch (RemoteException e) {
            return null;
        }
    }

    private static String getString(Bundle bundle, String key) {
        return bundle == null ? null : bundle.getString(key);
    }

    private static IContentProvider getProviderFromUri(Context context, Uri uri, Map<String, IContentProvider> providerMap) {
        if (uri == null) {
            return null;
        }
        String authority = uri.getAuthority();
        if (TextUtils.isEmpty(authority)) {
            return null;
        }
        if (!providerMap.containsKey(authority)) {
            providerMap.put(authority, context.getContentResolver().acquireUnstableProvider(uri));
        }
        return (IContentProvider) providerMap.get(authority);
    }

    static String getMethodFromUri(Uri uri) {
        if (uri == null) {
            return null;
        }
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments == null || pathSegments.isEmpty()) {
            return null;
        }
        return (String) pathSegments.get(0);
    }
}
