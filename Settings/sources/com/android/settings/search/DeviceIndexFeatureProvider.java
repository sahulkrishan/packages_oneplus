package com.android.settings.search;

import android.app.job.JobInfo.Builder;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ServiceInfo;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.slices.SettingsSliceProvider;
import com.android.settings.slices.SliceDeepLinkSpringBoard;
import java.util.List;
import java.util.Locale;

public interface DeviceIndexFeatureProvider {
    public static final String INDEX_LANGUAGE = "settings:language";
    public static final String INDEX_VERSION = "settings:index_version";
    public static final Locale LANGUAGE = Locale.getDefault();
    public static final String TAG = "DeviceIndex";
    public static final String VERSION = Build.FINGERPRINT;

    void clearIndex(Context context);

    void index(Context context, CharSequence charSequence, Uri uri, Uri uri2, List<String> list);

    boolean isIndexingEnabled();

    void updateIndex(Context context, boolean force) {
        if (!isIndexingEnabled()) {
            Log.i(TAG, "Skipping: device index is not enabled");
        } else if (Utils.isDeviceProvisioned(context)) {
            ComponentName jobComponent = new ComponentName(context.getPackageName(), DeviceIndexUpdateJobService.class.getName());
            try {
                int callerUid = Binder.getCallingUid();
                ServiceInfo si = context.getPackageManager().getServiceInfo(jobComponent, 786432);
                String str;
                StringBuilder stringBuilder;
                if (si == null) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Skipping: No such service ");
                    stringBuilder.append(jobComponent);
                    Log.w(str, stringBuilder.toString());
                } else if (si.applicationInfo.uid != callerUid) {
                    str = TAG;
                    stringBuilder = new StringBuilder();
                    stringBuilder.append("Skipping: Uid cannot schedule DeviceIndexUpdate: ");
                    stringBuilder.append(callerUid);
                    Log.w(str, stringBuilder.toString());
                } else if (force || !skipIndex(context)) {
                    setIndexState(context);
                    ((JobScheduler) context.getSystemService(JobScheduler.class)).schedule(new Builder(context.getResources().getInteger(R.integer.device_index_update), jobComponent).setPersisted(true).setMinimumLatency(1000).setOverrideDeadline(1).build());
                } else {
                    Log.i(TAG, "Skipping: already indexed.");
                }
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Skipping: error finding DeviceIndexUpdateJobService from packageManager");
            }
        } else {
            Log.w(TAG, "Skipping: device is not provisioned");
        }
    }

    static Uri createDeepLink(String s) {
        return new Uri.Builder().scheme(SliceDeepLinkSpringBoard.SETTINGS).authority(SettingsSliceProvider.SLICE_AUTHORITY).appendQueryParameter(SliceDeepLinkSpringBoard.INTENT, s).build();
    }

    static boolean skipIndex(Context context) {
        return TextUtils.equals(Secure.getString(context.getContentResolver(), INDEX_LANGUAGE), LANGUAGE.toString()) && TextUtils.equals(Secure.getString(context.getContentResolver(), INDEX_VERSION), VERSION);
    }

    static void setIndexState(Context context) {
        Secure.putString(context.getContentResolver(), INDEX_VERSION, VERSION);
        Secure.putString(context.getContentResolver(), INDEX_LANGUAGE, LANGUAGE.toString());
    }
}
