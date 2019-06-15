package com.android.settings.applications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.util.IconDrawableFactory;
import android.util.Log;
import android.util.Pair;
import com.android.settings.R;
import com.android.settings.widget.EntityHeaderController;
import com.android.settingslib.applications.AppUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class DirectoryAccessDetails extends AppInfoBase {
    private static final String TAG = "DirectoryAccessDetails";
    private boolean mCreated;

    private static class ExternalVolume {
        final List<Pair<String, Boolean>> children = new ArrayList();
        boolean granted;
        final String uuid;

        ExternalVolume(String uuid) {
            this.uuid = uuid;
        }

        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ExternalVolume: [uuid=");
            stringBuilder.append(this.uuid);
            stringBuilder.append(", granted=");
            stringBuilder.append(this.granted);
            stringBuilder.append(", children=");
            stringBuilder.append(this.children);
            stringBuilder.append("]");
            return stringBuilder.toString();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (this.mCreated) {
            Log.w(TAG, "onActivityCreated(): ignoring duplicate call");
            return;
        }
        this.mCreated = true;
        if (this.mPackageInfo == null) {
            Log.w(TAG, "onActivityCreated(): no package info");
            return;
        }
        Activity activity = getActivity();
        getPreferenceScreen().addPreference(EntityHeaderController.newInstance(activity, this, null).setRecyclerView(getListView(), getLifecycle()).setIcon(IconDrawableFactory.newInstance(getPrefContext()).getBadgedIcon(this.mPackageInfo.applicationInfo)).setLabel(this.mPackageInfo.applicationInfo.loadLabel(this.mPm)).setIsInstantApp(AppUtils.isInstant(this.mPackageInfo.applicationInfo)).setPackageName(this.mPackageName).setUid(this.mPackageInfo.applicationInfo.uid).setHasAppInfoLink(false).setButtonActions(0, 0).done(activity, getPrefContext()));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.directory_access_details);
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:80:0x02e3  */
    public boolean refreshUi() {
        /*
        r23 = this;
        r15 = r23;
        r14 = r23.getPrefContext();
        r13 = r23.getPreferenceScreen();
        r13.removeAll();
        r0 = new java.util.HashMap;
        r0.<init>();
        r12 = r0;
        r0 = new android.net.Uri$Builder;
        r0.<init>();
        r1 = "content";
        r0 = r0.scheme(r1);
        r1 = "com.android.documentsui.scopedAccess";
        r0 = r0.authority(r1);
        r1 = "permissions";
        r0 = r0.appendPath(r1);
        r1 = "*";
        r0 = r0.appendPath(r1);
        r17 = r0.build();
        r1 = r14.getContentResolver();
        r3 = android.os.storage.StorageVolume.ScopedAccessProviderContract.TABLE_PERMISSIONS_COLUMNS;
        r0 = 1;
        r5 = new java.lang.String[r0];
        r2 = r15.mPackageName;
        r9 = 0;
        r5[r9] = r2;
        r4 = 0;
        r6 = 0;
        r2 = r17;
        r10 = r1.query(r2, r3, r4, r5, r6);
        r11 = 0;
        if (r10 != 0) goto L_0x007c;
    L_0x004d:
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.<init>();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = "Didn't get cursor for ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = r15.mPackageName;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2 = r2.toString();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        android.util.Log.w(r1, r2);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        if (r10 == 0) goto L_0x006b;
    L_0x0068:
        r10.close();
    L_0x006b:
        return r0;
    L_0x006c:
        r0 = move-exception;
        r1 = r0;
        r6 = r12;
        r7 = r13;
        r21 = r14;
        goto L_0x02e1;
    L_0x0074:
        r0 = move-exception;
        r11 = r0;
        r6 = r12;
        r7 = r13;
        r21 = r14;
        goto L_0x02de;
    L_0x007c:
        r1 = r10.getCount();	 Catch:{ Throwable -> 0x02d8, all -> 0x02d1 }
        r16 = r1;
        if (r16 != 0) goto L_0x00a3;
    L_0x0084:
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.<init>();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = "No permissions for ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = r15.mPackageName;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2 = r2.toString();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        android.util.Log.w(r1, r2);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        if (r10 == 0) goto L_0x00a2;
    L_0x009f:
        r10.close();
    L_0x00a2:
        return r0;
    L_0x00a3:
        r1 = r10.moveToNext();	 Catch:{ Throwable -> 0x02d8, all -> 0x02d1 }
        if (r1 == 0) goto L_0x018b;
    L_0x00a9:
        r1 = r10.getString(r9);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r8 = r1;
        r1 = r10.getString(r0);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r7 = r1;
        r1 = 2;
        r1 = r10.getString(r1);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r6 = r1;
        r1 = 3;
        r1 = r10.getInt(r1);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        if (r1 != r0) goto L_0x00c2;
    L_0x00c0:
        r1 = r0;
        goto L_0x00c3;
    L_0x00c2:
        r1 = r9;
    L_0x00c3:
        r5 = r1;
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.<init>();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = "Pkg:";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r8);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = " uuid: ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r7);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = " dir: ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r6);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = " granted:";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r5);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2 = r2.toString();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        android.util.Log.v(r1, r2);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r1 = r15.mPackageName;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r1 = r1.equals(r8);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        if (r1 != 0) goto L_0x012c;
    L_0x00fa:
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.<init>();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = "Ignoring ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r7);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = "/";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r6);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = " due to package mismatch: expected ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = r15.mPackageName;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r3 = ", got ";
        r2.append(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.append(r8);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2 = r2.toString();	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        android.util.Log.w(r1, r2);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        goto L_0x00a3;
    L_0x012c:
        if (r7 != 0) goto L_0x0156;
    L_0x012e:
        if (r6 != 0) goto L_0x0138;
    L_0x0130:
        r1 = "DirectoryAccessDetails";
        r2 = "Ignoring permission on primary storage root";
        android.util.Log.wtf(r1, r2);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        goto L_0x0187;
    L_0x0138:
        r18 = 0;
        r19 = 0;
        r1 = r15;
        r2 = r14;
        r3 = r6;
        r4 = r17;
        r20 = r5;
        r5 = r18;
        r21 = r6;
        r9 = r7;
        r7 = r20;
        r18 = r8;
        r8 = r19;
        r1 = r1.newPreference(r2, r3, r4, r5, r6, r7, r8);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r13.addPreference(r1);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        goto L_0x0187;
    L_0x0156:
        r20 = r5;
        r21 = r6;
        r9 = r7;
        r18 = r8;
        r1 = r12.get(r9);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r1 = (com.android.settings.applications.DirectoryAccessDetails.ExternalVolume) r1;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        if (r1 != 0) goto L_0x016e;
    L_0x0165:
        r2 = new com.android.settings.applications.DirectoryAccessDetails$ExternalVolume;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r2.<init>(r9);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r1 = r2;
        r12.put(r9, r1);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
    L_0x016e:
        r2 = r21;
        if (r2 != 0) goto L_0x0177;
    L_0x0172:
        r3 = r20;
        r1.granted = r3;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        goto L_0x0187;
    L_0x0177:
        r3 = r20;
        r4 = r1.children;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r5 = new android.util.Pair;	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r6 = java.lang.Boolean.valueOf(r3);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r5.<init>(r2, r6);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r4.add(r5);	 Catch:{ Throwable -> 0x0074, all -> 0x006c }
        r9 = 0;
        goto L_0x00a3;
    L_0x018b:
        if (r10 == 0) goto L_0x0190;
    L_0x018d:
        r10.close();
    L_0x0190:
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "external volumes: ";
        r2.append(r3);
        r2.append(r12);
        r2 = r2.toString();
        android.util.Log.v(r1, r2);
        r1 = r12.isEmpty();
        if (r1 == 0) goto L_0x01ad;
    L_0x01ac:
        return r0;
    L_0x01ad:
        r1 = android.os.storage.StorageManager.class;
        r1 = r14.getSystemService(r1);
        r11 = r1;
        r11 = (android.os.storage.StorageManager) r11;
        r10 = r11.getVolumes();
        r1 = r10.isEmpty();
        if (r1 == 0) goto L_0x01c8;
    L_0x01c0:
        r1 = "DirectoryAccessDetails";
        r2 = "StorageManager returned no secondary volumes";
        android.util.Log.w(r1, r2);
        return r0;
    L_0x01c8:
        r1 = new java.util.HashMap;
        r2 = r10.size();
        r1.<init>(r2);
        r9 = r1;
        r1 = r10.iterator();
    L_0x01d6:
        r2 = r1.hasNext();
        if (r2 == 0) goto L_0x0212;
    L_0x01dc:
        r2 = r1.next();
        r2 = (android.os.storage.VolumeInfo) r2;
        r3 = r2.getFsUuid();
        if (r3 != 0) goto L_0x01e9;
    L_0x01e8:
        goto L_0x01d6;
    L_0x01e9:
        r4 = r11.getBestVolumeDescription(r2);
        if (r4 != 0) goto L_0x020e;
    L_0x01ef:
        r5 = "DirectoryAccessDetails";
        r6 = new java.lang.StringBuilder;
        r6.<init>();
        r7 = "No description for ";
        r6.append(r7);
        r6.append(r2);
        r7 = "; using uuid instead: ";
        r6.append(r7);
        r6.append(r3);
        r6 = r6.toString();
        android.util.Log.w(r5, r6);
        r4 = r3;
    L_0x020e:
        r9.put(r3, r4);
        goto L_0x01d6;
    L_0x0212:
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "UUID -> name mapping: ";
        r2.append(r3);
        r2.append(r9);
        r2 = r2.toString();
        android.util.Log.v(r1, r2);
        r1 = r12.values();
        r7 = r1.iterator();
    L_0x0230:
        r1 = r7.hasNext();
        if (r1 == 0) goto L_0x02c8;
    L_0x0236:
        r1 = r7.next();
        r6 = r1;
        r6 = (com.android.settings.applications.DirectoryAccessDetails.ExternalVolume) r6;
        r1 = r6.uuid;
        r1 = r9.get(r1);
        r18 = r1;
        r18 = (java.lang.String) r18;
        if (r18 != 0) goto L_0x0262;
    L_0x0249:
        r1 = "DirectoryAccessDetails";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "Ignoring entry for invalid UUID: ";
        r2.append(r3);
        r3 = r6.uuid;
        r2.append(r3);
        r2 = r2.toString();
        android.util.Log.w(r1, r2);
        goto L_0x0230;
    L_0x0262:
        r1 = new android.support.v7.preference.PreferenceCategory;
        r1.<init>(r14);
        r5 = r1;
        r13.addPreference(r5);
        r8 = new java.util.HashSet;
        r1 = r6.children;
        r1 = r1.size();
        r8.<init>(r1);
        r4 = r6.uuid;
        r16 = 0;
        r3 = r6.granted;
        r1 = r15;
        r2 = r14;
        r19 = r3;
        r3 = r18;
        r20 = r4;
        r4 = r17;
        r0 = r5;
        r5 = r20;
        r22 = r13;
        r13 = r6;
        r6 = r16;
        r20 = r7;
        r7 = r19;
        r1 = r1.newPreference(r2, r3, r4, r5, r6, r7, r8);
        r0.addPreference(r1);
        r1 = r13.children;
        r2 = new com.android.settings.applications.-$$Lambda$DirectoryAccessDetails$K0N0BhiTAIxLxuaXU9qwR-rLnAY;
        r3 = r9;
        r9 = r2;
        r4 = r10;
        r10 = r15;
        r5 = r11;
        r11 = r14;
        r6 = r12;
        r12 = r18;
        r19 = r13;
        r7 = r22;
        r13 = r17;
        r21 = r14;
        r14 = r19;
        r15 = r0;
        r16 = r8;
        r9.<init>(r10, r11, r12, r13, r14, r15, r16);
        r1.forEach(r2);
        r15 = r23;
        r9 = r3;
        r10 = r4;
        r11 = r5;
        r12 = r6;
        r13 = r7;
        r7 = r20;
        r14 = r21;
        r0 = 1;
        goto L_0x0230;
    L_0x02c8:
        r3 = r9;
        r4 = r10;
        r5 = r11;
        r6 = r12;
        r7 = r13;
        r21 = r14;
        r0 = 1;
        return r0;
    L_0x02d1:
        r0 = move-exception;
        r6 = r12;
        r7 = r13;
        r21 = r14;
        r1 = r0;
        goto L_0x02e1;
    L_0x02d8:
        r0 = move-exception;
        r6 = r12;
        r7 = r13;
        r21 = r14;
        r11 = r0;
    L_0x02de:
        throw r11;	 Catch:{ all -> 0x02df }
    L_0x02df:
        r0 = move-exception;
        r1 = r0;
    L_0x02e1:
        if (r10 == 0) goto L_0x02f2;
    L_0x02e3:
        if (r11 == 0) goto L_0x02ef;
    L_0x02e5:
        r10.close();	 Catch:{ Throwable -> 0x02e9 }
        goto L_0x02f2;
    L_0x02e9:
        r0 = move-exception;
        r2 = r0;
        r11.addSuppressed(r2);
        goto L_0x02f2;
    L_0x02ef:
        r10.close();
    L_0x02f2:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.applications.DirectoryAccessDetails.refreshUi():boolean");
    }

    public static /* synthetic */ void lambda$refreshUi$0(DirectoryAccessDetails directoryAccessDetails, Context context, String volumeName, Uri providerUri, ExternalVolume volume, PreferenceCategory category, Set children, Pair pair) {
        Pair pair2 = pair;
        String dir = pair2.first;
        SwitchPreference childPref = directoryAccessDetails.newPreference(context, context.getResources().getString(R.string.directory_on_volume, new Object[]{volumeName, dir}), providerUri, volume.uuid, dir, ((Boolean) pair2.second).booleanValue(), null);
        category.addPreference(childPref);
        children.add(childPref);
    }

    private SwitchPreference newPreference(Context context, String title, Uri providerUri, String uuid, String dir, boolean granted, Set<SwitchPreference> children) {
        Context context2 = context;
        SwitchPreference pref = new SwitchPreference(context2);
        pref.setKey(String.format("%s:%s", new Object[]{uuid, dir}));
        pref.setTitle((CharSequence) title);
        pref.setChecked(granted);
        pref.setOnPreferenceChangeListener(new -$$Lambda$DirectoryAccessDetails$lMkU9x3CDhpq6XQS106C_-FREgc(this, context2, providerUri, uuid, dir, children));
        return pref;
    }

    public static /* synthetic */ boolean lambda$newPreference$1(DirectoryAccessDetails directoryAccessDetails, Context context, Uri providerUri, String uuid, String dir, Set children, Preference unused, Object value) {
        if (Boolean.class.isInstance(value)) {
            boolean newValue = ((Boolean) value).booleanValue();
            directoryAccessDetails.resetDoNotAskAgain(context, newValue, providerUri, uuid, dir);
            if (children != null) {
                boolean newChildValue = newValue ^ 1;
                for (SwitchPreference child : children) {
                    child.setVisible(newChildValue);
                }
            }
            return true;
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Invalid value from switch: ");
        stringBuilder.append(value);
        Log.wtf(str, stringBuilder.toString());
        return true;
    }

    private void resetDoNotAskAgain(Context context, boolean newValue, Uri providerUri, String uuid, String directory) {
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Asking ");
        stringBuilder.append(providerUri);
        stringBuilder.append(" to update ");
        stringBuilder.append(uuid);
        stringBuilder.append("/");
        stringBuilder.append(directory);
        stringBuilder.append(" to ");
        stringBuilder.append(newValue);
        Log.d(str, stringBuilder.toString());
        ContentValues values = new ContentValues(1);
        values.put("granted", Boolean.valueOf(newValue));
        int updated = context.getContentResolver().update(providerUri, values, 0, new String[]{this.mPackageName, uuid, directory});
        String str2 = TAG;
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append("Updated ");
        stringBuilder2.append(updated);
        stringBuilder2.append(" entries for ");
        stringBuilder2.append(uuid);
        stringBuilder2.append("/");
        stringBuilder2.append(directory);
        Log.d(str2, stringBuilder2.toString());
    }

    /* Access modifiers changed, original: protected */
    public AlertDialog createDialog(int id, int errorCode) {
        return null;
    }

    public int getMetricsCategory() {
        return 1284;
    }
}
