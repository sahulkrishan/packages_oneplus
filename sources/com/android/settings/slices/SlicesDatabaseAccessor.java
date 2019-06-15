package com.android.settings.slices;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Binder;
import android.util.Pair;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.slices.SlicesDatabaseHelper.IndexColumns;
import com.android.settings.slices.SlicesDatabaseHelper.Tables;

public class SlicesDatabaseAccessor {
    public static final String[] SELECT_COLUMNS_ALL = new String[]{"key", "title", "summary", IndexColumns.SCREENTITLE, "keywords", "icon", IndexColumns.FRAGMENT, "controller", "platform_slice", IndexColumns.SLICE_TYPE};
    private final int TRUE = 1;
    private final Context mContext;
    private final SlicesDatabaseHelper mHelper;

    public SlicesDatabaseAccessor(Context context) {
        this.mContext = context;
        this.mHelper = SlicesDatabaseHelper.getInstance(this.mContext);
    }

    public SliceData getSliceDataFromUri(Uri uri) {
        Pair<Boolean, String> pathData = SliceBuilderUtils.getPathData(uri);
        return buildSliceData(getIndexedSliceData((String) pathData.second), uri, ((Boolean) pathData.first).booleanValue());
    }

    public SliceData getSliceDataFromKey(String key) {
        return buildSliceData(getIndexedSliceData(key), null, false);
    }

    /* JADX WARNING: Missing block: B:23:0x004f, code skipped:
            if (r1 != null) goto L_0x0051;
     */
    /* JADX WARNING: Missing block: B:24:0x0051, code skipped:
            if (r2 != null) goto L_0x0053;
     */
    /* JADX WARNING: Missing block: B:26:?, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:27:0x0057, code skipped:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:28:0x0058, code skipped:
            r2.addSuppressed(r6);
     */
    /* JADX WARNING: Missing block: B:29:0x005c, code skipped:
            r1.close();
     */
    public java.util.List<java.lang.String> getSliceKeys(boolean r11) {
        /*
        r10 = this;
        r10.verifyIndexing();
        if (r11 == 0) goto L_0x0009;
    L_0x0005:
        r0 = "platform_slice = 1";
    L_0x0007:
        r4 = r0;
        goto L_0x000c;
    L_0x0009:
        r0 = "platform_slice = 0";
        goto L_0x0007;
    L_0x000c:
        r0 = r10.mHelper;
        r0 = r0.getReadableDatabase();
        r1 = "key";
        r3 = new java.lang.String[]{r1};
        r1 = new java.util.ArrayList;
        r1.<init>();
        r9 = r1;
        r2 = "slices_index";
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r8 = 0;
        r1 = r0;
        r1 = r1.query(r2, r3, r4, r5, r6, r7, r8);
        r2 = 0;
        r5 = r1.moveToFirst();	 Catch:{ Throwable -> 0x004d }
        if (r5 != 0) goto L_0x0037;
        if (r1 == 0) goto L_0x0036;
    L_0x0033:
        r1.close();
    L_0x0036:
        return r9;
    L_0x0037:
        r5 = 0;
        r5 = r1.getString(r5);	 Catch:{ Throwable -> 0x004d }
        r9.add(r5);	 Catch:{ Throwable -> 0x004d }
        r5 = r1.moveToNext();	 Catch:{ Throwable -> 0x004d }
        if (r5 != 0) goto L_0x0037;
    L_0x0045:
        if (r1 == 0) goto L_0x004a;
    L_0x0047:
        r1.close();
    L_0x004a:
        return r9;
    L_0x004b:
        r5 = move-exception;
        goto L_0x004f;
    L_0x004d:
        r2 = move-exception;
        throw r2;	 Catch:{ all -> 0x004b }
    L_0x004f:
        if (r1 == 0) goto L_0x005f;
    L_0x0051:
        if (r2 == 0) goto L_0x005c;
    L_0x0053:
        r1.close();	 Catch:{ Throwable -> 0x0057 }
        goto L_0x005f;
    L_0x0057:
        r6 = move-exception;
        r2.addSuppressed(r6);
        goto L_0x005f;
    L_0x005c:
        r1.close();
    L_0x005f:
        throw r5;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.slices.SlicesDatabaseAccessor.getSliceKeys(boolean):java.util.List");
    }

    private Cursor getIndexedSliceData(String path) {
        verifyIndexing();
        String whereClause = buildKeyMatchWhereClause();
        SQLiteDatabase resultCursor = this.mHelper.getReadableDatabase();
        Cursor resultCursor2 = resultCursor.query(Tables.TABLE_SLICES_INDEX, SELECT_COLUMNS_ALL, whereClause, new String[]{path}, null, null, null);
        int numResults = resultCursor2.getCount();
        StringBuilder stringBuilder;
        if (numResults == 0) {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid Slices key from path: ");
            stringBuilder.append(path);
            throw new IllegalStateException(stringBuilder.toString());
        } else if (numResults <= 1) {
            resultCursor2.moveToFirst();
            return resultCursor2;
        } else {
            stringBuilder = new StringBuilder();
            stringBuilder.append("Should not match more than 1 slice with path: ");
            stringBuilder.append(path);
            throw new IllegalStateException(stringBuilder.toString());
        }
    }

    private String buildKeyMatchWhereClause() {
        StringBuilder stringBuilder = new StringBuilder("key");
        stringBuilder.append(" = ?");
        return stringBuilder.toString();
    }

    private SliceData buildSliceData(Cursor cursor, Uri uri, boolean isIntentOnly) {
        String key = cursor.getString(cursor.getColumnIndex("key"));
        String title = cursor.getString(cursor.getColumnIndex("title"));
        String summary = cursor.getString(cursor.getColumnIndex("summary"));
        String screenTitle = cursor.getString(cursor.getColumnIndex(IndexColumns.SCREENTITLE));
        String keywords = cursor.getString(cursor.getColumnIndex("keywords"));
        int iconResource = cursor.getInt(cursor.getColumnIndex("icon"));
        String fragmentClassName = cursor.getString(cursor.getColumnIndex(IndexColumns.FRAGMENT));
        String controllerClassName = cursor.getString(cursor.getColumnIndex("controller"));
        boolean z = true;
        if (cursor.getInt(cursor.getColumnIndex("platform_slice")) != 1) {
            z = false;
        }
        boolean isPlatformDefined = z;
        int sliceType = cursor.getInt(cursor.getColumnIndex(IndexColumns.SLICE_TYPE));
        if (isIntentOnly) {
            sliceType = 0;
        }
        return new Builder().setKey(key).setTitle(title).setSummary(summary).setScreenTitle(screenTitle).setKeywords(keywords).setIcon(iconResource).setFragmentName(fragmentClassName).setPreferenceControllerClassName(controllerClassName).setUri(uri).setPlatformDefined(isPlatformDefined).setSliceType(sliceType).build();
    }

    private void verifyIndexing() {
        long uidToken = Binder.clearCallingIdentity();
        try {
            FeatureFactory.getFactory(this.mContext).getSlicesFeatureProvider().indexSliceData(this.mContext);
        } finally {
            Binder.restoreCallingIdentity(uidToken);
        }
    }
}
