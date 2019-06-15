package com.android.settings.search.indexing;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.SearchIndexableResource;
import android.provider.SearchIndexablesContract;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PreIndexDataCollector {
    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static final String TAG = "IndexableDataCollector";
    private final String BASE_AUTHORITY = "com.android.settings";
    private Context mContext;
    private PreIndexData mIndexData;

    public PreIndexDataCollector(Context context) {
        this.mContext = context;
    }

    public PreIndexData collectIndexableData(List<ResolveInfo> providers, boolean isFullIndex) {
        this.mIndexData = new PreIndexData();
        for (ResolveInfo info : providers) {
            if (isWellKnownProvider(info)) {
                String authority = info.providerInfo.authority;
                String packageName = info.providerInfo.packageName;
                if (isFullIndex) {
                    addIndexablesFromRemoteProvider(packageName, authority);
                }
                long nonIndexableStartTime = System.currentTimeMillis();
                addNonIndexablesKeysFromRemoteProvider(packageName, authority);
            }
        }
        return this.mIndexData;
    }

    private boolean addIndexablesFromRemoteProvider(String packageName, String authority) {
        try {
            Context context = "com.android.settings".equals(authority) ? this.mContext : this.mContext.createPackageContext(packageName, 0);
            this.mIndexData.dataToUpdate.addAll(getIndexablesForXmlResourceUri(context, packageName, buildUriForXmlResources(authority), SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS));
            this.mIndexData.dataToUpdate.addAll(getIndexablesForRawDataUri(context, packageName, buildUriForRawData(authority), SearchIndexablesContract.INDEXABLES_RAW_COLUMNS));
            return true;
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not create context for ");
            stringBuilder.append(packageName);
            stringBuilder.append(": ");
            stringBuilder.append(Log.getStackTraceString(e));
            Log.w(str, stringBuilder.toString());
            return false;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<SearchIndexableResource> getIndexablesForXmlResourceUri(Context packageContext, String packageName, Uri uri, String[] projection) {
        Cursor cursor = packageContext.getContentResolver().query(uri, projection, null, null, null);
        List<SearchIndexableResource> resources = new ArrayList();
        if (cursor == null) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot add index data for Uri: ");
            stringBuilder.append(uri.toString());
            Log.w(str, stringBuilder.toString());
            return resources;
        }
        try {
            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    int xmlResId = cursor.getInt(1);
                    String className = cursor.getString(2);
                    int iconResId = cursor.getInt(3);
                    String action = cursor.getString(4);
                    String targetPackage = cursor.getString(5);
                    String targetClass = cursor.getString(6);
                    SearchIndexableResource sir = new SearchIndexableResource(packageContext);
                    sir.xmlResId = xmlResId;
                    sir.className = className;
                    sir.packageName = packageName;
                    sir.iconResId = iconResId;
                    sir.intentAction = action;
                    sir.intentTargetPackage = targetPackage;
                    sir.intentTargetClass = targetClass;
                    resources.add(sir);
                }
            }
            cursor.close();
            return resources;
        } catch (Throwable th) {
            cursor.close();
        }
    }

    private void addNonIndexablesKeysFromRemoteProvider(String packageName, String authority) {
        List<String> keys = getNonIndexablesKeysFromRemoteProvider(packageName, authority);
        if (keys != null && !keys.isEmpty()) {
            this.mIndexData.nonIndexableKeys.put(authority, new ArraySet(keys));
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<String> getNonIndexablesKeysFromRemoteProvider(String packageName, String authority) {
        try {
            return getNonIndexablesKeys(this.mContext.createPackageContext(packageName, 0), buildUriForNonIndexableKeys(authority), SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS);
        } catch (NameNotFoundException e) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Could not create context for ");
            stringBuilder.append(packageName);
            stringBuilder.append(": ");
            stringBuilder.append(Log.getStackTraceString(e));
            Log.w(str, stringBuilder.toString());
            return EMPTY_LIST;
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Uri buildUriForXmlResources(String authority) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("content://");
        stringBuilder.append(authority);
        stringBuilder.append("/");
        stringBuilder.append("settings/indexables_xml_res");
        return Uri.parse(stringBuilder.toString());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Uri buildUriForRawData(String authority) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("content://");
        stringBuilder.append(authority);
        stringBuilder.append("/");
        stringBuilder.append("settings/indexables_raw");
        return Uri.parse(stringBuilder.toString());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Uri buildUriForNonIndexableKeys(String authority) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("content://");
        stringBuilder.append(authority);
        stringBuilder.append("/");
        stringBuilder.append("settings/non_indexables_key");
        return Uri.parse(stringBuilder.toString());
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<SearchIndexableRaw> getIndexablesForRawDataUri(Context packageContext, String packageName, Uri uri, String[] projection) {
        Cursor cursor;
        Throwable th;
        String str;
        Cursor cursor2;
        ContentResolver resolver = packageContext.getContentResolver();
        Cursor cursor3 = resolver.query(uri, projection, null, null, null);
        ArrayList rawData = new ArrayList();
        String str2;
        if (cursor3 == null) {
            str2 = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Cannot add index data for Uri: ");
            stringBuilder.append(uri.toString());
            Log.w(str2, stringBuilder.toString());
            return rawData;
        }
        ContentResolver resolver2;
        try {
            int count = cursor3.getCount();
            if (count > 0) {
                while (cursor3.moveToNext()) {
                    try {
                        int providerRank = cursor3.getInt(0);
                        String title = cursor3.getString(1);
                        String summaryOn = cursor3.getString(2);
                        String summaryOff = cursor3.getString(3);
                        String entries = cursor3.getString(4);
                        String keywords = cursor3.getString(5);
                        String screenTitle = cursor3.getString(6);
                        String className = cursor3.getString(7);
                        int iconResId = cursor3.getInt(8);
                        String action = cursor3.getString(9);
                        String targetPackage = cursor3.getString(10);
                        String targetClass = cursor3.getString(11);
                        int count2 = count;
                        str2 = cursor3.getString(12);
                        providerRank = cursor3.getInt(13);
                        resolver2 = resolver;
                        try {
                            SearchIndexableRaw data;
                            cursor = cursor3;
                            try {
                                data = new SearchIndexableRaw(packageContext);
                                data.title = title;
                                data.summaryOn = summaryOn;
                                data.summaryOff = summaryOff;
                                data.entries = entries;
                                data.keywords = keywords;
                                data.screenTitle = screenTitle;
                                data.className = className;
                            } catch (Throwable th2) {
                                th = th2;
                                str = packageName;
                                cursor2 = cursor;
                                cursor2.close();
                                throw th;
                            }
                            try {
                                data.packageName = packageName;
                                data.iconResId = iconResId;
                                data.intentAction = action;
                                data.intentTargetPackage = targetPackage;
                                data.intentTargetClass = targetClass;
                                data.key = str2;
                                data.userId = providerRank;
                                rawData.add(data);
                                count = count2;
                                resolver = resolver2;
                                cursor3 = cursor;
                            } catch (Throwable th3) {
                                th = th3;
                                cursor2 = cursor;
                                cursor2.close();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            cursor = cursor3;
                            str = packageName;
                            cursor2 = cursor;
                            cursor2.close();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        cursor = cursor3;
                        resolver2 = resolver;
                        str = packageName;
                        cursor2 = cursor;
                        cursor2.close();
                        throw th;
                    }
                }
            }
            cursor = cursor3;
            resolver2 = resolver;
            str = packageName;
            cursor.close();
            return rawData;
        } catch (Throwable th6) {
            th = th6;
            cursor2 = cursor3;
            resolver2 = resolver;
            str = packageName;
            cursor2.close();
            throw th;
        }
    }

    private List<String> getNonIndexablesKeys(Context packageContext, Uri uri, String[] projection) {
        ContentResolver resolver = packageContext.getContentResolver();
        ArrayList result = new ArrayList();
        try {
            Cursor cursor = resolver.query(uri, projection, null, null, null);
            if (cursor == null) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Cannot add index data for Uri: ");
                stringBuilder.append(uri.toString());
                Log.w(str, stringBuilder.toString());
                return result;
            }
            try {
                if (cursor.getCount() > 0) {
                    while (cursor.moveToNext()) {
                        String key = cursor.getString(null);
                        if (TextUtils.isEmpty(key) && Log.isLoggable(TAG, 2)) {
                            String str2 = TAG;
                            StringBuilder stringBuilder2 = new StringBuilder();
                            stringBuilder2.append("Empty non-indexable key from: ");
                            stringBuilder2.append(packageContext.getPackageName());
                            Log.v(str2, stringBuilder2.toString());
                        } else {
                            result.add(key);
                        }
                    }
                }
                cursor.close();
                return result;
            } catch (Throwable th) {
                cursor.close();
            }
        } catch (NullPointerException e) {
            Log.e(TAG, "Exception querying the keys!", e);
            return result;
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:15:0x0046, code skipped:
            return false;
     */
    @com.android.internal.annotations.VisibleForTesting
    public boolean isWellKnownProvider(android.content.pm.ResolveInfo r7) {
        /*
        r6 = this;
        r0 = r7.providerInfo;
        r0 = r0.authority;
        r1 = r7.providerInfo;
        r1 = r1.applicationInfo;
        r1 = r1.packageName;
        r2 = android.text.TextUtils.isEmpty(r0);
        r3 = 0;
        if (r2 != 0) goto L_0x0047;
    L_0x0011:
        r2 = android.text.TextUtils.isEmpty(r1);
        if (r2 == 0) goto L_0x0018;
    L_0x0017:
        goto L_0x0047;
    L_0x0018:
        r2 = r7.providerInfo;
        r2 = r2.readPermission;
        r4 = r7.providerInfo;
        r4 = r4.writePermission;
        r5 = android.text.TextUtils.isEmpty(r2);
        if (r5 != 0) goto L_0x0046;
    L_0x0026:
        r5 = android.text.TextUtils.isEmpty(r4);
        if (r5 == 0) goto L_0x002d;
    L_0x002c:
        goto L_0x0046;
    L_0x002d:
        r5 = "android.permission.READ_SEARCH_INDEXABLES";
        r5 = r5.equals(r2);
        if (r5 == 0) goto L_0x0045;
    L_0x0035:
        r5 = "android.permission.READ_SEARCH_INDEXABLES";
        r5 = r5.equals(r4);
        if (r5 != 0) goto L_0x003e;
    L_0x003d:
        goto L_0x0045;
    L_0x003e:
        r3 = r6.mContext;
        r3 = r6.isPrivilegedPackage(r1, r3);
        return r3;
    L_0x0045:
        return r3;
    L_0x0046:
        return r3;
    L_0x0047:
        return r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.search.indexing.PreIndexDataCollector.isWellKnownProvider(android.content.pm.ResolveInfo):boolean");
    }

    private boolean isPrivilegedPackage(String packageName, Context context) {
        boolean z = false;
        try {
            if ((context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.privateFlags & 8) != 0) {
                z = true;
            }
            return z;
        } catch (NameNotFoundException e) {
            return false;
        }
    }
}
