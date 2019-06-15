package androidx.slice.compat;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.os.Process;
import android.os.RemoteException;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.util.ArraySet;
import android.support.v4.util.Preconditions;
import android.util.Log;
import androidx.slice.Slice;
import androidx.slice.SliceProvider;
import androidx.slice.SliceSpec;
import androidx.slice.core.SliceHints;
import androidx.versionedparcelable.ParcelUtils;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestrictTo({Scope.LIBRARY})
public class SliceProviderCompat {
    private static final String ALL_FILES = "slice_data_all_slice_files";
    public static final String ARG_SUPPORTS_VERSIONED_PARCELABLE = "supports_versioned_parcelable";
    private static final String DATA_PREFIX = "slice_data_";
    public static final String EXTRA_BIND_URI = "slice_uri";
    public static final String EXTRA_INTENT = "slice_intent";
    public static final String EXTRA_PID = "pid";
    public static final String EXTRA_PKG = "pkg";
    public static final String EXTRA_PROVIDER_PKG = "provider_pkg";
    public static final String EXTRA_RESULT = "result";
    public static final String EXTRA_SLICE = "slice";
    public static final String EXTRA_SLICE_DESCENDANTS = "slice_descendants";
    public static final String EXTRA_SUPPORTED_SPECS = "specs";
    public static final String EXTRA_SUPPORTED_SPECS_REVS = "revs";
    public static final String EXTRA_UID = "uid";
    public static final String METHOD_CHECK_PERMISSION = "check_perms";
    public static final String METHOD_GET_DESCENDANTS = "get_descendants";
    public static final String METHOD_GET_PINNED_SPECS = "get_specs";
    public static final String METHOD_GRANT_PERMISSION = "grant_perms";
    public static final String METHOD_MAP_INTENT = "map_slice";
    public static final String METHOD_MAP_ONLY_INTENT = "map_only";
    public static final String METHOD_PIN = "pin_slice";
    public static final String METHOD_REVOKE_PERMISSION = "revoke_perms";
    public static final String METHOD_SLICE = "bind_slice";
    public static final String METHOD_UNPIN = "unpin_slice";
    public static final String PERMS_PREFIX = "slice_perms_";
    private static final long SLICE_BIND_ANR = 2000;
    private static final String TAG = "SliceProviderCompat";
    private final Runnable mAnr = new Runnable() {
        public void run() {
            Process.sendSignal(Process.myPid(), 3);
            String str = SliceProviderCompat.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Timed out while handling slice callback ");
            stringBuilder.append(SliceProviderCompat.this.mCallback);
            Log.wtf(str, stringBuilder.toString());
        }
    };
    private String mCallback;
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private CompatPermissionManager mPermissionManager;
    private CompatPinnedList mPinnedList;
    private final SliceProvider mProvider;

    private static class ProviderHolder implements AutoCloseable {
        private final ContentProviderClient mProvider;

        ProviderHolder(ContentProviderClient provider) {
            this.mProvider = provider;
        }

        public void close() {
            if (this.mProvider != null) {
                if (VERSION.SDK_INT >= 24) {
                    this.mProvider.close();
                } else {
                    this.mProvider.release();
                }
            }
        }
    }

    public SliceProviderCompat(SliceProvider provider, CompatPermissionManager permissionManager, Context context) {
        this.mProvider = provider;
        this.mContext = context;
        String prefsFile = new StringBuilder();
        prefsFile.append(DATA_PREFIX);
        prefsFile.append(getClass().getName());
        prefsFile = prefsFile.toString();
        SharedPreferences allFiles = this.mContext.getSharedPreferences(ALL_FILES, 0);
        Collection files = allFiles.getStringSet(ALL_FILES, Collections.emptySet());
        if (!files.contains(prefsFile)) {
            ArraySet files2 = new ArraySet(files);
            files2.add(prefsFile);
            allFiles.edit().putStringSet(ALL_FILES, files2).commit();
        }
        this.mPinnedList = new CompatPinnedList(this.mContext, prefsFile);
        this.mPermissionManager = permissionManager;
    }

    private Context getContext() {
        return this.mContext;
    }

    public String getCallingPackage() {
        return this.mProvider.getCallingPackage();
    }

