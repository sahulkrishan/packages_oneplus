package com.android.settings.security.trustagent;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.widget.LockPatternUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import java.util.ArrayList;
import java.util.List;

public class TrustAgentManager {
    private static final boolean ONLY_ONE_TRUST_AGENT = true;
    @VisibleForTesting
    static final String PERMISSION_PROVIDE_AGENT = "android.permission.PROVIDE_TRUST_AGENT";
    private static final String TAG = "TrustAgentManager";
    private static final Intent TRUST_AGENT_INTENT = new Intent("android.service.trust.TrustAgentService");

    public static class TrustAgentComponentInfo {
        public EnforcedAdmin admin = null;
        public ComponentName componentName;
        public String summary;
        public String title;
    }

    public boolean shouldProvideTrust(ResolveInfo resolveInfo, PackageManager pm) {
        String packageName = resolveInfo.serviceInfo.packageName;
        if (pm.checkPermission(PERMISSION_PROVIDE_AGENT, packageName) == 0) {
            return true;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Skipping agent because package ");
        stringBuilder.append(packageName);
        stringBuilder.append(" does not have permission ");
        stringBuilder.append(PERMISSION_PROVIDE_AGENT);
        stringBuilder.append(".");
        Log.w(str, stringBuilder.toString());
        return false;
    }

    public CharSequence getActiveTrustAgentLabel(Context context, LockPatternUtils utils) {
        List<TrustAgentComponentInfo> agents = getActiveTrustAgents(context, utils);
        return agents.isEmpty() ? null : ((TrustAgentComponentInfo) agents.get(0)).title;
    }

    public List<TrustAgentComponentInfo> getActiveTrustAgents(Context context, LockPatternUtils utils) {
        int myUserId = UserHandle.myUserId();
        DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(DevicePolicyManager.class);
        PackageManager pm = context.getPackageManager();
        List<TrustAgentComponentInfo> result = new ArrayList();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(TRUST_AGENT_INTENT, 128);
        List<ComponentName> enabledTrustAgents = utils.getEnabledTrustAgents(myUserId);
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(context, 16, myUserId);
        if (!(enabledTrustAgents == null || enabledTrustAgents.isEmpty())) {
            for (ResolveInfo resolveInfo : resolveInfos) {
                if (resolveInfo.serviceInfo != null) {
                    if (shouldProvideTrust(resolveInfo, pm)) {
                        TrustAgentComponentInfo trustAgentComponentInfo = getSettingsComponent(pm, resolveInfo);
                        if (trustAgentComponentInfo.componentName != null && enabledTrustAgents.contains(getComponentName(resolveInfo))) {
                            if (!TextUtils.isEmpty(trustAgentComponentInfo.title)) {
                                if (admin != null && dpm.getTrustAgentConfiguration(null, getComponentName(resolveInfo)) == null) {
                                    trustAgentComponentInfo.admin = admin;
                                }
                                result.add(trustAgentComponentInfo);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    public ComponentName getComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    /* JADX WARNING: Missing block: B:28:0x007e, code skipped:
            if (r3 != null) goto L_0x0080;
     */
    /* JADX WARNING: Missing block: B:29:0x0080, code skipped:
            r3.close();
     */
    /* JADX WARNING: Missing block: B:36:0x008d, code skipped:
            if (r3 == null) goto L_0x009a;
     */
    /* JADX WARNING: Missing block: B:39:0x0092, code skipped:
            if (r3 == null) goto L_0x009a;
     */
    /* JADX WARNING: Missing block: B:42:0x0097, code skipped:
            if (r3 == null) goto L_0x009a;
     */
    /* JADX WARNING: Missing block: B:43:0x009a, code skipped:
            if (r4 == null) goto L_0x00b7;
     */
    /* JADX WARNING: Missing block: B:44:0x009c, code skipped:
            r5 = TAG;
            r6 = new java.lang.StringBuilder();
            r6.append("Error parsing : ");
            r6.append(r14.serviceInfo.packageName);
            android.util.Slog.w(r5, r6.toString(), r4);
     */
    /* JADX WARNING: Missing block: B:45:0x00b6, code skipped:
            return null;
     */
    /* JADX WARNING: Missing block: B:46:0x00b7, code skipped:
            if (r1 == null) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:48:0x00bf, code skipped:
            if (r1.indexOf(47) >= 0) goto L_0x00da;
     */
    /* JADX WARNING: Missing block: B:49:0x00c1, code skipped:
            r5 = new java.lang.StringBuilder();
            r5.append(r14.serviceInfo.packageName);
            r5.append("/");
            r5.append(r1);
            r1 = r5.toString();
     */
    /* JADX WARNING: Missing block: B:50:0x00da, code skipped:
            if (r1 != null) goto L_0x00dd;
     */
    /* JADX WARNING: Missing block: B:51:0x00dd, code skipped:
            r0 = android.content.ComponentName.unflattenFromString(r1);
     */
    /* JADX WARNING: Missing block: B:52:0x00e1, code skipped:
            r2.componentName = r0;
     */
    /* JADX WARNING: Missing block: B:53:0x00e3, code skipped:
            return r2;
     */
    private com.android.settings.security.trustagent.TrustAgentManager.TrustAgentComponentInfo getSettingsComponent(android.content.pm.PackageManager r13, android.content.pm.ResolveInfo r14) {
        /*
        r12 = this;
        r0 = 0;
        if (r14 == 0) goto L_0x00e4;
    L_0x0003:
        r1 = r14.serviceInfo;
        if (r1 == 0) goto L_0x00e4;
    L_0x0007:
        r1 = r14.serviceInfo;
        r1 = r1.metaData;
        if (r1 != 0) goto L_0x000f;
    L_0x000d:
        goto L_0x00e4;
    L_0x000f:
        r1 = 0;
        r2 = new com.android.settings.security.trustagent.TrustAgentManager$TrustAgentComponentInfo;
        r2.<init>();
        r3 = 0;
        r4 = r0;
        r5 = r14.serviceInfo;	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r6 = "android.service.trust.trustagent";
        r5 = r5.loadXmlMetaData(r13, r6);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r3 = r5;
        if (r3 != 0) goto L_0x0030;
    L_0x0022:
        r5 = "TrustAgentManager";
        r6 = "Can't find android.service.trust.trustagent meta-data";
        android.util.Slog.w(r5, r6);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        if (r3 == 0) goto L_0x002f;
    L_0x002c:
        r3.close();
    L_0x002f:
        return r0;
    L_0x0030:
        r5 = r14.serviceInfo;	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r5 = r5.applicationInfo;	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r5 = r13.getResourcesForApplication(r5);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r6 = android.util.Xml.asAttributeSet(r3);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
    L_0x003c:
        r7 = r3.next();	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r8 = r7;
        r9 = 2;
        r10 = 1;
        if (r7 == r10) goto L_0x0048;
    L_0x0045:
        if (r8 == r9) goto L_0x0048;
    L_0x0047:
        goto L_0x003c;
    L_0x0048:
        r7 = r3.getName();	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r11 = "trust-agent";
        r11 = r11.equals(r7);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        if (r11 != 0) goto L_0x0062;
    L_0x0054:
        r9 = "TrustAgentManager";
        r10 = "Meta-data does not start with trust-agent tag";
        android.util.Slog.w(r9, r10);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        if (r3 == 0) goto L_0x0061;
    L_0x005e:
        r3.close();
    L_0x0061:
        return r0;
    L_0x0062:
        r11 = com.android.internal.R.styleable.TrustAgent;	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r11 = r5.obtainAttributes(r6, r11);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r10 = r11.getString(r10);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r2.summary = r10;	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r10 = 0;
        r10 = r11.getString(r10);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r2.title = r10;	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r9 = r11.getString(r9);	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        r1 = r9;
        r11.recycle();	 Catch:{ NameNotFoundException -> 0x0095, IOException -> 0x0090, XmlPullParserException -> 0x008b, all -> 0x0084 }
        if (r3 == 0) goto L_0x009a;
    L_0x0080:
        r3.close();
        goto L_0x009a;
    L_0x0084:
        r0 = move-exception;
        if (r3 == 0) goto L_0x008a;
    L_0x0087:
        r3.close();
    L_0x008a:
        throw r0;
    L_0x008b:
        r5 = move-exception;
        r4 = r5;
        if (r3 == 0) goto L_0x009a;
    L_0x008f:
        goto L_0x0080;
    L_0x0090:
        r5 = move-exception;
        r4 = r5;
        if (r3 == 0) goto L_0x009a;
    L_0x0094:
        goto L_0x0080;
    L_0x0095:
        r5 = move-exception;
        r4 = r5;
        if (r3 == 0) goto L_0x009a;
    L_0x0099:
        goto L_0x0080;
    L_0x009a:
        if (r4 == 0) goto L_0x00b7;
    L_0x009c:
        r5 = "TrustAgentManager";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "Error parsing : ";
        r6.append(r7);
        r7 = r14.serviceInfo;
        r7 = r7.packageName;
        r6.append(r7);
        r6 = r6.toString();
        android.util.Slog.w(r5, r6, r4);
        return r0;
    L_0x00b7:
        if (r1 == 0) goto L_0x00d9;
    L_0x00b9:
        r5 = 47;
        r5 = r1.indexOf(r5);
        if (r5 >= 0) goto L_0x00d9;
    L_0x00c1:
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = r14.serviceInfo;
        r6 = r6.packageName;
        r5.append(r6);
        r6 = "/";
        r5.append(r6);
        r5.append(r1);
        r1 = r5.toString();
        if (r1 != 0) goto L_0x00dd;
    L_0x00dc:
        goto L_0x00e1;
    L_0x00dd:
        r0 = android.content.ComponentName.unflattenFromString(r1);
    L_0x00e1:
        r2.componentName = r0;
        return r2;
    L_0x00e4:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.security.trustagent.TrustAgentManager.getSettingsComponent(android.content.pm.PackageManager, android.content.pm.ResolveInfo):com.android.settings.security.trustagent.TrustAgentManager$TrustAgentComponentInfo");
    }
}
