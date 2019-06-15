package com.android.settingslib.inputmethod;

import android.app.ActivityManager;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class InputMethodSettingValuesWrapper {
    private static final String TAG = InputMethodSettingValuesWrapper.class.getSimpleName();
    private static volatile InputMethodSettingValuesWrapper sInstance;
    private final HashSet<InputMethodInfo> mAsciiCapableEnabledImis = new HashSet();
    private final InputMethodManager mImm;
    private final ArrayList<InputMethodInfo> mMethodList = new ArrayList();
    private final HashMap<String, InputMethodInfo> mMethodMap = new HashMap();
    private final InputMethodSettings mSettings;

    public static InputMethodSettingValuesWrapper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TAG) {
                if (sInstance == null) {
                    sInstance = new InputMethodSettingValuesWrapper(context);
                }
            }
        }
        return sInstance;
    }

    private static int getDefaultCurrentUserId() {
        try {
            return ActivityManager.getService().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
            return 0;
        }
    }

    private InputMethodSettingValuesWrapper(Context context) {
        this.mSettings = new InputMethodSettings(context.getResources(), context.getContentResolver(), this.mMethodMap, this.mMethodList, getDefaultCurrentUserId(), false);
        this.mImm = (InputMethodManager) context.getSystemService("input_method");
        refreshAllInputMethodAndSubtypes();
    }

    public void refreshAllInputMethodAndSubtypes() {
        synchronized (this.mMethodMap) {
            this.mMethodList.clear();
            this.mMethodMap.clear();
            List<InputMethodInfo> imms = this.mImm.getInputMethodList();
            this.mMethodList.addAll(imms);
            for (InputMethodInfo imi : imms) {
                this.mMethodMap.put(imi.getId(), imi);
            }
            updateAsciiCapableEnabledImis();
        }
    }

    private void updateAsciiCapableEnabledImis() {
        synchronized (this.mMethodMap) {
            this.mAsciiCapableEnabledImis.clear();
            for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
                int subtypeCount = imi.getSubtypeCount();
                for (int i = 0; i < subtypeCount; i++) {
                    InputMethodSubtype subtype = imi.getSubtypeAt(i);
                    if ("keyboard".equalsIgnoreCase(subtype.getMode()) && subtype.isAsciiCapable()) {
                        this.mAsciiCapableEnabledImis.add(imi);
                        break;
                    }
                }
            }
        }
    }

    public List<InputMethodInfo> getInputMethodList() {
        ArrayList arrayList;
        synchronized (this.mMethodMap) {
            arrayList = this.mMethodList;
        }
        return arrayList;
    }

    /* JADX WARNING: Missing block: B:9:0x0019, code skipped:
            r1 = getEnabledValidSystemNonAuxAsciiCapableImeCount(r6);
     */
    /* JADX WARNING: Missing block: B:10:0x001e, code skipped:
            if (r1 > 1) goto L_0x0031;
     */
    /* JADX WARNING: Missing block: B:11:0x0020, code skipped:
            if (r1 != 1) goto L_0x0024;
     */
    /* JADX WARNING: Missing block: B:12:0x0022, code skipped:
            if (r0 == false) goto L_0x0031;
     */
    /* JADX WARNING: Missing block: B:14:0x0028, code skipped:
            if (com.android.internal.inputmethod.InputMethodUtils.isSystemIme(r5) == false) goto L_0x0031;
     */
    /* JADX WARNING: Missing block: B:16:0x002e, code skipped:
            if (isValidSystemNonAuxAsciiCapableIme(r5, r6) == false) goto L_0x0031;
     */
    /* JADX WARNING: Missing block: B:17:0x0031, code skipped:
            r3 = false;
     */
    /* JADX WARNING: Missing block: B:18:0x0032, code skipped:
            return r3;
     */
    public boolean isAlwaysCheckedIme(android.view.inputmethod.InputMethodInfo r5, android.content.Context r6) {
        /*
        r4 = this;
        r0 = r4.isEnabledImi(r5);
        r1 = r4.mMethodMap;
        monitor-enter(r1);
        r2 = r4.mSettings;	 Catch:{ all -> 0x0033 }
        r2 = r2.getEnabledInputMethodListLocked();	 Catch:{ all -> 0x0033 }
        r2 = r2.size();	 Catch:{ all -> 0x0033 }
        r3 = 1;
        if (r2 > r3) goto L_0x0018;
    L_0x0014:
        if (r0 == 0) goto L_0x0018;
    L_0x0016:
        monitor-exit(r1);	 Catch:{ all -> 0x0033 }
        return r3;
    L_0x0018:
        monitor-exit(r1);	 Catch:{ all -> 0x0033 }
        r1 = r4.getEnabledValidSystemNonAuxAsciiCapableImeCount(r6);
        if (r1 > r3) goto L_0x0031;
    L_0x0020:
        if (r1 != r3) goto L_0x0024;
    L_0x0022:
        if (r0 == 0) goto L_0x0031;
    L_0x0024:
        r2 = com.android.internal.inputmethod.InputMethodUtils.isSystemIme(r5);
        if (r2 == 0) goto L_0x0031;
    L_0x002a:
        r2 = r4.isValidSystemNonAuxAsciiCapableIme(r5, r6);
        if (r2 == 0) goto L_0x0031;
    L_0x0030:
        goto L_0x0032;
    L_0x0031:
        r3 = 0;
    L_0x0032:
        return r3;
    L_0x0033:
        r2 = move-exception;
        monitor-exit(r1);	 Catch:{ all -> 0x0033 }
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settingslib.inputmethod.InputMethodSettingValuesWrapper.isAlwaysCheckedIme(android.view.inputmethod.InputMethodInfo, android.content.Context):boolean");
    }

    private int getEnabledValidSystemNonAuxAsciiCapableImeCount(Context context) {
        int count = 0;
        synchronized (this.mMethodMap) {
            List<InputMethodInfo> enabledImis = this.mSettings.getEnabledInputMethodListLocked();
        }
        for (InputMethodInfo imi : enabledImis) {
            if (isValidSystemNonAuxAsciiCapableIme(imi, context)) {
                count++;
            }
        }
        if (count == 0) {
            Log.w(TAG, "No \"enabledValidSystemNonAuxAsciiCapableIme\"s found.");
        }
        return count;
    }

    public boolean isEnabledImi(InputMethodInfo imi) {
        synchronized (this.mMethodMap) {
            List<InputMethodInfo> enabledImis = this.mSettings.getEnabledInputMethodListLocked();
        }
        for (InputMethodInfo tempImi : enabledImis) {
            if (tempImi.getId().equals(imi.getId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isValidSystemNonAuxAsciiCapableIme(InputMethodInfo imi, Context context) {
        if (imi.isAuxiliaryIme()) {
            return false;
        }
        if (InputMethodUtils.isSystemImeThatHasSubtypeOf(imi, context, true, context.getResources().getConfiguration().locale, false, InputMethodUtils.SUBTYPE_MODE_ANY)) {
            return true;
        }
        if (!this.mAsciiCapableEnabledImis.isEmpty()) {
            return this.mAsciiCapableEnabledImis.contains(imi);
        }
        Log.w(TAG, "ascii capable subtype enabled imi not found. Fall back to English Keyboard subtype.");
        return InputMethodUtils.containsSubtypeOf(imi, Locale.ENGLISH, false, "keyboard");
    }
}