    public Bundle call(String method, String arg, Bundle extras) {
        Parcelable parcelable = null;
        Bundle b;
        Uri uri;
        Bundle b2;
        if (method.equals(METHOD_SLICE)) {
            Slice s = handleBindSlice((Uri) extras.getParcelable(EXTRA_BIND_URI), getSpecs(extras), getCallingPackage());
            b = new Bundle();
            String str;
            if (ARG_SUPPORTS_VERSIONED_PARCELABLE.equals(arg)) {
                str = "slice";
                if (s != null) {
                    parcelable = ParcelUtils.toParcelable(s);
                }
                b.putParcelable(str, parcelable);
            } else {
                str = "slice";
                if (s != null) {
                    parcelable = s.toBundle();
                }
                b.putParcelable(str, parcelable);
            }
            return b;
        } else if (method.equals(METHOD_MAP_INTENT)) {
            Uri uri2 = this.mProvider.onMapIntentToUri((Intent) extras.getParcelable(EXTRA_INTENT));
            Bundle b3 = new Bundle();
            if (uri2 != null) {
                Slice s2 = handleBindSlice(uri2, getSpecs(extras), getCallingPackage());
                String str2;
                if (ARG_SUPPORTS_VERSIONED_PARCELABLE.equals(arg)) {
                    str2 = "slice";
                    if (s2 != null) {
                        parcelable = ParcelUtils.toParcelable(s2);
                    }
                    b3.putParcelable(str2, parcelable);
                } else {
                    str2 = "slice";
                    if (s2 != null) {
                        parcelable = s2.toBundle();
                    }
                    b3.putParcelable(str2, parcelable);
                }
            } else {
                b3.putParcelable("slice", null);
            }
            return b3;
        } else if (method.equals(METHOD_MAP_ONLY_INTENT)) {
            Uri uri3 = this.mProvider.onMapIntentToUri((Intent) extras.getParcelable(EXTRA_INTENT));
            Bundle b4 = new Bundle();
            b4.putParcelable("slice", uri3);
            return b4;
        } else if (method.equals(METHOD_PIN)) {
            uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
            Set<SliceSpec> specs = getSpecs(extras);
            if (this.mPinnedList.addPin(uri, extras.getString("pkg"), specs)) {
                handleSlicePinned(uri);
            }
            return null;
        } else if (method.equals(METHOD_UNPIN)) {
            uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
            if (this.mPinnedList.removePin(uri, extras.getString("pkg"))) {
                handleSliceUnpinned(uri);
            }
            return null;
        } else if (method.equals(METHOD_GET_PINNED_SPECS)) {
            uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
            b2 = new Bundle();
            addSpecs(b2, this.mPinnedList.getSpecs(uri));
            return b2;
        } else if (method.equals(METHOD_GET_DESCENDANTS)) {
            uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
            b2 = new Bundle();
            b2.putParcelableArrayList(EXTRA_SLICE_DESCENDANTS, new ArrayList(handleGetDescendants(uri)));
            return b2;
        } else if (method.equals(METHOD_CHECK_PERMISSION)) {
            uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
            String pkg = extras.getString("pkg");
            int pid = extras.getInt(EXTRA_PID);
            int uid = extras.getInt("uid");
            b = new Bundle();
            b.putInt(EXTRA_RESULT, this.mPermissionManager.checkSlicePermission(uri, pid, uid));
            return b;
        } else {
            String toPkg;
            if (method.equals(METHOD_GRANT_PERMISSION)) {
                uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
                toPkg = extras.getString("pkg");
                if (Binder.getCallingUid() == Process.myUid()) {
                    this.mPermissionManager.grantSlicePermission(uri, toPkg);
                } else {
                    throw new SecurityException("Only the owning process can manage slice permissions");
                }
            } else if (method.equals(METHOD_REVOKE_PERMISSION)) {
                uri = (Uri) extras.getParcelable(EXTRA_BIND_URI);
                toPkg = extras.getString("pkg");
                if (Binder.getCallingUid() == Process.myUid()) {
                    this.mPermissionManager.revokeSlicePermission(uri, toPkg);
                } else {
                    throw new SecurityException("Only the owning process can manage slice permissions");
                }
            }
            return null;
        }
    }

    private Collection<Uri> handleGetDescendants(Uri uri) {
        this.mCallback = "onGetSliceDescendants";
        return this.mProvider.onGetSliceDescendants(uri);
    }

    private void handleSlicePinned(Uri sliceUri) {
        this.mCallback = "onSlicePinned";
        this.mHandler.postDelayed(this.mAnr, 2000);
        try {
            this.mProvider.onSlicePinned(sliceUri);
            this.mProvider.handleSlicePinned(sliceUri);
        } finally {
            this.mHandler.removeCallbacks(this.mAnr);
        }
    }

