package androidx.slice.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.ArraySet;
import android.text.TextUtils;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@RestrictTo({Scope.LIBRARY})
public class CompatPermissionManager {
    public static final String ALL_SUFFIX = "_all";
    private static final String TAG = "CompatPermissionManager";
    private final String[] mAutoGrantPermissions;
    private final Context mContext;
    private final int mMyUid;
    private final String mPrefsName;

    public static class PermissionState {
        private final String mKey;
        private final ArraySet<String[]> mPaths = new ArraySet();

        PermissionState(Set<String> grant, String key, boolean hasAllPermissions) {
            if (hasAllPermissions) {
                this.mPaths.add(new String[0]);
            } else {
                for (String g : grant) {
                    this.mPaths.add(decodeSegments(g));
                }
            }
            this.mKey = key;
        }

        public boolean hasAllPermissions() {
            return hasAccess(Collections.emptyList());
        }

        public String getKey() {
            return this.mKey;
        }

        public Set<String> toPersistable() {
            ArraySet<String> ret = new ArraySet();
            Iterator it = this.mPaths.iterator();
            while (it.hasNext()) {
                ret.add(encodeSegments((String[]) it.next()));
            }
            return ret;
        }

        public boolean hasAccess(List<String> path) {
            String[] inPath = (String[]) path.toArray(new String[path.size()]);
            Iterator it = this.mPaths.iterator();
            while (it.hasNext()) {
                if (isPathPrefixMatch((String[]) it.next(), inPath)) {
                    return true;
                }
            }
            return false;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean addPath(List<String> path) {
            String[] pathSegs = (String[]) path.toArray(new String[path.size()]);
            for (int i = this.mPaths.size() - 1; i >= 0; i--) {
                String[] existing = (String[]) this.mPaths.valueAt(i);
                if (isPathPrefixMatch(existing, pathSegs)) {
                    return false;
                }
                if (isPathPrefixMatch(pathSegs, existing)) {
                    this.mPaths.removeAt(i);
                }
            }
            this.mPaths.add(pathSegs);
            return true;
        }

        /* Access modifiers changed, original: 0000 */
        public boolean removePath(List<String> path) {
            boolean changed = false;
            String[] pathSegs = (String[]) path.toArray(new String[path.size()]);
            for (int i = this.mPaths.size() - 1; i >= 0; i--) {
                if (isPathPrefixMatch(pathSegs, (String[]) this.mPaths.valueAt(i))) {
                    changed = true;
                    this.mPaths.removeAt(i);
                }
            }
            return changed;
        }

        private boolean isPathPrefixMatch(String[] prefix, String[] path) {
            int prefixSize = prefix.length;
            if (path.length < prefixSize) {
                return false;
            }
            for (int i = 0; i < prefixSize; i++) {
                if (!Objects.equals(path[i], prefix[i])) {
                    return false;
                }
            }
            return true;
        }

        private String encodeSegments(String[] s) {
            String[] out = new String[s.length];
            for (int i = 0; i < s.length; i++) {
                out[i] = Uri.encode(s[i]);
            }
            return TextUtils.join("/", out);
        }

        private String[] decodeSegments(String s) {
            String[] sets = s.split("/", -1);
            for (int i = 0; i < sets.length; i++) {
                sets[i] = Uri.decode(sets[i]);
            }
            return sets;
        }
    }

    public CompatPermissionManager(Context context, String prefsName, int myUid, String[] autoGrantPermissions) {
        this.mContext = context;
        this.mPrefsName = prefsName;
        this.mMyUid = myUid;
        this.mAutoGrantPermissions = autoGrantPermissions;
    }

    private SharedPreferences getPrefs() {
        return this.mContext.getSharedPreferences(this.mPrefsName, 0);
    }

    public int checkSlicePermission(Uri uri, int pid, int uid) {
        if (uid == this.mMyUid) {
            return 0;
        }
        String[] pkgs = this.mContext.getPackageManager().getPackagesForUid(uid);
        for (String pkg : pkgs) {
            if (checkSlicePermission(uri, pkg) == 0) {
                return 0;
            }
        }
        for (String autoGrantPermission : this.mAutoGrantPermissions) {
            if (this.mContext.checkPermission(autoGrantPermission, pid, uid) == 0) {
                for (String pkg2 : pkgs) {
                    grantSlicePermission(uri, pkg2);
                }
                return 0;
            }
        }
        return this.mContext.checkUriPermission(uri, pid, uid, 2);
    }

    private int checkSlicePermission(Uri uri, String pkg) {
        return getPermissionState(pkg, uri.getAuthority()).hasAccess(uri.getPathSegments()) ? 0 : -1;
    }

    public void grantSlicePermission(Uri uri, String toPkg) {
        PermissionState state = getPermissionState(toPkg, uri.getAuthority());
        if (state.addPath(uri.getPathSegments())) {
            persist(state);
        }
    }

    public void revokeSlicePermission(Uri uri, String toPkg) {
        PermissionState state = getPermissionState(toPkg, uri.getAuthority());
        if (state.removePath(uri.getPathSegments())) {
            persist(state);
        }
    }

    private synchronized void persist(PermissionState state) {
        Editor putStringSet = getPrefs().edit().putStringSet(state.getKey(), state.toPersistable());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(state.getKey());
        stringBuilder.append(ALL_SUFFIX);
        putStringSet.putBoolean(stringBuilder.toString(), state.hasAllPermissions()).apply();
    }

    private PermissionState getPermissionState(String pkg, String authority) {
        String key = new StringBuilder();
        key.append(pkg);
        key.append("_");
        key.append(authority);
        key = key.toString();
        Set<String> grant = getPrefs().getStringSet(key, Collections.emptySet());
        boolean hasAllPermissions = getPrefs();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(key);
        stringBuilder.append(ALL_SUFFIX);
        return new PermissionState(grant, key, hasAllPermissions.getBoolean(stringBuilder.toString(), false));
    }
}
