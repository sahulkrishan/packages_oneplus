package com.android.settingslib.dream;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings.Secure;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DreamBackend {
    private static final boolean DEBUG = false;
    public static final int EITHER = 2;
    public static final int NEVER = 3;
    private static final String TAG = "DreamBackend";
    public static final int WHILE_CHARGING = 0;
    public static final int WHILE_DOCKED = 1;
    private static DreamBackend sInstance;
    private final DreamInfoComparator mComparator = new DreamInfoComparator(getDefaultDream());
    private final Context mContext;
    private final IDreamManager mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
    private final boolean mDreamsActivatedOnDockByDefault = this.mContext.getResources().getBoolean(17956942);
    private final boolean mDreamsActivatedOnSleepByDefault = this.mContext.getResources().getBoolean(17956943);
    private final boolean mDreamsEnabledByDefault = this.mContext.getResources().getBoolean(17956944);

    public static class DreamInfo {
        public CharSequence caption;
        public ComponentName componentName;
        public Drawable icon;
        public boolean isActive;
        public ComponentName settingsComponentName;

        public String toString() {
            StringBuilder sb = new StringBuilder(DreamInfo.class.getSimpleName());
            sb.append('[');
            sb.append(this.caption);
            if (this.isActive) {
                sb.append(",active");
            }
            sb.append(',');
            sb.append(this.componentName);
            if (this.settingsComponentName != null) {
                sb.append("settings=");
                sb.append(this.settingsComponentName);
            }
            sb.append(']');
            return sb.toString();
        }
    }

    private static class DreamInfoComparator implements Comparator<DreamInfo> {
        private final ComponentName mDefaultDream;

        public DreamInfoComparator(ComponentName defaultDream) {
            this.mDefaultDream = defaultDream;
        }

        public int compare(DreamInfo lhs, DreamInfo rhs) {
            return sortKey(lhs).compareTo(sortKey(rhs));
        }

        private String sortKey(DreamInfo di) {
            StringBuilder sb = new StringBuilder();
            sb.append(di.componentName.equals(this.mDefaultDream) ? '0' : '1');
            sb.append(di.caption);
            return sb.toString();
        }
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WhenToDream {
    }

    public static DreamBackend getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DreamBackend(context);
        }
        return sInstance;
    }

    public DreamBackend(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public List<DreamInfo> getDreamInfos() {
        logd("getDreamInfos()", new Object[0]);
        ComponentName activeDream = getActiveDream();
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent("android.service.dreams.DreamService"), 128);
        List<DreamInfo> dreamInfos = new ArrayList(resolveInfos.size());
        for (ResolveInfo resolveInfo : resolveInfos) {
            if (resolveInfo.serviceInfo != null) {
                DreamInfo dreamInfo = new DreamInfo();
                dreamInfo.caption = resolveInfo.loadLabel(pm);
                dreamInfo.icon = resolveInfo.loadIcon(pm);
                dreamInfo.componentName = getDreamComponentName(resolveInfo);
                dreamInfo.isActive = dreamInfo.componentName.equals(activeDream);
                dreamInfo.settingsComponentName = getSettingsComponentName(pm, resolveInfo);
                dreamInfos.add(dreamInfo);
            }
        }
        Collections.sort(dreamInfos, this.mComparator);
        return dreamInfos;
    }

    public ComponentName getDefaultDream() {
        if (this.mDreamManager == null) {
            return null;
        }
        try {
            return this.mDreamManager.getDefaultDreamComponent();
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get default dream", e);
            return null;
        }
    }

    public CharSequence getActiveDreamName() {
        ComponentName cn = getActiveDream();
        if (cn != null) {
            PackageManager pm = this.mContext.getPackageManager();
            try {
                ServiceInfo ri = pm.getServiceInfo(cn, null);
                if (ri != null) {
                    return ri.loadLabel(pm);
                }
            } catch (NameNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    public int getWhenToDreamSetting() {
        int i = 3;
        if (!isEnabled()) {
            return 3;
        }
        if (isActivatedOnDock() && isActivatedOnSleep()) {
            i = 2;
        } else if (isActivatedOnDock()) {
            i = 1;
        } else if (isActivatedOnSleep()) {
            i = 0;
        }
        return i;
    }

    public void setWhenToDream(int whenToDream) {
        setEnabled(whenToDream != 3);
        switch (whenToDream) {
            case 0:
                setActivatedOnDock(false);
                setActivatedOnSleep(true);
                return;
            case 1:
                setActivatedOnDock(true);
                setActivatedOnSleep(false);
                return;
            case 2:
                setActivatedOnDock(true);
                setActivatedOnSleep(true);
                return;
            default:
                return;
        }
    }

    public boolean isEnabled() {
        return getBoolean("screensaver_enabled", this.mDreamsEnabledByDefault);
    }

    public void setEnabled(boolean value) {
        logd("setEnabled(%s)", Boolean.valueOf(value));
        setBoolean("screensaver_enabled", value);
    }

    public boolean isActivatedOnDock() {
        return getBoolean("screensaver_activate_on_dock", this.mDreamsActivatedOnDockByDefault);
    }

    public void setActivatedOnDock(boolean value) {
        logd("setActivatedOnDock(%s)", Boolean.valueOf(value));
        setBoolean("screensaver_activate_on_dock", value);
    }

    public boolean isActivatedOnSleep() {
        return getBoolean("screensaver_activate_on_sleep", this.mDreamsActivatedOnSleepByDefault);
    }

    public void setActivatedOnSleep(boolean value) {
        logd("setActivatedOnSleep(%s)", Boolean.valueOf(value));
        setBoolean("screensaver_activate_on_sleep", value);
    }

    private boolean getBoolean(String key, boolean def) {
        return Secure.getInt(this.mContext.getContentResolver(), key, def) == 1;
    }

    private void setBoolean(String key, boolean value) {
        Secure.putInt(this.mContext.getContentResolver(), key, value);
    }

    public void setActiveDream(ComponentName dream) {
        logd("setActiveDream(%s)", dream);
        if (this.mDreamManager != null) {
            try {
                this.mDreamManager.setDreamComponents(dream == null ? null : new ComponentName[]{dream});
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to set active dream to ");
                stringBuilder.append(dream);
                Log.w(str, stringBuilder.toString(), e);
            }
        }
    }

    public ComponentName getActiveDream() {
        ComponentName componentName = null;
        if (this.mDreamManager == null) {
            return null;
        }
        try {
            ComponentName[] dreams = this.mDreamManager.getDreamComponents();
            if (dreams != null && dreams.length > 0) {
                componentName = dreams[0];
            }
            return componentName;
        } catch (RemoteException e) {
            Log.w(TAG, "Failed to get active dream", e);
            return null;
        }
    }

    public void launchSettings(DreamInfo dreamInfo) {
        logd("launchSettings(%s)", dreamInfo);
        if (dreamInfo != null && dreamInfo.settingsComponentName != null) {
            try {
                this.mContext.startActivity(new Intent().setComponent(dreamInfo.settingsComponentName));
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setComponent(dreamInfo.settingsComponentName);
                intent.addFlags(268435456);
                this.mContext.startActivity(intent);
            }
        }
    }

    public void preview(DreamInfo dreamInfo) {
        logd("preview(%s)", dreamInfo);
        if (this.mDreamManager != null && dreamInfo != null && dreamInfo.componentName != null) {
            try {
                this.mDreamManager.testDream(dreamInfo.componentName);
            } catch (RemoteException e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Failed to preview ");
                stringBuilder.append(dreamInfo);
                Log.w(str, stringBuilder.toString(), e);
            }
        }
    }

    public void startDreaming() {
        logd("startDreaming()", new Object[0]);
        if (this.mDreamManager != null) {
            try {
                this.mDreamManager.dream();
            } catch (RemoteException e) {
                Log.w(TAG, "Failed to dream", e);
            }
        }
    }

    private static ComponentName getDreamComponentName(ResolveInfo resolveInfo) {
        if (resolveInfo == null || resolveInfo.serviceInfo == null) {
            return null;
        }
        return new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name);
    }

    /* JADX WARNING: Missing block: B:29:0x006c, code skipped:
            if (r2 != null) goto L_0x006e;
     */
    /* JADX WARNING: Missing block: B:30:0x006e, code skipped:
            r2.close();
     */
    /* JADX WARNING: Missing block: B:37:0x007b, code skipped:
            if (r2 == null) goto L_0x007e;
     */
    /* JADX WARNING: Missing block: B:38:0x007e, code skipped:
            if (r3 == null) goto L_0x009b;
     */
    /* JADX WARNING: Missing block: B:39:0x0080, code skipped:
            r4 = TAG;
            r5 = new java.lang.StringBuilder();
            r5.append("Error parsing : ");
            r5.append(r11.serviceInfo.packageName);
            android.util.Log.w(r4, r5.toString(), r3);
     */
    /* JADX WARNING: Missing block: B:40:0x009a, code skipped:
            return null;
     */
    /* JADX WARNING: Missing block: B:41:0x009b, code skipped:
            if (r1 == null) goto L_0x00bd;
     */
    /* JADX WARNING: Missing block: B:43:0x00a3, code skipped:
            if (r1.indexOf(47) >= 0) goto L_0x00bd;
     */
    /* JADX WARNING: Missing block: B:44:0x00a5, code skipped:
            r4 = new java.lang.StringBuilder();
            r4.append(r11.serviceInfo.packageName);
            r4.append("/");
            r4.append(r1);
            r1 = r4.toString();
     */
    /* JADX WARNING: Missing block: B:45:0x00bd, code skipped:
            if (r1 != null) goto L_0x00c0;
     */
    /* JADX WARNING: Missing block: B:46:0x00c0, code skipped:
            r0 = android.content.ComponentName.unflattenFromString(r1);
     */
    /* JADX WARNING: Missing block: B:47:0x00c4, code skipped:
            return r0;
     */
    private static android.content.ComponentName getSettingsComponentName(android.content.pm.PackageManager r10, android.content.pm.ResolveInfo r11) {
        /*
        r0 = 0;
        if (r11 == 0) goto L_0x00c5;
    L_0x0003:
        r1 = r11.serviceInfo;
        if (r1 == 0) goto L_0x00c5;
    L_0x0007:
        r1 = r11.serviceInfo;
        r1 = r1.metaData;
        if (r1 != 0) goto L_0x000f;
    L_0x000d:
        goto L_0x00c5;
    L_0x000f:
        r1 = 0;
        r2 = 0;
        r3 = r0;
        r4 = r11.serviceInfo;	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r5 = "android.service.dream";
        r4 = r4.loadXmlMetaData(r10, r5);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r2 = r4;
        if (r2 != 0) goto L_0x002b;
    L_0x001d:
        r4 = "DreamBackend";
        r5 = "No android.service.dream meta-data";
        android.util.Log.w(r4, r5);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        if (r2 == 0) goto L_0x002a;
    L_0x0027:
        r2.close();
    L_0x002a:
        return r0;
    L_0x002b:
        r4 = r11.serviceInfo;	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r4 = r4.applicationInfo;	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r4 = r10.getResourcesForApplication(r4);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r5 = android.util.Xml.asAttributeSet(r2);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
    L_0x0037:
        r6 = r2.next();	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r7 = r6;
        r8 = 1;
        if (r6 == r8) goto L_0x0043;
    L_0x003f:
        r6 = 2;
        if (r7 == r6) goto L_0x0043;
    L_0x0042:
        goto L_0x0037;
    L_0x0043:
        r6 = r2.getName();	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r8 = "dream";
        r8 = r8.equals(r6);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        if (r8 != 0) goto L_0x005d;
    L_0x004f:
        r8 = "DreamBackend";
        r9 = "Meta-data does not start with dream tag";
        android.util.Log.w(r8, r9);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        if (r2 == 0) goto L_0x005c;
    L_0x0059:
        r2.close();
    L_0x005c:
        return r0;
    L_0x005d:
        r8 = com.android.internal.R.styleable.Dream;	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r8 = r4.obtainAttributes(r5, r8);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r9 = 0;
        r9 = r8.getString(r9);	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        r1 = r9;
        r8.recycle();	 Catch:{ NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, NameNotFoundException | IOException | XmlPullParserException -> 0x0079, all -> 0x0072 }
        if (r2 == 0) goto L_0x007e;
    L_0x006e:
        r2.close();
        goto L_0x007e;
    L_0x0072:
        r0 = move-exception;
        if (r2 == 0) goto L_0x0078;
    L_0x0075:
        r2.close();
    L_0x0078:
        throw r0;
    L_0x0079:
        r4 = move-exception;
        r3 = r4;
        if (r2 == 0) goto L_0x007e;
    L_0x007d:
        goto L_0x006e;
    L_0x007e:
        if (r3 == 0) goto L_0x009b;
    L_0x0080:
        r4 = "DreamBackend";
        r5 = new java.lang.StringBuilder;
        r5.<init>();
        r6 = "Error parsing : ";
        r5.append(r6);
        r6 = r11.serviceInfo;
        r6 = r6.packageName;
        r5.append(r6);
        r5 = r5.toString();
        android.util.Log.w(r4, r5, r3);
        return r0;
    L_0x009b:
        if (r1 == 0) goto L_0x00bd;
    L_0x009d:
        r4 = 47;
        r4 = r1.indexOf(r4);
        if (r4 >= 0) goto L_0x00bd;
    L_0x00a5:
        r4 = new java.lang.StringBuilder;
        r4.<init>();
        r5 = r11.serviceInfo;
        r5 = r5.packageName;
        r4.append(r5);
        r5 = "/";
        r4.append(r5);
        r4.append(r1);
        r1 = r4.toString();
    L_0x00bd:
        if (r1 != 0) goto L_0x00c0;
    L_0x00bf:
        goto L_0x00c4;
    L_0x00c0:
        r0 = android.content.ComponentName.unflattenFromString(r1);
    L_0x00c4:
        return r0;
    L_0x00c5:
        return r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.dream.DreamBackend.getSettingsComponentName(android.content.pm.PackageManager, android.content.pm.ResolveInfo):android.content.ComponentName");
    }

    private static void logd(String msg, Object... args) {
    }
}
