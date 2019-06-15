package com.oneplus.lib.preference;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.XmlResourceParser;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.util.Log;
import com.oneplus.lib.preference.GenericInflater.Parent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public class PreferenceManager {
    public static final String KEY_HAS_SET_DEFAULT_VALUES = "_has_set_default_values";
    public static final String METADATA_KEY_PREFERENCES = "com.oneplus.lib.preference";
    private static final int STORAGE_DEFAULT = 0;
    private static final int STORAGE_DEVICE_PROTECTED = 1;
    private static final String TAG = "PreferenceManager";
    private Activity mActivity;
    private List<OnActivityDestroyListener> mActivityDestroyListeners;
    private List<OnActivityResultListener> mActivityResultListeners;
    private List<OnActivityStopListener> mActivityStopListeners;
    private Context mContext;
    private Editor mEditor;
    private PreferenceFragment mFragment;
    private long mNextId = 0;
    private int mNextRequestCode;
    private boolean mNoCommit;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private PreferenceScreen mPreferenceScreen;
    private List<DialogInterface> mPreferencesScreens;
    private SharedPreferences mSharedPreferences;
    private int mSharedPreferencesMode;
    private String mSharedPreferencesName;
    private int mStorage = 0;

    public interface OnActivityDestroyListener {
        void onActivityDestroy();
    }

    public interface OnActivityResultListener {
        boolean onActivityResult(int i, int i2, Intent intent);
    }

    public interface OnActivityStopListener {
        void onActivityStop();
    }

    public interface OnPreferenceTreeClickListener {
        boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference);
    }

    public PreferenceManager(Activity activity, int firstRequestCode) {
        this.mActivity = activity;
        this.mNextRequestCode = firstRequestCode;
        init(activity);
    }

    PreferenceManager(Context context) {
        init(context);
    }

    private void init(Context context) {
        this.mContext = context;
        setSharedPreferencesName(getDefaultSharedPreferencesName(context));
    }

    /* Access modifiers changed, original: 0000 */
    public void setFragment(PreferenceFragment fragment) {
        this.mFragment = fragment;
    }

    /* Access modifiers changed, original: 0000 */
    public PreferenceFragment getFragment() {
        return this.mFragment;
    }

    private List<ResolveInfo> queryIntentActivities(Intent queryIntent) {
        return this.mContext.getPackageManager().queryIntentActivities(queryIntent, 128);
    }

    /* Access modifiers changed, original: 0000 */
    public PreferenceScreen inflateFromIntent(Intent queryIntent, PreferenceScreen rootPreferences) {
        List<ResolveInfo> activities = queryIntentActivities(queryIntent);
        HashSet<String> inflatedRes = new HashSet();
        for (int i = activities.size() - 1; i >= 0; i--) {
            ActivityInfo activityInfo = ((ResolveInfo) activities.get(i)).activityInfo;
            Bundle metaData = activityInfo.metaData;
            if (metaData != null && metaData.containsKey(METADATA_KEY_PREFERENCES)) {
                String uniqueResId = new StringBuilder();
                uniqueResId.append(activityInfo.packageName);
                uniqueResId.append(":");
                uniqueResId.append(activityInfo.metaData.getInt(METADATA_KEY_PREFERENCES));
                uniqueResId = uniqueResId.toString();
                if (!inflatedRes.contains(uniqueResId)) {
                    inflatedRes.add(uniqueResId);
                    try {
                        Context context = this.mContext.createPackageContext(activityInfo.packageName, 0);
                        PreferenceInflater inflater = new PreferenceInflater(context, this);
                        XmlResourceParser parser = activityInfo.loadXmlMetaData(context.getPackageManager(), METADATA_KEY_PREFERENCES);
                        rootPreferences = (PreferenceScreen) inflater.inflate((XmlPullParser) parser, (Parent) rootPreferences, true);
                        parser.close();
                    } catch (NameNotFoundException e) {
                        String str = TAG;
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("Could not create context for ");
                        stringBuilder.append(activityInfo.packageName);
                        stringBuilder.append(": ");
                        stringBuilder.append(Log.getStackTraceString(e));
                        Log.w(str, stringBuilder.toString());
                    }
                }
            }
        }
        rootPreferences.onAttachedToHierarchy(this);
        return rootPreferences;
    }

    public PreferenceScreen inflateFromResource(Context context, int resId, PreferenceScreen rootPreferences) {
        setNoCommit(true);
        rootPreferences = (PreferenceScreen) new PreferenceInflater(context, this).inflate(resId, (Parent) rootPreferences, true);
        rootPreferences.onAttachedToHierarchy(this);
        setNoCommit(false);
        return rootPreferences;
    }

    public PreferenceScreen createPreferenceScreen(Context context) {
        PreferenceScreen preferenceScreen = new PreferenceScreen(context, null);
        preferenceScreen.onAttachedToHierarchy(this);
        return preferenceScreen;
    }

    /* Access modifiers changed, original: 0000 */
    public long getNextId() {
        long j;
        synchronized (this) {
            j = this.mNextId;
            this.mNextId = 1 + j;
        }
        return j;
    }

    public String getSharedPreferencesName() {
        return this.mSharedPreferencesName;
    }

    public void setSharedPreferencesName(String sharedPreferencesName) {
        this.mSharedPreferencesName = sharedPreferencesName;
        this.mSharedPreferences = null;
    }

    public int getSharedPreferencesMode() {
        return this.mSharedPreferencesMode;
    }

    public void setSharedPreferencesMode(int sharedPreferencesMode) {
        this.mSharedPreferencesMode = sharedPreferencesMode;
        this.mSharedPreferences = null;
    }

    public void setStorageDefault() {
        if (VERSION.SDK_INT >= 24) {
            this.mStorage = 0;
            this.mSharedPreferences = null;
        }
    }

    public void setStorageDeviceProtected() {
        if (VERSION.SDK_INT >= 24) {
            this.mStorage = 1;
            this.mSharedPreferences = null;
        }
    }

    @Deprecated
    public void setStorageDeviceEncrypted() {
        setStorageDeviceProtected();
    }

    public boolean isStorageDefault() {
        boolean z = true;
        if (VERSION.SDK_INT < 24) {
            return true;
        }
        if (this.mStorage != 0) {
            z = false;
        }
        return z;
    }

    public boolean isStorageDeviceProtected() {
        boolean z = false;
        if (VERSION.SDK_INT < 24) {
            return false;
        }
        if (this.mStorage == 1) {
            z = true;
        }
        return z;
    }

    public SharedPreferences getSharedPreferences() {
        if (this.mSharedPreferences == null) {
            Context storageContext = this.mStorage != 1 ? this.mContext : VERSION.SDK_INT >= 24 ? this.mContext.createDeviceProtectedStorageContext() : null;
            this.mSharedPreferences = storageContext.getSharedPreferences(this.mSharedPreferencesName, this.mSharedPreferencesMode);
        }
        return this.mSharedPreferences;
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return context.getSharedPreferences(getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode());
    }

    private static String getDefaultSharedPreferencesName(Context context) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(context.getPackageName());
        stringBuilder.append("_preferences");
        return stringBuilder.toString();
    }

    private static int getDefaultSharedPreferencesMode() {
        return 0;
    }

    /* Access modifiers changed, original: 0000 */
    public PreferenceScreen getPreferenceScreen() {
        return this.mPreferenceScreen;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean setPreferences(PreferenceScreen preferenceScreen) {
        if (preferenceScreen == this.mPreferenceScreen) {
            return false;
        }
        this.mPreferenceScreen = preferenceScreen;
        return true;
    }

    public Preference findPreference(CharSequence key) {
        if (this.mPreferenceScreen == null) {
            return null;
        }
        return this.mPreferenceScreen.findPreference(key);
    }

    public static void setDefaultValues(Context context, int resId, boolean readAgain) {
        setDefaultValues(context, getDefaultSharedPreferencesName(context), getDefaultSharedPreferencesMode(), resId, readAgain);
    }

    public static void setDefaultValues(Context context, String sharedPreferencesName, int sharedPreferencesMode, int resId, boolean readAgain) {
        SharedPreferences defaultValueSp = context.getSharedPreferences("_has_set_default_values", 0);
        if (readAgain || !defaultValueSp.getBoolean("_has_set_default_values", false)) {
            PreferenceManager pm = new PreferenceManager(context);
            pm.setSharedPreferencesName(sharedPreferencesName);
            pm.setSharedPreferencesMode(sharedPreferencesMode);
            pm.inflateFromResource(context, resId, null);
            Editor editor = defaultValueSp.edit().putBoolean("_has_set_default_values", true);
            try {
                editor.apply();
            } catch (AbstractMethodError e) {
                editor.commit();
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public Editor getEditor() {
        if (!this.mNoCommit) {
            return getSharedPreferences().edit();
        }
        if (this.mEditor == null) {
            this.mEditor = getSharedPreferences().edit();
        }
        return this.mEditor;
    }

    /* Access modifiers changed, original: 0000 */
    public boolean shouldCommit() {
        return this.mNoCommit ^ 1;
    }

    private void setNoCommit(boolean noCommit) {
        if (!(noCommit || this.mEditor == null)) {
            try {
                this.mEditor.apply();
            } catch (AbstractMethodError e) {
                this.mEditor.commit();
            }
        }
        this.mNoCommit = noCommit;
    }

    /* Access modifiers changed, original: 0000 */
    public Activity getActivity() {
        return this.mActivity;
    }

    /* Access modifiers changed, original: 0000 */
    public Context getContext() {
        return this.mContext;
    }

    /* Access modifiers changed, original: 0000 */
    public void registerOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners == null) {
                this.mActivityResultListeners = new ArrayList();
            }
            if (!this.mActivityResultListeners.contains(listener)) {
                this.mActivityResultListeners.add(listener);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void unregisterOnActivityResultListener(OnActivityResultListener listener) {
        synchronized (this) {
            if (this.mActivityResultListeners != null) {
                this.mActivityResultListeners.remove(listener);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:8:0x000f, code skipped:
            r1 = r0.size();
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:9:0x0014, code skipped:
            if (r2 >= r1) goto L_0x0026;
     */
    /* JADX WARNING: Missing block: B:11:0x0020, code skipped:
            if (((com.oneplus.lib.preference.PreferenceManager.OnActivityResultListener) r0.get(r2)).onActivityResult(r5, r6, r7) == false) goto L_0x0023;
     */
    /* JADX WARNING: Missing block: B:12:0x0023, code skipped:
            r2 = r2 + 1;
     */
    /* JADX WARNING: Missing block: B:13:0x0026, code skipped:
            return;
     */
    public void dispatchActivityResult(int r5, int r6, android.content.Intent r7) {
        /*
        r4 = this;
        monitor-enter(r4);
        r0 = r4.mActivityResultListeners;	 Catch:{ all -> 0x0027 }
        if (r0 != 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r4);	 Catch:{ all -> 0x0027 }
        return;
    L_0x0007:
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x0027 }
        r1 = r4.mActivityResultListeners;	 Catch:{ all -> 0x0027 }
        r0.<init>(r1);	 Catch:{ all -> 0x0027 }
        monitor-exit(r4);	 Catch:{ all -> 0x0027 }
        r1 = r0.size();
        r2 = 0;
    L_0x0014:
        if (r2 >= r1) goto L_0x0026;
    L_0x0016:
        r3 = r0.get(r2);
        r3 = (com.oneplus.lib.preference.PreferenceManager.OnActivityResultListener) r3;
        r3 = r3.onActivityResult(r5, r6, r7);
        if (r3 == 0) goto L_0x0023;
    L_0x0022:
        goto L_0x0026;
    L_0x0023:
        r2 = r2 + 1;
        goto L_0x0014;
    L_0x0026:
        return;
    L_0x0027:
        r0 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0027 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.preference.PreferenceManager.dispatchActivityResult(int, int, android.content.Intent):void");
    }

    public void registerOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (this.mActivityStopListeners == null) {
                this.mActivityStopListeners = new ArrayList();
            }
            if (!this.mActivityStopListeners.contains(listener)) {
                this.mActivityStopListeners.add(listener);
            }
        }
    }

    public void unregisterOnActivityStopListener(OnActivityStopListener listener) {
        synchronized (this) {
            if (this.mActivityStopListeners != null) {
                this.mActivityStopListeners.remove(listener);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    /* JADX WARNING: Missing block: B:8:0x000f, code skipped:
            r1 = r0.size();
            r2 = 0;
     */
    /* JADX WARNING: Missing block: B:9:0x0014, code skipped:
            if (r2 >= r1) goto L_0x0022;
     */
    /* JADX WARNING: Missing block: B:10:0x0016, code skipped:
            ((com.oneplus.lib.preference.PreferenceManager.OnActivityStopListener) r0.get(r2)).onActivityStop();
            r2 = r2 + 1;
     */
    /* JADX WARNING: Missing block: B:11:0x0022, code skipped:
            return;
     */
    public void dispatchActivityStop() {
        /*
        r4 = this;
        monitor-enter(r4);
        r0 = r4.mActivityStopListeners;	 Catch:{ all -> 0x0023 }
        if (r0 != 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r4);	 Catch:{ all -> 0x0023 }
        return;
    L_0x0007:
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x0023 }
        r1 = r4.mActivityStopListeners;	 Catch:{ all -> 0x0023 }
        r0.<init>(r1);	 Catch:{ all -> 0x0023 }
        monitor-exit(r4);	 Catch:{ all -> 0x0023 }
        r1 = r0.size();
        r2 = 0;
    L_0x0014:
        if (r2 >= r1) goto L_0x0022;
    L_0x0016:
        r3 = r0.get(r2);
        r3 = (com.oneplus.lib.preference.PreferenceManager.OnActivityStopListener) r3;
        r3.onActivityStop();
        r2 = r2 + 1;
        goto L_0x0014;
    L_0x0022:
        return;
    L_0x0023:
        r0 = move-exception;
        monitor-exit(r4);	 Catch:{ all -> 0x0023 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.preference.PreferenceManager.dispatchActivityStop():void");
    }

    /* Access modifiers changed, original: 0000 */
    public void registerOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners == null) {
                this.mActivityDestroyListeners = new ArrayList();
            }
            if (!this.mActivityDestroyListeners.contains(listener)) {
                this.mActivityDestroyListeners.add(listener);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                this.mActivityDestroyListeners.remove(listener);
            }
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchActivityDestroy() {
        List<OnActivityDestroyListener> list = null;
        synchronized (this) {
            if (this.mActivityDestroyListeners != null) {
                list = new ArrayList(this.mActivityDestroyListeners);
            }
        }
        if (list != null) {
            int N = list.size();
            for (int i = 0; i < N; i++) {
                ((OnActivityDestroyListener) list.get(i)).onActivityDestroy();
            }
        }
        dismissAllScreens();
    }

    /* Access modifiers changed, original: 0000 */
    public int getNextRequestCode() {
        int i;
        synchronized (this) {
            i = this.mNextRequestCode;
            this.mNextRequestCode = i + 1;
        }
        return i;
    }

    /* Access modifiers changed, original: 0000 */
    public void addPreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            if (this.mPreferencesScreens == null) {
                this.mPreferencesScreens = new ArrayList();
            }
            this.mPreferencesScreens.add(screen);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void removePreferencesScreen(DialogInterface screen) {
        synchronized (this) {
            if (this.mPreferencesScreens == null) {
                return;
            }
            this.mPreferencesScreens.remove(screen);
        }
    }

    /* Access modifiers changed, original: 0000 */
    public void dispatchNewIntent(Intent intent) {
        dismissAllScreens();
    }

    /* JADX WARNING: Missing block: B:8:0x0014, code skipped:
            r1 = r0.size() - 1;
     */
    /* JADX WARNING: Missing block: B:9:0x001a, code skipped:
            if (r1 < 0) goto L_0x0028;
     */
    /* JADX WARNING: Missing block: B:10:0x001c, code skipped:
            ((android.content.DialogInterface) r0.get(r1)).dismiss();
            r1 = r1 - 1;
     */
    /* JADX WARNING: Missing block: B:11:0x0028, code skipped:
            return;
     */
    private void dismissAllScreens() {
        /*
        r3 = this;
        monitor-enter(r3);
        r0 = r3.mPreferencesScreens;	 Catch:{ all -> 0x0029 }
        if (r0 != 0) goto L_0x0007;
    L_0x0005:
        monitor-exit(r3);	 Catch:{ all -> 0x0029 }
        return;
    L_0x0007:
        r0 = new java.util.ArrayList;	 Catch:{ all -> 0x0029 }
        r1 = r3.mPreferencesScreens;	 Catch:{ all -> 0x0029 }
        r0.<init>(r1);	 Catch:{ all -> 0x0029 }
        r1 = r3.mPreferencesScreens;	 Catch:{ all -> 0x0029 }
        r1.clear();	 Catch:{ all -> 0x0029 }
        monitor-exit(r3);	 Catch:{ all -> 0x0029 }
        r1 = r0.size();
        r1 = r1 + -1;
    L_0x001a:
        if (r1 < 0) goto L_0x0028;
    L_0x001c:
        r2 = r0.get(r1);
        r2 = (android.content.DialogInterface) r2;
        r2.dismiss();
        r1 = r1 + -1;
        goto L_0x001a;
    L_0x0028:
        return;
    L_0x0029:
        r0 = move-exception;
        monitor-exit(r3);	 Catch:{ all -> 0x0029 }
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.lib.preference.PreferenceManager.dismissAllScreens():void");
    }

    /* Access modifiers changed, original: 0000 */
    public void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        this.mOnPreferenceTreeClickListener = listener;
    }

    /* Access modifiers changed, original: 0000 */
    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return this.mOnPreferenceTreeClickListener;
    }
}
