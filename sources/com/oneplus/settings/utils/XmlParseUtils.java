package com.oneplus.settings.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import com.android.settings.R;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import java.util.Arrays;
import java.util.List;

public class XmlParseUtils {
    private static final String NODE_NAME_ONEPLUS_PREFERENCE_SCREEN = "com.oneplus.lib.preference.PreferenceScreen";
    private static final String NODE_NAME_PREFERENCE_SCREEN = "PreferenceScreen";
    private static final List<String> SKIP_NODES = Arrays.asList(new String[]{SliceDeepLinkSpringBoard.INTENT, "extra"});
    private static final String TAG = "XmlParserUtils";

    private static String getData(Context context, AttributeSet set, int[] attrs, int resId) {
        TypedArray ta = context.obtainStyledAttributes(set, attrs);
        String data = ta.getString(resId);
        ta.recycle();
        return data;
    }

    public static String getDataKey(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.Preference, 6);
    }

    public static String getDataTitle(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.Preference, 4);
    }

    public static String getDataSummary(Context context, AttributeSet attrs) {
        return getData(context, attrs, R.styleable.Preference, 7);
    }

    /* JADX WARNING: Missing block: B:31:0x00a4, code skipped:
            if (r0 == null) goto L_0x00ca;
     */
    /* JADX WARNING: Missing block: B:32:0x00a6, code skipped:
            r0.close();
     */
    /* JADX WARNING: Missing block: B:37:0x00c7, code skipped:
            if (r0 == null) goto L_0x00ca;
     */
    /* JADX WARNING: Missing block: B:38:0x00ca, code skipped:
            return r1;
     */
    public static java.util.List<java.lang.String> parsePreferenceKeyFromResource(int r11, android.content.Context r12) {
        /*
        r0 = 0;
        r1 = new java.util.ArrayList;
        r1.<init>();
        r2 = r12.getResources();	 Catch:{ Exception -> 0x00ac }
        r2 = r2.getXml(r11);	 Catch:{ Exception -> 0x00ac }
        r0 = r2;
    L_0x000f:
        r2 = r0.next();	 Catch:{ Exception -> 0x00ac }
        r3 = r2;
        r4 = 1;
        if (r2 == r4) goto L_0x001b;
    L_0x0017:
        r2 = 2;
        if (r3 == r2) goto L_0x001b;
    L_0x001a:
        goto L_0x000f;
    L_0x001b:
        r2 = r0.getName();	 Catch:{ Exception -> 0x00ac }
        r5 = "PreferenceScreen";
        r5 = r5.equals(r2);	 Catch:{ Exception -> 0x00ac }
        if (r5 != 0) goto L_0x0053;
    L_0x0027:
        r5 = "com.oneplus.lib.preference.PreferenceScreen";
        r5 = r5.equals(r2);	 Catch:{ Exception -> 0x00ac }
        if (r5 == 0) goto L_0x0030;
    L_0x002f:
        goto L_0x0053;
    L_0x0030:
        r4 = new java.lang.RuntimeException;	 Catch:{ Exception -> 0x00ac }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00ac }
        r5.<init>();	 Catch:{ Exception -> 0x00ac }
        r6 = "XML document must start with <PreferenceScreen> tag; found";
        r5.append(r6);	 Catch:{ Exception -> 0x00ac }
        r5.append(r2);	 Catch:{ Exception -> 0x00ac }
        r6 = " at ";
        r5.append(r6);	 Catch:{ Exception -> 0x00ac }
        r6 = r0.getPositionDescription();	 Catch:{ Exception -> 0x00ac }
        r5.append(r6);	 Catch:{ Exception -> 0x00ac }
        r5 = r5.toString();	 Catch:{ Exception -> 0x00ac }
        r4.<init>(r5);	 Catch:{ Exception -> 0x00ac }
        throw r4;	 Catch:{ Exception -> 0x00ac }
    L_0x0053:
        r5 = r0.getDepth();	 Catch:{ Exception -> 0x00ac }
        r6 = android.util.Xml.asAttributeSet(r0);	 Catch:{ Exception -> 0x00ac }
        r7 = getDataKey(r12, r6);	 Catch:{ Exception -> 0x00ac }
        r1.add(r7);	 Catch:{ Exception -> 0x00ac }
    L_0x0062:
        r8 = r0.next();	 Catch:{ Exception -> 0x00ac }
        r3 = r8;
        if (r8 == r4) goto L_0x00a4;
    L_0x0069:
        r8 = 3;
        if (r3 != r8) goto L_0x0072;
    L_0x006c:
        r9 = r0.getDepth();	 Catch:{ Exception -> 0x00ac }
        if (r9 <= r5) goto L_0x00a4;
    L_0x0072:
        if (r3 == r8) goto L_0x0062;
    L_0x0074:
        r8 = 4;
        if (r3 != r8) goto L_0x0078;
    L_0x0077:
        goto L_0x0062;
    L_0x0078:
        r8 = r0.getName();	 Catch:{ Exception -> 0x00ac }
        r2 = r8;
        r8 = SKIP_NODES;	 Catch:{ Exception -> 0x00ac }
        r8 = r8.contains(r2);	 Catch:{ Exception -> 0x00ac }
        if (r8 == 0) goto L_0x009c;
    L_0x0085:
        r8 = "XmlParserUtils";
        r9 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00ac }
        r9.<init>();	 Catch:{ Exception -> 0x00ac }
        r9.append(r2);	 Catch:{ Exception -> 0x00ac }
        r10 = " is not a valid entity to index, skip.";
        r9.append(r10);	 Catch:{ Exception -> 0x00ac }
        r9 = r9.toString();	 Catch:{ Exception -> 0x00ac }
        android.util.Log.d(r8, r9);	 Catch:{ Exception -> 0x00ac }
        goto L_0x0062;
    L_0x009c:
        r8 = getDataKey(r12, r6);	 Catch:{ Exception -> 0x00ac }
        r1.add(r8);	 Catch:{ Exception -> 0x00ac }
        goto L_0x0062;
    L_0x00a4:
        if (r0 == 0) goto L_0x00ca;
    L_0x00a6:
        r0.close();
        goto L_0x00ca;
    L_0x00aa:
        r2 = move-exception;
        goto L_0x00cb;
    L_0x00ac:
        r2 = move-exception;
        r3 = "XmlParserUtils";
        r4 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00aa }
        r4.<init>();	 Catch:{ all -> 0x00aa }
        r5 = "XML Error parsing e:";
        r4.append(r5);	 Catch:{ all -> 0x00aa }
        r5 = r2.getMessage();	 Catch:{ all -> 0x00aa }
        r4.append(r5);	 Catch:{ all -> 0x00aa }
        r4 = r4.toString();	 Catch:{ all -> 0x00aa }
        android.util.Log.w(r3, r4);	 Catch:{ all -> 0x00aa }
        if (r0 == 0) goto L_0x00ca;
    L_0x00c9:
        goto L_0x00a6;
    L_0x00ca:
        return r1;
    L_0x00cb:
        if (r0 == 0) goto L_0x00d0;
    L_0x00cd:
        r0.close();
    L_0x00d0:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.utils.XmlParseUtils.parsePreferenceKeyFromResource(int, android.content.Context):java.util.List");
    }
}
