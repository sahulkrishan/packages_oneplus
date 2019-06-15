package com.android.settings.slices;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.provider.SearchIndexableResource;
import android.util.Log;
import android.view.accessibility.AccessibilityManager;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.accessibility.AccessibilitySettings;
import com.android.settings.accessibility.AccessibilitySlicePreferenceController;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.DatabaseIndexingUtils;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.slices.SliceData.InvalidSliceDataException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

class SliceDataConverter {
    private static final String NODE_NAME_PREFERENCE_SCREEN = "PreferenceScreen";
    private static final String TAG = "SliceDataConverter";
    private Context mContext;
    private List<SliceData> mSliceData = new ArrayList();

    public SliceDataConverter(Context context) {
        this.mContext = context;
    }

    public List<SliceData> getSliceData() {
        if (!this.mSliceData.isEmpty()) {
            return this.mSliceData;
        }
        for (Class clazz : FeatureFactory.getFactory(this.mContext).getSearchFeatureProvider().getSearchIndexableResources().getProviderValues()) {
            String fragmentName = clazz.getName();
            SearchIndexProvider provider = DatabaseIndexingUtils.getSearchIndexProvider(clazz);
            if (provider == null) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(fragmentName);
                stringBuilder.append(" dose not implement Search Index Provider");
                Log.e(str, stringBuilder.toString());
            } else {
                this.mSliceData.addAll(getSliceDataFromProvider(provider, fragmentName));
            }
        }
        this.mSliceData.addAll(getAccessibilitySliceData());
        return this.mSliceData;
    }

    private List<SliceData> getSliceDataFromProvider(SearchIndexProvider provider, String fragmentName) {
        List<SliceData> sliceData = new ArrayList();
        List<SearchIndexableResource> resList = provider.getXmlResourcesToIndex(this.mContext, true);
        if (resList == null) {
            return sliceData;
        }
        for (SearchIndexableResource resource : resList) {
            int xmlResId = resource.xmlResId;
            if (xmlResId == 0) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(fragmentName);
                stringBuilder.append(" provides invalid XML (0) in search provider.");
                Log.e(str, stringBuilder.toString());
            } else {
                sliceData.addAll(getSliceDataFromXML(xmlResId, fragmentName));
            }
        }
        return sliceData;
    }

    /* JADX WARNING: Missing block: B:22:0x00d8, code skipped:
            if (r4 != null) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:44:0x013d, code skipped:
            if (r4 == null) goto L_0x0140;
     */
    private java.util.List<com.android.settings.slices.SliceData> getSliceDataFromXML(int r22, java.lang.String r23) {
        /*
        r21 = this;
        r1 = r21;
        r2 = r22;
        r3 = r23;
        r4 = 0;
        r0 = new java.util.ArrayList;
        r0.<init>();
        r5 = r0;
        r0 = r1.mContext;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r0 = r0.getResources();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r0 = r0.getXml(r2);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r4 = r0;
    L_0x0018:
        r0 = r4.next();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6 = r0;
        r7 = 1;
        if (r0 == r7) goto L_0x0024;
    L_0x0020:
        r0 = 2;
        if (r6 == r0) goto L_0x0024;
    L_0x0023:
        goto L_0x0018;
    L_0x0024:
        r0 = r4.getName();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r7 = "PreferenceScreen";
        r7 = r7.equals(r0);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        if (r7 == 0) goto L_0x00de;
    L_0x0030:
        r7 = android.util.Xml.asAttributeSet(r4);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r8 = r1.mContext;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r8 = com.android.settings.core.PreferenceXmlParserUtils.getDataTitle(r8, r7);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r1.mContext;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r10 = 254; // 0xfe float:3.56E-43 double:1.255E-321;
        r9 = com.android.settings.core.PreferenceXmlParserUtils.extractMetadata(r9, r2, r10);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r10 = r9.iterator();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
    L_0x0046:
        r11 = r10.hasNext();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        if (r11 == 0) goto L_0x00d8;
    L_0x004c:
        r11 = r10.next();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r11 = (android.os.Bundle) r11;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r12 = "controller";
        r12 = r11.getString(r12);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r13 = android.text.TextUtils.isEmpty(r12);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        if (r13 == 0) goto L_0x005f;
    L_0x005e:
        goto L_0x0046;
    L_0x005f:
        r13 = "key";
        r13 = r11.getString(r13);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r14 = "title";
        r14 = r11.getString(r14);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r15 = "summary";
        r15 = r11.getString(r15);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r2 = "icon";
        r2 = r11.getInt(r2);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r16 = r6;
        r6 = r1.mContext;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6 = com.android.settings.slices.SliceBuilderUtils.getSliceType(r6, r12, r13);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r17 = r7;
        r7 = "platform_slice";
        r7 = r11.getBoolean(r7);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r18 = r9;
        r9 = new com.android.settings.slices.SliceData$Builder;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9.<init>();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setKey(r13);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setTitle(r14);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setSummary(r15);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setIcon(r2);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setScreenTitle(r8);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setPreferenceControllerClassName(r12);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setFragmentName(r3);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setSliceType(r6);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.setPlatformDefined(r7);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r9 = r9.build();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r19 = r2;
        r2 = r1.mContext;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r2 = com.android.settings.slices.SliceBuilderUtils.getPreferenceController(r2, r9);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r20 = r2.isAvailable();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        if (r20 == 0) goto L_0x00cd;
    L_0x00c4:
        r20 = r2.isSliceable();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        if (r20 == 0) goto L_0x00cd;
    L_0x00ca:
        r5.add(r9);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6 = r16;
        r7 = r17;
        r9 = r18;
        r2 = r22;
        goto L_0x0046;
    L_0x00d8:
        if (r4 == 0) goto L_0x0140;
    L_0x00da:
        r4.close();
        goto L_0x0140;
    L_0x00de:
        r16 = r6;
        r2 = new java.lang.RuntimeException;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6 = new java.lang.StringBuilder;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6.<init>();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r7 = "XML document must start with <PreferenceScreen> tag; found";
        r6.append(r7);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6.append(r0);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r7 = " at ";
        r6.append(r7);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r7 = r4.getPositionDescription();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6.append(r7);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r6 = r6.toString();	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        r2.<init>(r6);	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
        throw r2;	 Catch:{ InvalidSliceDataException -> 0x0126, XmlPullParserException -> 0x011b, IOException -> 0x0110, NotFoundException -> 0x0105 }
    L_0x0103:
        r0 = move-exception;
        goto L_0x0141;
    L_0x0105:
        r0 = move-exception;
        r2 = "SliceDataConverter";
        r6 = "Resource not found error parsing PreferenceScreen: ";
        android.util.Log.w(r2, r6, r0);	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0140;
    L_0x010f:
        goto L_0x00da;
    L_0x0110:
        r0 = move-exception;
        r2 = "SliceDataConverter";
        r6 = "IO Error parsing PreferenceScreen: ";
        android.util.Log.w(r2, r6, r0);	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0140;
    L_0x011a:
        goto L_0x00da;
    L_0x011b:
        r0 = move-exception;
        r2 = "SliceDataConverter";
        r6 = "XML Error parsing PreferenceScreen: ";
        android.util.Log.w(r2, r6, r0);	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0140;
    L_0x0125:
        goto L_0x00da;
    L_0x0126:
        r0 = move-exception;
        r2 = "SliceDataConverter";
        r6 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0103 }
        r6.<init>();	 Catch:{ all -> 0x0103 }
        r7 = "Invalid data when building SliceData for ";
        r6.append(r7);	 Catch:{ all -> 0x0103 }
        r6.append(r3);	 Catch:{ all -> 0x0103 }
        r6 = r6.toString();	 Catch:{ all -> 0x0103 }
        android.util.Log.w(r2, r6, r0);	 Catch:{ all -> 0x0103 }
        if (r4 == 0) goto L_0x0140;
    L_0x013f:
        goto L_0x00da;
    L_0x0140:
        return r5;
    L_0x0141:
        if (r4 == 0) goto L_0x0146;
    L_0x0143:
        r4.close();
    L_0x0146:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.slices.SliceDataConverter.getSliceDataFromXML(int, java.lang.String):java.util.List");
    }

    private List<SliceData> getAccessibilitySliceData() {
        String fragmentClassName;
        CharSequence screenTitle;
        ArrayList sliceData = new ArrayList();
        String accessibilityControllerClassName = AccessibilitySlicePreferenceController.class.getName();
        String fragmentClassName2 = AccessibilitySettings.class.getName();
        CharSequence screenTitle2 = this.mContext.getText(R.string.accessibility_settings);
        Builder sliceDataBuilder = new Builder().setFragmentName(fragmentClassName2).setScreenTitle(screenTitle2).setPreferenceControllerClassName(accessibilityControllerClassName);
        HashSet a11yServiceNames = new HashSet();
        Collections.addAll(a11yServiceNames, this.mContext.getResources().getStringArray(R.array.config_settings_slices_accessibility_components));
        List<AccessibilityServiceInfo> installedServices = getAccessibilityServiceInfoList();
        PackageManager packageManager = this.mContext.getPackageManager();
        for (AccessibilityServiceInfo a11yServiceInfo : installedServices) {
            ResolveInfo resolveInfo = a11yServiceInfo.getResolveInfo();
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            String flattenedName = new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToString();
            if (a11yServiceNames.contains(flattenedName)) {
                String accessibilityControllerClassName2 = accessibilityControllerClassName;
                accessibilityControllerClassName = resolveInfo.loadLabel(packageManager).toString();
                int iconResource = resolveInfo.getIconResource();
                if (iconResource == 0) {
                    iconResource = R.mipmap.ic_accessibility_generic;
                }
                fragmentClassName = fragmentClassName2;
                int iconResource2 = iconResource;
                String title = accessibilityControllerClassName;
                sliceDataBuilder.setKey(flattenedName).setTitle(accessibilityControllerClassName).setIcon(iconResource2).setSliceType(1);
                try {
                    sliceData.add(sliceDataBuilder.build());
                    screenTitle = screenTitle2;
                } catch (InvalidSliceDataException e) {
                    accessibilityControllerClassName = TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    screenTitle = screenTitle2;
                    stringBuilder.append("Invalid data when building a11y SliceData for ");
                    stringBuilder.append(flattenedName);
                    Log.w(accessibilityControllerClassName, stringBuilder.toString(), e);
                }
                accessibilityControllerClassName = accessibilityControllerClassName2;
                fragmentClassName2 = fragmentClassName;
                screenTitle2 = screenTitle;
            }
        }
        fragmentClassName = fragmentClassName2;
        screenTitle = screenTitle2;
        return sliceData;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public List<AccessibilityServiceInfo> getAccessibilityServiceInfoList() {
        return AccessibilityManager.getInstance(this.mContext).getInstalledAccessibilityServiceList();
    }
}