    private void handleSliceUnpinned(Uri sliceUri) {
        this.mCallback = "onSliceUnpinned";
        this.mHandler.postDelayed(this.mAnr, 2000);
        try {
            this.mProvider.onSliceUnpinned(sliceUri);
            this.mProvider.handleSliceUnpinned(sliceUri);
        } finally {
            this.mHandler.removeCallbacks(this.mAnr);
        }
    }

    private Slice handleBindSlice(Uri sliceUri, Set<SliceSpec> specs, String callingPkg) {
        String pkg = callingPkg != null ? callingPkg : getContext().getPackageManager().getNameForUid(Binder.getCallingUid());
        if (this.mPermissionManager.checkSlicePermission(sliceUri, Binder.getCallingPid(), Binder.getCallingUid()) == 0) {
            return onBindSliceStrict(sliceUri, specs);
        }
        SliceProvider sliceProvider = this.mProvider;
        return SliceProvider.createPermissionSlice(getContext(), sliceUri, pkg);
    }

    private Slice onBindSliceStrict(Uri sliceUri, Set<SliceSpec> specs) {
        ThreadPolicy oldPolicy = StrictMode.getThreadPolicy();
        this.mCallback = "onBindSlice";
        this.mHandler.postDelayed(this.mAnr, 2000);
        try {
            StrictMode.setThreadPolicy(new Builder().detectAll().penaltyDeath().build());
            SliceProvider.setSpecs(specs);
            Slice onBindSlice = this.mProvider.onBindSlice(sliceUri);
            SliceProvider.setSpecs(null);
            this.mHandler.removeCallbacks(this.mAnr);
            StrictMode.setThreadPolicy(oldPolicy);
            return onBindSlice;
        } catch (Throwable th) {
            StrictMode.setThreadPolicy(oldPolicy);
        }
    }

