package com.android.settings.search.indexing;

import android.content.Context;
import android.provider.SearchIndexableData;
import android.provider.SearchIndexableResource;
import android.util.Log;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.search.indexing.IndexData.Builder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexDataConverter {
    private static final String LOG_TAG = "IndexDataConverter";
    private static final String NODE_NAME_CHECK_BOX_PREFERENCE = "CheckBoxPreference";
    private static final String NODE_NAME_LIST_PREFERENCE = "ListPreference";
    private static final String NODE_NAME_PREFERENCE_SCREEN = "PreferenceScreen";
    private final Context mContext;

    public IndexDataConverter(Context context) {
        this.mContext = context;
    }

    public List<IndexData> convertPreIndexDataToIndexData(PreIndexData preIndexData) {
        long current = System.currentTimeMillis();
        List<SearchIndexableData> indexableData = preIndexData.dataToUpdate;
        Map<String, Set<String>> nonIndexableKeys = preIndexData.nonIndexableKeys;
        List<IndexData> indexData = new ArrayList();
        for (SearchIndexableData data : indexableData) {
            if (data instanceof SearchIndexableRaw) {
                SearchIndexableRaw rawData = (SearchIndexableRaw) data;
                Builder builder = convertRaw(rawData, (Set) nonIndexableKeys.get(rawData.intentTargetPackage));
                if (builder != null) {
                    indexData.add(builder.build(this.mContext));
                }
            } else if (data instanceof SearchIndexableResource) {
                SearchIndexableResource sir = (SearchIndexableResource) data;
                indexData.addAll(convertResource(sir, getNonIndexableKeysForResource(nonIndexableKeys, sir.packageName)));
            }
        }
        long endConversion = System.currentTimeMillis();
        String str = LOG_TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Converting pre-index data to index data took: ");
        stringBuilder.append(endConversion - current);
        Log.d(str, stringBuilder.toString());
        return indexData;
    }

    private Builder convertRaw(SearchIndexableRaw raw, Set<String> nonIndexableKeys) {
        boolean enabled = nonIndexableKeys == null || !nonIndexableKeys.contains(raw.key);
        Builder builder = new Builder();
        builder.setTitle(raw.title).setSummaryOn(raw.summaryOn).setEntries(raw.entries).setKeywords(raw.keywords).setClassName(raw.className).setScreenTitle(raw.screenTitle).setIconResId(raw.iconResId).setIntentAction(raw.intentAction).setIntentTargetPackage(raw.intentTargetPackage).setIntentTargetClass(raw.intentTargetClass).setEnabled(enabled).setKey(raw.key).setUserId(raw.userId);
        return builder;
    }

    /* JADX WARNING: Removed duplicated region for block: B:108:0x0289 A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x027d A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0289 A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x027d A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x029a  */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0289 A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x027d A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:108:0x0289 A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x027d A:{Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f, all -> 0x0297 }} */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x029a  */
    /* JADX WARNING: Missing block: B:86:0x0259, code skipped:
            if (r5 != null) goto L_0x025b;
     */
    /* JADX WARNING: Missing block: B:112:0x0293, code skipped:
            if (r5 == null) goto L_0x0296;
     */
    private java.util.List<com.android.settings.search.indexing.IndexData> convertResource(android.provider.SearchIndexableResource r50, java.util.Set<java.lang.String> r51) {
        /*
        r49 = this;
        r1 = r49;
        r2 = r50;
        r3 = r51;
        r4 = r2.context;
        r5 = 0;
        r0 = new java.util.ArrayList;
        r0.<init>();
        r6 = r0;
        r0 = r4.getResources();	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r7 = r2.xmlResId;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r0 = r0.getXml(r7);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r5 = r0;
    L_0x001a:
        r0 = r5.next();	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r7 = r0;
        r8 = 1;
        if (r0 == r8) goto L_0x0026;
    L_0x0022:
        r0 = 2;
        if (r7 == r0) goto L_0x0026;
    L_0x0025:
        goto L_0x001a;
    L_0x0026:
        r0 = r5.getName();	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r9 = r5.getDepth();	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r10 = android.util.Xml.asAttributeSet(r5);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r11 = com.android.settings.core.PreferenceXmlParserUtils.getDataTitle(r4, r10);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r12 = com.android.settings.core.PreferenceXmlParserUtils.getDataKey(r4, r10);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r13 = r2.className;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r14 = r2.intentAction;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r15 = r2.intentTargetPackage;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r8 = r2.intentTargetClass;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r17 = r0;
        r0 = new java.util.HashMap;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r0.<init>();	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        if (r13 == 0) goto L_0x0063;
        r18 = com.android.settings.search.DatabaseIndexingUtils.getPayloadKeyMap(r13, r4);	 Catch:{ XmlPullParserException -> 0x005f, IOException -> 0x005b, NotFoundException -> 0x0057, all -> 0x0053 }
        r0 = r18;
        goto L_0x0063;
    L_0x0053:
        r0 = move-exception;
        r2 = r6;
        goto L_0x0298;
    L_0x0057:
        r0 = move-exception;
        r2 = r6;
        goto L_0x0274;
    L_0x005b:
        r0 = move-exception;
        r2 = r6;
        goto L_0x0280;
    L_0x005f:
        r0 = move-exception;
        r2 = r6;
        goto L_0x028c;
    L_0x0063:
        r18 = com.android.settings.core.PreferenceXmlParserUtils.getDataTitle(r4, r10);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r19 = r18;
        r18 = com.android.settings.core.PreferenceXmlParserUtils.getDataSummary(r4, r10);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r20 = r18;
        r18 = com.android.settings.core.PreferenceXmlParserUtils.getDataKeywords(r4, r10);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r21 = r18;
        r18 = r3.contains(r12);	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r16 = 1;
        r18 = r18 ^ 1;
        r22 = r18;
        r2 = new com.android.settings.search.indexing.IndexData$Builder;	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r2.<init>();	 Catch:{ XmlPullParserException -> 0x028a, IOException -> 0x027e, NotFoundException -> 0x0272, all -> 0x026f }
        r24 = r6;
        r23 = r7;
        r7 = r19;
        r6 = r2.setTitle(r7);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r25 = r2;
        r2 = r20;
        r6 = r6.setSummaryOn(r2);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r26 = r2;
        r2 = r21;
        r6 = r6.setKeywords(r2);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6 = r6.setClassName(r13);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6 = r6.setScreenTitle(r11);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6 = r6.setIntentAction(r14);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6 = r6.setIntentTargetPackage(r15);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6 = r6.setIntentTargetClass(r8);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r27 = r2;
        r2 = r22;
        r6 = r6.setEnabled(r2);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6 = r6.setKey(r12);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r28 = r2;
        r2 = -1;
        r6.setUserId(r2);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r22 = r28;
        r6 = 1;
    L_0x00c7:
        r2 = r5.next();	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r29 = r2;
        r30 = r12;
        r12 = 1;
        if (r2 == r12) goto L_0x023b;
    L_0x00d2:
        r2 = 3;
        r12 = r29;
        if (r12 != r2) goto L_0x00fc;
    L_0x00d7:
        r2 = r5.getDepth();	 Catch:{ XmlPullParserException -> 0x00f7, IOException -> 0x00f2, NotFoundException -> 0x00ed, all -> 0x00e8 }
        if (r2 <= r9) goto L_0x00de;
    L_0x00dd:
        goto L_0x00fc;
    L_0x00de:
        r45 = r0;
        r38 = r7;
        r33 = r9;
        r2 = r24;
        goto L_0x0245;
    L_0x00e8:
        r0 = move-exception;
        r2 = r24;
        goto L_0x0298;
    L_0x00ed:
        r0 = move-exception;
        r2 = r24;
        goto L_0x0274;
    L_0x00f2:
        r0 = move-exception;
        r2 = r24;
        goto L_0x0280;
    L_0x00f7:
        r0 = move-exception;
        r2 = r24;
        goto L_0x028c;
    L_0x00fc:
        r2 = 3;
        if (r12 == r2) goto L_0x0220;
    L_0x00ff:
        r2 = 4;
        if (r12 != r2) goto L_0x010f;
        r45 = r0;
        r38 = r7;
        r33 = r9;
        r2 = r24;
        r16 = 1;
        goto L_0x022a;
    L_0x010f:
        r2 = r5.getName();	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r17 = com.android.settings.core.PreferenceXmlParserUtils.getDataTitle(r4, r10);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r31 = r17;
        r17 = com.android.settings.core.PreferenceXmlParserUtils.getDataKey(r4, r10);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r32 = r17;
        r33 = r9;
        r9 = r32;
        r17 = r3.contains(r9);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r16 = 1;
        r17 = r17 ^ 1;
        r34 = r17;
        r17 = com.android.settings.core.PreferenceXmlParserUtils.getDataKeywords(r4, r10);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r35 = r17;
        r17 = com.android.settings.core.PreferenceXmlParserUtils.getDataIcon(r4, r10);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r36 = r17;
        if (r6 == 0) goto L_0x0145;
    L_0x013b:
        r3 = r31;
        r17 = android.text.TextUtils.equals(r7, r3);	 Catch:{ XmlPullParserException -> 0x00f7, IOException -> 0x00f2, NotFoundException -> 0x00ed, all -> 0x00e8 }
        if (r17 == 0) goto L_0x0147;
    L_0x0143:
        r6 = 0;
        goto L_0x0147;
    L_0x0145:
        r3 = r31;
    L_0x0147:
        r37 = r6;
        r6 = new com.android.settings.search.indexing.IndexData$Builder;	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r6.<init>();	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r38 = r7;
        r7 = r6.setTitle(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r39 = r3;
        r3 = r35;
        r7 = r7.setKeywords(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = r7.setClassName(r13);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = r7.setScreenTitle(r11);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r40 = r3;
        r3 = r36;
        r7 = r7.setIconResId(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = r7.setIntentAction(r14);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = r7.setIntentTargetPackage(r15);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = r7.setIntentTargetClass(r8);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r41 = r3;
        r3 = r34;
        r7 = r7.setEnabled(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = r7.setKey(r9);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r42 = r3;
        r3 = -1;
        r7.setUserId(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r7 = "CheckBoxPreference";
        r7 = r2.equals(r7);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        if (r7 != 0) goto L_0x01de;
    L_0x0192:
        r7 = com.android.settings.core.PreferenceXmlParserUtils.getDataSummary(r4, r10);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r17 = 0;
        r3 = "ListPreference";
        r3 = r2.endsWith(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        if (r3 == 0) goto L_0x01a7;
    L_0x01a0:
        r3 = com.android.settings.core.PreferenceXmlParserUtils.getDataEntries(r4, r10);	 Catch:{ XmlPullParserException -> 0x00f7, IOException -> 0x00f2, NotFoundException -> 0x00ed, all -> 0x00e8 }
        r17 = r3;
        goto L_0x01a9;
    L_0x01a7:
        r3 = r17;
    L_0x01a9:
        r17 = r0.get(r9);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r17 = (com.android.settings.search.ResultPayload) r17;	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r43 = r17;
        r17 = com.android.settings.core.PreferenceXmlParserUtils.getDataChildFragment(r4, r10);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r44 = r17;
        r45 = r0;
        r0 = r6.setSummaryOn(r7);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r0 = r0.setEntries(r3);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r46 = r2;
        r2 = r44;
        r0 = r0.setChildClassName(r2);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r47 = r2;
        r2 = r43;
        r0.setPayload(r2);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r0 = r1.mContext;	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r0 = r6.build(r0);	 Catch:{ XmlPullParserException -> 0x026b, IOException -> 0x0267, NotFoundException -> 0x0263, all -> 0x025f }
        r48 = r2;
        r2 = r24;
        r2.add(r0);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        goto L_0x020a;
    L_0x01de:
        r45 = r0;
        r46 = r2;
        r2 = r24;
        r0 = com.android.settings.core.PreferenceXmlParserUtils.getDataSummaryOn(r4, r10);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r3 = com.android.settings.core.PreferenceXmlParserUtils.getDataSummaryOff(r4, r10);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r7 = android.text.TextUtils.isEmpty(r0);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        if (r7 == 0) goto L_0x01fd;
    L_0x01f2:
        r7 = android.text.TextUtils.isEmpty(r3);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        if (r7 == 0) goto L_0x01fd;
    L_0x01f8:
        r7 = com.android.settings.core.PreferenceXmlParserUtils.getDataSummary(r4, r10);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r0 = r7;
    L_0x01fd:
        r6.setSummaryOn(r0);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r7 = r1.mContext;	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r7 = r6.build(r7);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r2.add(r7);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
    L_0x020a:
        r24 = r2;
        r23 = r12;
        r6 = r37;
        r7 = r38;
        r22 = r42;
        r0 = r45;
        r17 = r46;
        r2 = -1;
        r3 = r51;
        r12 = r9;
        r9 = r33;
        goto L_0x00c7;
    L_0x0220:
        r45 = r0;
        r38 = r7;
        r33 = r9;
        r2 = r24;
        r16 = 1;
    L_0x022a:
        r24 = r2;
        r23 = r12;
        r12 = r30;
        r9 = r33;
        r7 = r38;
        r0 = r45;
        r2 = -1;
        r3 = r51;
        goto L_0x00c7;
    L_0x023b:
        r45 = r0;
        r38 = r7;
        r33 = r9;
        r2 = r24;
        r12 = r29;
    L_0x0245:
        if (r6 == 0) goto L_0x0259;
    L_0x0247:
        r0 = r1.mContext;	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r3 = r25;
        r0 = r3.build(r0);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        r2.add(r0);	 Catch:{ XmlPullParserException -> 0x0257, IOException -> 0x0255, NotFoundException -> 0x0253 }
        goto L_0x0259;
    L_0x0253:
        r0 = move-exception;
        goto L_0x0274;
    L_0x0255:
        r0 = move-exception;
        goto L_0x0280;
    L_0x0257:
        r0 = move-exception;
        goto L_0x028c;
    L_0x0259:
        if (r5 == 0) goto L_0x0296;
    L_0x025b:
        r5.close();
        goto L_0x0296;
    L_0x025f:
        r0 = move-exception;
        r2 = r24;
        goto L_0x0298;
    L_0x0263:
        r0 = move-exception;
        r2 = r24;
        goto L_0x0274;
    L_0x0267:
        r0 = move-exception;
        r2 = r24;
        goto L_0x0280;
    L_0x026b:
        r0 = move-exception;
        r2 = r24;
        goto L_0x028c;
    L_0x026f:
        r0 = move-exception;
        r2 = r6;
        goto L_0x0298;
    L_0x0272:
        r0 = move-exception;
        r2 = r6;
    L_0x0274:
        r3 = "IndexDataConverter";
        r6 = "Resoucre not found error parsing PreferenceScreen: ";
        android.util.Log.w(r3, r6, r0);	 Catch:{ all -> 0x0297 }
        if (r5 == 0) goto L_0x0296;
    L_0x027d:
        goto L_0x025b;
    L_0x027e:
        r0 = move-exception;
        r2 = r6;
    L_0x0280:
        r3 = "IndexDataConverter";
        r6 = "IO Error parsing PreferenceScreen: ";
        android.util.Log.w(r3, r6, r0);	 Catch:{ all -> 0x0297 }
        if (r5 == 0) goto L_0x0296;
    L_0x0289:
        goto L_0x025b;
    L_0x028a:
        r0 = move-exception;
        r2 = r6;
    L_0x028c:
        r3 = "IndexDataConverter";
        r6 = "XML Error parsing PreferenceScreen: ";
        android.util.Log.w(r3, r6, r0);	 Catch:{ all -> 0x0297 }
        if (r5 == 0) goto L_0x0296;
    L_0x0295:
        goto L_0x025b;
    L_0x0296:
        return r2;
    L_0x0297:
        r0 = move-exception;
    L_0x0298:
        if (r5 == 0) goto L_0x029d;
    L_0x029a:
        r5.close();
    L_0x029d:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.search.indexing.IndexDataConverter.convertResource(android.provider.SearchIndexableResource, java.util.Set):java.util.List");
    }

    private Set<String> getNonIndexableKeysForResource(Map<String, Set<String>> nonIndexableKeys, String packageName) {
        if (nonIndexableKeys.containsKey(packageName)) {
            return (Set) nonIndexableKeys.get(packageName);
        }
        return new HashSet();
    }
}
