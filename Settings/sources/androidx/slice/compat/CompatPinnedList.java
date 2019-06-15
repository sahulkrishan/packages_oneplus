package androidx.slice.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.annotation.VisibleForTesting;
import android.support.v4.util.ArraySet;
import android.support.v4.util.ObjectsCompat;
import android.text.TextUtils;
import androidx.slice.SliceSpec;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@RestrictTo({Scope.LIBRARY})
public class CompatPinnedList {
    private static final long BOOT_THRESHOLD = 2000;
    private static final String LAST_BOOT = "last_boot";
    private static final String PIN_PREFIX = "pinned_";
    private static final String SPEC_NAME_PREFIX = "spec_names_";
    private static final String SPEC_REV_PREFIX = "spec_revs_";
    private final Context mContext;
    private final String mPrefsName;

    public CompatPinnedList(Context context, String prefsName) {
        this.mContext = context;
        this.mPrefsName = prefsName;
    }

    private SharedPreferences getPrefs() {
        SharedPreferences prefs = this.mContext.getSharedPreferences(this.mPrefsName, 0);
        long lastBootTime = prefs.getLong(LAST_BOOT, 0);
        long currentBootTime = getBootTime();
        if (Math.abs(lastBootTime - currentBootTime) > 2000) {
            prefs.edit().clear().putLong(LAST_BOOT, currentBootTime).commit();
        }
        return prefs;
    }

    public List<Uri> getPinnedSlices() {
        List<Uri> pinned = new ArrayList();
        for (String key : getPrefs().getAll().keySet()) {
            if (key.startsWith(PIN_PREFIX)) {
                Uri uri = Uri.parse(key.substring(PIN_PREFIX.length()));
                if (!getPins(uri).isEmpty()) {
                    pinned.add(uri);
                }
            }
        }
        return pinned;
    }

    private Set<String> getPins(Uri uri) {
        SharedPreferences prefs = getPrefs();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PIN_PREFIX);
        stringBuilder.append(uri.toString());
        return prefs.getStringSet(stringBuilder.toString(), new ArraySet());
    }

    public synchronized ArraySet<SliceSpec> getSpecs(Uri uri) {
        ArraySet<SliceSpec> specs = new ArraySet();
        SharedPreferences prefs = getPrefs();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SPEC_NAME_PREFIX);
        stringBuilder.append(uri.toString());
        String specNamesStr = prefs.getString(stringBuilder.toString(), null);
        StringBuilder stringBuilder2 = new StringBuilder();
        stringBuilder2.append(SPEC_REV_PREFIX);
        stringBuilder2.append(uri.toString());
        String specRevsStr = prefs.getString(stringBuilder2.toString(), null);
        if (!TextUtils.isEmpty(specNamesStr)) {
            if (!TextUtils.isEmpty(specRevsStr)) {
                String[] specNames = specNamesStr.split(",", -1);
                String[] specRevs = specRevsStr.split(",", -1);
                if (specNames.length != specRevs.length) {
                    return new ArraySet();
                }
                for (int i = 0; i < specNames.length; i++) {
                    specs.add(new SliceSpec(specNames[i], Integer.parseInt(specRevs[i])));
                }
                return specs;
            }
        }
        return new ArraySet();
    }

    private void setPins(Uri uri, Set<String> pins) {
        Editor edit = getPrefs().edit();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(PIN_PREFIX);
        stringBuilder.append(uri.toString());
        edit.putStringSet(stringBuilder.toString(), pins).commit();
    }

    private void setSpecs(Uri uri, ArraySet<SliceSpec> specs) {
        String[] specNames = new String[specs.size()];
        String[] specRevs = new String[specs.size()];
        for (int i = 0; i < specs.size(); i++) {
            specNames[i] = ((SliceSpec) specs.valueAt(i)).getType();
            specRevs[i] = String.valueOf(((SliceSpec) specs.valueAt(i)).getRevision());
        }
        Editor edit = getPrefs().edit();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(SPEC_NAME_PREFIX);
        stringBuilder.append(uri.toString());
        edit = edit.putString(stringBuilder.toString(), TextUtils.join(",", specNames));
        stringBuilder = new StringBuilder();
        stringBuilder.append(SPEC_REV_PREFIX);
        stringBuilder.append(uri.toString());
        edit.putString(stringBuilder.toString(), TextUtils.join(",", specRevs)).commit();
    }

    /* Access modifiers changed, original: protected */
    @VisibleForTesting
    public long getBootTime() {
        return System.currentTimeMillis() - SystemClock.elapsedRealtime();
    }

    public synchronized boolean addPin(Uri uri, String pkg, Set<SliceSpec> specs) {
        boolean wasNotPinned;
        Set<String> pins = getPins(uri);
        wasNotPinned = pins.isEmpty();
        pins.add(pkg);
        setPins(uri, pins);
        if (wasNotPinned) {
            setSpecs(uri, new ArraySet((Collection) specs));
        } else {
            setSpecs(uri, mergeSpecs(getSpecs(uri), specs));
        }
        return wasNotPinned;
    }

    /* JADX WARNING: Missing block: B:11:0x0022, code skipped:
            return r2;
     */
    /* JADX WARNING: Missing block: B:13:0x0024, code skipped:
            return false;
     */
    public synchronized boolean removePin(android.net.Uri r4, java.lang.String r5) {
        /*
        r3 = this;
        monitor-enter(r3);
        r0 = r3.getPins(r4);	 Catch:{ all -> 0x0025 }
        r1 = r0.isEmpty();	 Catch:{ all -> 0x0025 }
        r2 = 0;
        if (r1 != 0) goto L_0x0023;
    L_0x000c:
        r1 = r0.contains(r5);	 Catch:{ all -> 0x0025 }
        if (r1 != 0) goto L_0x0013;
    L_0x0012:
        goto L_0x0023;
    L_0x0013:
        r0.remove(r5);	 Catch:{ all -> 0x0025 }
        r3.setPins(r4, r0);	 Catch:{ all -> 0x0025 }
        r1 = r0.size();	 Catch:{ all -> 0x0025 }
        if (r1 != 0) goto L_0x0021;
    L_0x001f:
        r2 = 1;
    L_0x0021:
        monitor-exit(r3);
        return r2;
    L_0x0023:
        monitor-exit(r3);
        return r2;
    L_0x0025:
        r4 = move-exception;
        monitor-exit(r3);
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slice.compat.CompatPinnedList.removePin(android.net.Uri, java.lang.String):boolean");
    }

    private static ArraySet<SliceSpec> mergeSpecs(ArraySet<SliceSpec> specs, Set<SliceSpec> supportedSpecs) {
        int i = 0;
        while (i < specs.size()) {
            int i2;
            SliceSpec s = (SliceSpec) specs.valueAt(i);
            SliceSpec other = findSpec(supportedSpecs, s.getType());
            if (other == null) {
                i2 = i - 1;
                specs.removeAt(i);
            } else if (other.getRevision() < s.getRevision()) {
                i2 = i - 1;
                specs.removeAt(i);
                specs.add(other);
            } else {
                i2 = i;
            }
            i = i2 + 1;
        }
        return specs;
    }

    private static SliceSpec findSpec(Set<SliceSpec> specs, String type) {
        for (SliceSpec spec : specs) {
            if (ObjectsCompat.equals(spec.getType(), type)) {
                return spec;
            }
        }
        return null;
    }
}