    public static Slice bindSlice(Context context, Uri uri, Set<SliceSpec> supportedSpecs) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider != null) {
            try {
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_BIND_URI, uri);
                addSpecs(extras, supportedSpecs);
                Bundle res = holder.mProvider.call(METHOD_SLICE, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
                if (res == null) {
                    return null;
                }
                res.setClassLoader(SliceProviderCompat.class.getClassLoader());
                Parcelable parcel = res.getParcelable("slice");
                if (parcel == null) {
                    return null;
                }
                if (parcel instanceof Bundle) {
                    return new Slice((Bundle) parcel);
                }
                return (Slice) ParcelUtils.fromParcelable(parcel);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to bind slice", e);
                return null;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unknown URI ");
        stringBuilder.append(uri);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static void addSpecs(Bundle extras, Set<SliceSpec> supportedSpecs) {
        ArrayList<String> types = new ArrayList();
        ArrayList<Integer> revs = new ArrayList();
        for (SliceSpec spec : supportedSpecs) {
            types.add(spec.getType());
            revs.add(Integer.valueOf(spec.getRevision()));
        }
        extras.putStringArrayList(EXTRA_SUPPORTED_SPECS, types);
        extras.putIntegerArrayList(EXTRA_SUPPORTED_SPECS_REVS, revs);
    }

    public static Set<SliceSpec> getSpecs(Bundle extras) {
        ArraySet<SliceSpec> specs = new ArraySet();
        ArrayList<String> types = extras.getStringArrayList(EXTRA_SUPPORTED_SPECS);
        ArrayList<Integer> revs = extras.getIntegerArrayList(EXTRA_SUPPORTED_SPECS_REVS);
        if (!(types == null || revs == null)) {
            for (int i = 0; i < types.size(); i++) {
                specs.add(new SliceSpec((String) types.get(i), ((Integer) revs.get(i)).intValue()));
            }
        }
        return specs;
    }

    public static Slice bindSlice(Context context, Intent intent, Set<SliceSpec> supportedSpecs) {
        Preconditions.checkNotNull(intent, SliceDeepLinkSpringBoard.INTENT);
        boolean z = (intent.getComponent() == null && intent.getPackage() == null && intent.getData() == null) ? false : true;
        Preconditions.checkArgument(z, String.format("Slice intent must be explicit %s", new Object[]{intent}));
        ContentResolver resolver = context.getContentResolver();
        Uri intentData = intent.getData();
        if (intentData != null && "vnd.android.slice".equals(resolver.getType(intentData))) {
            return bindSlice(context, intentData, (Set) supportedSpecs);
        }
        Intent queryIntent = new Intent(intent);
        if (!queryIntent.hasCategory("android.app.slice.category.SLICE")) {
            queryIntent.addCategory("android.app.slice.category.SLICE");
        }
        List<ResolveInfo> providers = context.getPackageManager().queryIntentContentProviders(queryIntent, 0);
        if (providers == null || providers.isEmpty()) {
            ResolveInfo resolve = context.getPackageManager().resolveActivity(intent, 128);
            if (resolve == null || resolve.activityInfo == null || resolve.activityInfo.metaData == null || !resolve.activityInfo.metaData.containsKey(SliceHints.SLICE_METADATA_KEY)) {
                return null;
            }
            return bindSlice(context, Uri.parse(resolve.activityInfo.metaData.getString(SliceHints.SLICE_METADATA_KEY)), (Set) supportedSpecs);
        }
        Uri uri = new Uri.Builder().scheme("content").authority(((ResolveInfo) providers.get(0)).providerInfo.authority).build();
        ProviderHolder holder = acquireClient(resolver, uri);
        if (holder.mProvider != null) {
            try {
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_INTENT, intent);
                addSpecs(extras, supportedSpecs);
                Bundle res = holder.mProvider.call(METHOD_MAP_INTENT, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
                if (res == null) {
                    return null;
                }
                res.setClassLoader(SliceProviderCompat.class.getClassLoader());
                Parcelable parcel = res.getParcelable("slice");
                if (parcel == null) {
                    return null;
                }
                if (parcel instanceof Bundle) {
                    return new Slice((Bundle) parcel);
                }
                return (Slice) ParcelUtils.fromParcelable(parcel);
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to bind slice", e);
                return null;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unknown URI ");
        stringBuilder.append(uri);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static void pinSlice(Context context, Uri uri, Set<SliceSpec> supportedSpecs) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider != null) {
            try {
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_BIND_URI, uri);
                extras.putString("pkg", context.getPackageName());
                addSpecs(extras, supportedSpecs);
                holder.mProvider.call(METHOD_PIN, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to pin slice", e);
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unknown URI ");
        stringBuilder.append(uri);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static void unpinSlice(Context context, Uri uri, Set<SliceSpec> supportedSpecs) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider != null) {
            try {
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_BIND_URI, uri);
                extras.putString("pkg", context.getPackageName());
                addSpecs(extras, supportedSpecs);
                holder.mProvider.call(METHOD_UNPIN, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to unpin slice", e);
                return;
            }
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unknown URI ");
        stringBuilder.append(uri);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static Set<SliceSpec> getPinnedSpecs(Context context, Uri uri) {
        ProviderHolder holder = acquireClient(context.getContentResolver(), uri);
        if (holder.mProvider != null) {
            try {
                Bundle extras = new Bundle();
                extras.putParcelable(EXTRA_BIND_URI, uri);
                Bundle res = holder.mProvider.call(METHOD_GET_PINNED_SPECS, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
                if (res != null) {
                    return getSpecs(res);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "Unable to get pinned specs", e);
            }
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Unknown URI ");
        stringBuilder.append(uri);
        throw new IllegalArgumentException(stringBuilder.toString());
    }

    public static Uri mapIntentToUri(Context context, Intent intent) {
        ProviderHolder holder;
        Throwable th;
        Throwable th2;
        Preconditions.checkNotNull(intent, SliceDeepLinkSpringBoard.INTENT);
        boolean z = (intent.getComponent() == null && intent.getPackage() == null && intent.getData() == null) ? false : true;
        Preconditions.checkArgument(z, String.format("Slice intent must be explicit %s", new Object[]{intent}));
        ContentResolver resolver = context.getContentResolver();
        Uri intentData = intent.getData();
        if (intentData != null && "vnd.android.slice".equals(resolver.getType(intentData))) {
            return intentData;
        }
        Intent queryIntent = new Intent(intent);
        if (!queryIntent.hasCategory("android.app.slice.category.SLICE")) {
            queryIntent.addCategory("android.app.slice.category.SLICE");
        }
        List<ResolveInfo> providers = context.getPackageManager().queryIntentContentProviders(queryIntent, 0);
        if (providers == null || providers.isEmpty()) {
            ResolveInfo resolve = context.getPackageManager().resolveActivity(intent, 128);
            if (resolve == null || resolve.activityInfo == null || resolve.activityInfo.metaData == null || !resolve.activityInfo.metaData.containsKey(SliceHints.SLICE_METADATA_KEY)) {
                return null;
            }
            return Uri.parse(resolve.activityInfo.metaData.getString(SliceHints.SLICE_METADATA_KEY));
        }
        Uri uri = new Uri.Builder().scheme("content").authority(((ResolveInfo) providers.get(0)).providerInfo.authority).build();
        try {
            holder = acquireClient(resolver, uri);
            try {
                if (holder.mProvider != null) {
                    Bundle extras = new Bundle();
                    extras.putParcelable(EXTRA_INTENT, intent);
                    Bundle res = holder.mProvider.call(METHOD_MAP_ONLY_INTENT, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
                    if (res != null) {
                        Uri uri2 = (Uri) res.getParcelable("slice");
                        if (holder != null) {
                            holder.close();
                        }
                        return uri2;
                    }
                    if (holder != null) {
                        holder.close();
                    }
                    return null;
                }
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unknown URI ");
                stringBuilder.append(uri);
                throw new IllegalArgumentException(stringBuilder.toString());
            } catch (Throwable th22) {
                Throwable th3 = th22;
                th22 = th;
                th = th3;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to map slice", e);
        }
        if (holder != null) {
            if (th22 != null) {
                try {
                    holder.close();
                } catch (Throwable th4) {
                    th22.addSuppressed(th4);
                }
            } else {
                holder.close();
            }
        }
        throw th;
        throw th;
    }

    @NonNull
    public static Collection<Uri> getSliceDescendants(Context context, @NonNull Uri uri) {
        ProviderHolder holder;
        try {
            holder = acquireClient(context.getContentResolver(), uri);
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_BIND_URI, uri);
            Bundle res = holder.mProvider.call(METHOD_GET_DESCENDANTS, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
            if (res != null) {
                ArrayList parcelableArrayList = res.getParcelableArrayList(EXTRA_SLICE_DESCENDANTS);
                if (holder != null) {
                    holder.close();
                }
                return parcelableArrayList;
            }
            if (holder != null) {
                holder.close();
            }
            return Collections.emptyList();
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get slice descendants", e);
        } catch (Throwable th) {
            r2.addSuppressed(th);
        }
    }

    public static int checkSlicePermission(Context context, String packageName, Uri uri, int pid, int uid) {
        ProviderHolder holder;
        try {
            holder = acquireClient(context.getContentResolver(), uri);
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_BIND_URI, uri);
            extras.putString("pkg", packageName);
            extras.putInt(EXTRA_PID, pid);
            extras.putInt("uid", uid);
            Bundle res = holder.mProvider.call(METHOD_CHECK_PERMISSION, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
            if (res != null) {
                int i = res.getInt(EXTRA_RESULT);
                if (holder != null) {
                    holder.close();
                }
                return i;
            }
            if (holder != null) {
                holder.close();
            }
            return -1;
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to check slice permission", e);
        } catch (Throwable th) {
            r2.addSuppressed(th);
        }
    }

    public static void grantSlicePermission(Context context, String packageName, String toPackage, Uri uri) {
        ProviderHolder holder;
        try {
            holder = acquireClient(context.getContentResolver(), uri);
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_BIND_URI, uri);
            extras.putString(EXTRA_PROVIDER_PKG, packageName);
            extras.putString("pkg", toPackage);
            holder.mProvider.call(METHOD_GRANT_PERMISSION, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
            if (holder != null) {
                holder.close();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get slice descendants", e);
        } catch (Throwable th) {
            r2.addSuppressed(th);
        }
    }

    public static void revokeSlicePermission(Context context, String packageName, String toPackage, Uri uri) {
        ProviderHolder holder;
        try {
            holder = acquireClient(context.getContentResolver(), uri);
            Bundle extras = new Bundle();
            extras.putParcelable(EXTRA_BIND_URI, uri);
            extras.putString(EXTRA_PROVIDER_PKG, packageName);
            extras.putString("pkg", toPackage);
            holder.mProvider.call(METHOD_REVOKE_PERMISSION, ARG_SUPPORTS_VERSIONED_PARCELABLE, extras);
            if (holder != null) {
                holder.close();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to get slice descendants", e);
        } catch (Throwable th) {
            r2.addSuppressed(th);
        }
    }

    public static List<Uri> getPinnedSlices(Context context) {
        ArrayList<Uri> pinnedSlices = new ArrayList();
        for (String pref : context.getSharedPreferences(ALL_FILES, 0).getStringSet(ALL_FILES, Collections.emptySet())) {
            pinnedSlices.addAll(new CompatPinnedList(context, pref).getPinnedSlices());
        }
        return pinnedSlices;
    }

    private static ProviderHolder acquireClient(ContentResolver resolver, Uri uri) {
        ContentProviderClient provider = resolver.acquireContentProviderClient(uri);
        if (provider != null) {
            return new ProviderHolder(provider);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("No provider found for ");
        stringBuilder.append(uri);
        throw new IllegalArgumentException(stringBuilder.toString());
    }
}
