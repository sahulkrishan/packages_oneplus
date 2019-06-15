package com.android.settings.inputmethod;

import android.content.Context;
import android.content.CursorLoader;
import android.support.annotation.VisibleForTesting;
import com.oneplus.settings.utils.OPFirewallUtils;

public class UserDictionaryCursorLoader extends CursorLoader {
    static final int INDEX_SHORTCUT = 2;
    @VisibleForTesting
    static final String[] QUERY_PROJECTION = new String[]{OPFirewallUtils._ID, UserDictionaryAddWordContents.EXTRA_WORD, UserDictionaryAddWordContents.EXTRA_SHORTCUT};
    private static final String QUERY_SELECTION = "locale=?";
    private static final String QUERY_SELECTION_ALL_LOCALES = "locale is null";
    private final String mLocale;

    public UserDictionaryCursorLoader(Context context, String locale) {
        super(context);
        this.mLocale = locale;
    }

    /* JADX WARNING: Missing block: B:19:0x009b, code skipped:
            if (r1 != null) goto L_0x00a8;
     */
    /* JADX WARNING: Missing block: B:25:0x00a6, code skipped:
            if (r1 == null) goto L_0x00ab;
     */
    /* JADX WARNING: Missing block: B:26:0x00a8, code skipped:
            r1.close();
     */
    /* JADX WARNING: Missing block: B:27:0x00ab, code skipped:
            return r0;
     */
    public android.database.Cursor loadInBackground() {
        /*
        r12 = this;
        r0 = new android.database.MatrixCursor;
        r1 = QUERY_PROJECTION;
        r0.<init>(r1);
        r1 = "";
        r2 = r12.mLocale;
        r1 = r1.equals(r2);
        r2 = 0;
        r3 = 1;
        if (r1 == 0) goto L_0x0029;
    L_0x0013:
        r1 = r12.getContext();
        r4 = r1.getContentResolver();
        r5 = android.provider.UserDictionary.Words.CONTENT_URI;
        r6 = QUERY_PROJECTION;
        r7 = "locale is null";
        r8 = 0;
        r9 = "UPPER(word)";
        r1 = r4.query(r5, r6, r7, r8, r9);
        goto L_0x0050;
    L_0x0029:
        r1 = r12.mLocale;
        if (r1 == 0) goto L_0x0030;
    L_0x002d:
        r1 = r12.mLocale;
        goto L_0x0038;
    L_0x0030:
        r1 = java.util.Locale.getDefault();
        r1 = r1.toString();
    L_0x0038:
        r4 = r12.getContext();
        r5 = r4.getContentResolver();
        r6 = android.provider.UserDictionary.Words.CONTENT_URI;
        r7 = QUERY_PROJECTION;
        r8 = "locale=?";
        r9 = new java.lang.String[r3];
        r9[r2] = r1;
        r10 = "UPPER(word)";
        r1 = r5.query(r6, r7, r8, r9, r10);
    L_0x0050:
        r4 = new android.util.ArraySet;
        r4.<init>();
        r1.moveToFirst();	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
    L_0x0058:
        r5 = r1.isAfterLast();	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        if (r5 != 0) goto L_0x009b;
    L_0x005e:
        r5 = r1.getInt(r2);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r6 = r1.getString(r3);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r7 = 2;
        r8 = r1.getString(r7);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r9 = new java.lang.Object[r7];	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r9[r2] = r6;	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r9[r3] = r8;	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r9 = java.util.Objects.hash(r9);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r10 = java.lang.Integer.valueOf(r9);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r10 = r4.contains(r10);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        if (r10 == 0) goto L_0x0080;
    L_0x007f:
        goto L_0x0097;
    L_0x0080:
        r10 = java.lang.Integer.valueOf(r9);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r4.add(r10);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r10 = 3;
        r10 = new java.lang.Object[r10];	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r11 = java.lang.Integer.valueOf(r5);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r10[r2] = r11;	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r10[r3] = r6;	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r10[r7] = r8;	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        r0.addRow(r10);	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
    L_0x0097:
        r1.moveToNext();	 Catch:{ Exception -> 0x00a5, all -> 0x009e }
        goto L_0x0058;
    L_0x009b:
        if (r1 == 0) goto L_0x00ab;
    L_0x009d:
        goto L_0x00a8;
    L_0x009e:
        r2 = move-exception;
        if (r1 == 0) goto L_0x00a4;
    L_0x00a1:
        r1.close();
    L_0x00a4:
        throw r2;
    L_0x00a5:
        r2 = move-exception;
        if (r1 == 0) goto L_0x00ab;
    L_0x00a8:
        r1.close();
    L_0x00ab:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.inputmethod.UserDictionaryCursorLoader.loadInBackground():android.database.Cursor");
    }
}
