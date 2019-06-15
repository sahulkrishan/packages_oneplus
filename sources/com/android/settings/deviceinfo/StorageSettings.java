package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.deviceinfo.PrivateStorageInfo;
import com.android.settingslib.deviceinfo.StorageManagerVolumeProvider;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageSettings extends SettingsPreferenceFragment implements Indexable {
    static final int[] COLOR_PRIVATE = new int[]{Color.parseColor("#ff26a69a"), Color.parseColor("#ffab47bc"), Color.parseColor("#fff2a600"), Color.parseColor("#ffec407a"), Color.parseColor("#ffc0ca33")};
    static final int COLOR_PUBLIC = Color.parseColor("#ff9e9e9e");
    private static final int METRICS_CATEGORY = 42;
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.storage_settings);
            data.key = "storage_settings";
            data.screenTitle = context.getString(R.string.storage_settings);
            data.keywords = context.getString(R.string.keywords_storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.internal_storage);
            data.key = "storage_settings_internal_storage";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
            for (VolumeInfo vol : storage.getVolumes()) {
                if (StorageSettings.isInteresting(vol)) {
                    data.title = storage.getBestVolumeDescription(vol);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("storage_settings_volume_");
                    stringBuilder.append(vol.id);
                    data.key = stringBuilder.toString();
                    data.screenTitle = context.getString(R.string.storage_settings);
                    result.add(data);
                }
            }
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.memory_size);
            data.key = "storage_settings_memory_size";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.memory_available);
            data.key = "storage_settings_memory_available";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.memory_apps_usage);
            data.key = "storage_settings_apps_space";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.memory_dcim_usage);
            data.key = "storage_settings_dcim_space";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.memory_music_usage);
            data.key = "storage_settings_music_space";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.memory_media_misc_usage);
            data.key = "storage_settings_misc_space";
            data.screenTitle = context.getString(R.string.storage_settings);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(R.string.storage_menu_free);
            data.key = "storage_settings_free_space";
            data.screenTitle = context.getString(R.string.storage_menu_free);
            data.intentAction = "android.os.storage.action.MANAGE_STORAGE";
            data.intentTargetPackage = context.getString(R.string.config_deletion_helper_package);
            data.intentTargetClass = context.getString(R.string.config_deletion_helper_class);
            result.add(data);
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = -$$Lambda$StorageSettings$pjFUgWj2HWW95DLVydfI8EgfTdg.INSTANCE;
    static final String TAG = "StorageSettings";
    private static final String TAG_DISK_INIT = "disk_init";
    private static final String TAG_VOLUME_UNMOUNTED = "volume_unmounted";
    private static long sTotalInternalStorage;
    private PreferenceCategory mExternalCategory;
    private boolean mHasLaunchedPrivateVolumeSettings = false;
    private PreferenceCategory mInternalCategory;
    private StorageSummaryPreference mInternalSummary;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (StorageSettings.isInteresting(vol)) {
                StorageSettings.this.refresh();
            }
        }

        public void onDiskDestroyed(DiskInfo disk) {
            StorageSettings.this.refresh();
        }
    };
    private StorageManager mStorageManager;

    public static class MountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final String mDescription;
        private final StorageManager mStorageManager = ((StorageManager) this.mContext.getSystemService(StorageManager.class));
        private final String mVolumeId;

        public MountTask(Context context, VolumeInfo volume) {
            this.mContext = context.getApplicationContext();
            this.mVolumeId = volume.getId();
            this.mDescription = this.mStorageManager.getBestVolumeDescription(volume);
        }

        /* Access modifiers changed, original: protected|varargs */
        public Exception doInBackground(Void... params) {
            try {
                this.mStorageManager.mount(this.mVolumeId);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Exception e) {
            if (e == null) {
                Toast.makeText(this.mContext, this.mContext.getString(R.string.storage_mount_success, new Object[]{this.mDescription}), 0).show();
                return;
            }
            String str = StorageSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to mount ");
            stringBuilder.append(this.mVolumeId);
            Log.e(str, stringBuilder.toString(), e);
            Toast.makeText(this.mContext, this.mContext.getString(R.string.storage_mount_failure, new Object[]{this.mDescription}), 0).show();
        }
    }

    public static class UnmountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final String mDescription;
        private final StorageManager mStorageManager = ((StorageManager) this.mContext.getSystemService(StorageManager.class));
        private final String mVolumeId;

        public UnmountTask(Context context, VolumeInfo volume) {
            this.mContext = context.getApplicationContext();
            this.mVolumeId = volume.getId();
            this.mDescription = this.mStorageManager.getBestVolumeDescription(volume);
        }

        /* Access modifiers changed, original: protected|varargs */
        public Exception doInBackground(Void... params) {
            try {
                this.mStorageManager.unmount(this.mVolumeId);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        /* Access modifiers changed, original: protected */
        public void onPostExecute(Exception e) {
            if (e == null) {
                Toast.makeText(this.mContext, this.mContext.getString(R.string.storage_unmount_success, new Object[]{this.mDescription}), 0).show();
                return;
            }
            String str = StorageSettings.TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to unmount ");
            stringBuilder.append(this.mVolumeId);
            Log.e(str, stringBuilder.toString(), e);
            Toast.makeText(this.mContext, this.mContext.getString(R.string.storage_unmount_failure, new Object[]{this.mDescription}), 0).show();
        }
    }

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mLoader;
        private final StorageManagerVolumeProvider mStorageManagerVolumeProvider;

        /* synthetic */ SummaryProvider(Context x0, SummaryLoader x1, AnonymousClass1 x2) {
            this(x0, x1);
        }

        private SummaryProvider(Context context, SummaryLoader loader) {
            this.mContext = context;
            this.mLoader = loader;
            this.mStorageManagerVolumeProvider = new StorageManagerVolumeProvider((StorageManager) this.mContext.getSystemService(StorageManager.class));
        }

        public void setListening(boolean listening) {
            if (listening) {
                updateSummary();
            }
        }

        private void updateSummary() {
            NumberFormat percentageFormat = NumberFormat.getPercentInstance();
            PrivateStorageInfo info = PrivateStorageInfo.getPrivateStorageInfo(this.mStorageManagerVolumeProvider);
            double privateUsedBytes = (double) (info.totalBytes - info.freeBytes);
            this.mLoader.setSummary(this, this.mContext.getString(R.string.storage_summary, new Object[]{percentageFormat.format(privateUsedBytes / ((double) info.totalBytes)), Formatter.formatFileSize(this.mContext, info.freeBytes)}));
        }
    }

    public static class DiskInitFragment extends InstrumentedDialogFragment {
        public int getMetricsCategory() {
            return 561;
        }

        public static void show(Fragment parent, int resId, String diskId) {
            Bundle args = new Bundle();
            args.putInt("android.intent.extra.TEXT", resId);
            args.putString("android.os.storage.extra.DISK_ID", diskId);
            DiskInitFragment dialog = new DiskInitFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), StorageSettings.TAG_DISK_INIT);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            StorageManager sm = (StorageManager) context.getSystemService(StorageManager.class);
            int resId = getArguments().getInt("android.intent.extra.TEXT");
            final String diskId = getArguments().getString("android.os.storage.extra.DISK_ID");
            DiskInfo disk = sm.findDiskById(diskId);
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(resId), new CharSequence[]{disk.getDescription()}));
            builder.setPositiveButton(R.string.storage_menu_set_up, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(context, StorageWizardInit.class);
                    intent.putExtra("android.os.storage.extra.DISK_ID", diskId);
                    DiskInitFragment.this.startActivity(intent);
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }
    }

    public static class VolumeUnmountedFragment extends InstrumentedDialogFragment {
        public static void show(Fragment parent, String volumeId) {
            Bundle args = new Bundle();
            args.putString("android.os.storage.extra.VOLUME_ID", volumeId);
            VolumeUnmountedFragment dialog = new VolumeUnmountedFragment();
            dialog.setArguments(args);
            dialog.setTargetFragment(parent, 0);
            dialog.show(parent.getFragmentManager(), StorageSettings.TAG_VOLUME_UNMOUNTED);
        }

        public int getMetricsCategory() {
            return 562;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final VolumeInfo vol = ((StorageManager) context.getSystemService(StorageManager.class)).findVolumeById(getArguments().getString("android.os.storage.extra.VOLUME_ID"));
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(R.string.storage_dialog_unmounted), new CharSequence[]{vol.getDisk().getDescription()}));
            builder.setPositiveButton(R.string.storage_menu_mount, new OnClickListener() {
                private boolean wasAdminSupportIntentShown(@NonNull String restriction) {
                    EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(VolumeUnmountedFragment.this.getActivity(), restriction, UserHandle.myUserId());
                    boolean hasBaseUserRestriction = RestrictedLockUtils.hasBaseUserRestriction(VolumeUnmountedFragment.this.getActivity(), restriction, UserHandle.myUserId());
                    if (admin == null || hasBaseUserRestriction) {
                        return false;
                    }
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(VolumeUnmountedFragment.this.getActivity(), admin);
                    return true;
                }

                public void onClick(DialogInterface dialog, int which) {
                    if (!wasAdminSupportIntentShown("no_physical_media")) {
                        if (vol.disk == null || !vol.disk.isUsb() || !wasAdminSupportIntentShown("no_usb_file_transfer")) {
                            new MountTask(context, vol).execute(new Void[0]);
                        }
                    }
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }
    }

    public int getMetricsCategory() {
        return 42;
    }

    public int getHelpResource() {
        return R.string.help_uri_storage;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mStorageManager = (StorageManager) getActivity().getSystemService(StorageManager.class);
        if (sTotalInternalStorage <= 0) {
            sTotalInternalStorage = this.mStorageManager.getPrimaryStorageSize();
        }
        addPreferencesFromResource(R.xml.device_info_storage);
        this.mInternalCategory = (PreferenceCategory) findPreference("storage_internal");
        this.mExternalCategory = (PreferenceCategory) findPreference("storage_external");
        this.mInternalSummary = new StorageSummaryPreference(getPrefContext());
        setHasOptionsMenu(true);
    }

    private static boolean isInteresting(VolumeInfo vol) {
        switch (vol.getType()) {
            case 0:
            case 1:
                return true;
            default:
                return false;
        }
    }

    private synchronized void refresh() {
        synchronized (this) {
            PrivateStorageInfo info;
            Context context = getPrefContext();
            getPreferenceScreen().removeAll();
            this.mInternalCategory.removeAll();
            this.mExternalCategory.removeAll();
            this.mInternalCategory.addPreference(this.mInternalSummary);
            StorageManagerVolumeProvider smvp = new StorageManagerVolumeProvider(this.mStorageManager);
            PrivateStorageInfo info2 = PrivateStorageInfo.getPrivateStorageInfo(smvp);
            long privateTotalBytes = info2.totalBytes;
            long privateUsedBytes = info2.totalBytes - info2.freeBytes;
            List<VolumeInfo> volumes = this.mStorageManager.getVolumes();
            Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
            int privateCount = 0;
            for (VolumeInfo vol : volumes) {
                StorageManagerVolumeProvider smvp2;
                VolumeInfo vol2;
                Preference storageVolumePreference;
                PreferenceCategory smvp3;
                if (vol2.getType() == 1) {
                    long volumeTotalBytes = PrivateStorageInfo.getTotalSize(vol2, sTotalInternalStorage);
                    int privateCount2 = privateCount + 1;
                    int color = COLOR_PRIVATE[privateCount % COLOR_PRIVATE.length];
                    Preference preference = storageVolumePreference;
                    smvp2 = smvp;
                    smvp3 = this.mInternalCategory;
                    storageVolumePreference = new StorageVolumePreference(context, vol2, color, volumeTotalBytes);
                    smvp3.addPreference(preference);
                    info = info2;
                    privateCount = privateCount2;
                } else {
                    smvp2 = smvp;
                    smvp = vol2;
                    if (smvp.getType() == 0) {
                        vol2 = smvp;
                        VolumeInfo vol3 = smvp;
                        info = info2;
                        smvp3 = this.mExternalCategory;
                        Preference preference2 = storageVolumePreference;
                        storageVolumePreference = new StorageVolumePreference(context, vol2, COLOR_PUBLIC, 0);
                        smvp3.addPreference(preference2);
                    } else {
                        info = info2;
                    }
                }
                smvp = smvp2;
                info2 = info;
            }
            info = info2;
            for (VolumeRecord rec : this.mStorageManager.getVolumeRecords()) {
                if (rec.getType() == 1 && this.mStorageManager.findVolumeByUuid(rec.getFsUuid()) == null) {
                    Drawable icon = context.getDrawable(R.drawable.ic_sim_sd);
                    icon.mutate();
                    icon.setTint(COLOR_PUBLIC);
                    Preference pref = new Preference(context);
                    pref.setKey(rec.getFsUuid());
                    pref.setTitle(rec.getNickname());
                    pref.setSummary(17039897);
                    pref.setIcon(icon);
                    this.mInternalCategory.addPreference(pref);
                }
            }
            for (DiskInfo disk : this.mStorageManager.getDisks()) {
                if (disk.volumeCount == 0 && disk.size > 0) {
                    Preference pref2 = new Preference(context);
                    pref2.setKey(disk.getId());
                    pref2.setTitle(disk.getDescription());
                    pref2.setSummary(17039903);
                    pref2.setIcon((int) R.drawable.ic_sim_sd);
                    this.mExternalCategory.addPreference(pref2);
                }
            }
            BytesResult result = Formatter.formatBytes(getResources(), privateUsedBytes, 0);
            this.mInternalSummary.setTitle(TextUtils.expandTemplate(getText(R.string.storage_size_large), new CharSequence[]{result.value, result.units}));
            this.mInternalSummary.setSummary((CharSequence) getString(R.string.storage_volume_used_total, new Object[]{Formatter.formatFileSize(context, privateTotalBytes)}));
            if (this.mInternalCategory.getPreferenceCount() > 0) {
                getPreferenceScreen().addPreference(this.mInternalCategory);
            }
            if (this.mExternalCategory.getPreferenceCount() > 0) {
                getPreferenceScreen().addPreference(this.mExternalCategory);
            }
            if (this.mInternalCategory.getPreferenceCount() == 2 && this.mExternalCategory.getPreferenceCount() == 0 && !this.mHasLaunchedPrivateVolumeSettings) {
                this.mHasLaunchedPrivateVolumeSettings = true;
                Bundle args = new Bundle();
                args.putString("android.os.storage.extra.VOLUME_ID", "private");
                new SubSettingLauncher(getActivity()).setDestination(StorageDashboardFragment.class.getName()).setArguments(args).setTitle((int) R.string.storage_settings).setSourceMetricsCategory(getMetricsCategory()).launch();
                finish();
            }
        }
    }

    public void onResume() {
        super.onResume();
        this.mStorageManager.registerListener(this.mStorageListener);
        refresh();
    }

    public void onPause() {
        super.onPause();
        this.mStorageManager.unregisterListener(this.mStorageListener);
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        String key = pref.getKey();
        if (pref instanceof StorageVolumePreference) {
            VolumeInfo vol = this.mStorageManager.findVolumeById(key);
            if (vol == null) {
                return false;
            }
            if (vol.getState() == 0) {
                VolumeUnmountedFragment.show(this, vol.getId());
                return true;
            } else if (vol.getState() == 6) {
                DiskInitFragment.show(this, R.string.storage_dialog_unmountable, vol.getDiskId());
                return true;
            } else if (vol.getType() == 1) {
                Bundle args = new Bundle();
                args.putString("android.os.storage.extra.VOLUME_ID", vol.getId());
                if ("private".equals(vol.getId())) {
                    new SubSettingLauncher(getContext()).setDestination(StorageDashboardFragment.class.getCanonicalName()).setTitle((int) R.string.storage_settings).setSourceMetricsCategory(getMetricsCategory()).setArguments(args).launch();
                } else {
                    PrivateVolumeSettings.setVolumeSize(args, PrivateStorageInfo.getTotalSize(vol, sTotalInternalStorage));
                    new SubSettingLauncher(getContext()).setDestination(PrivateVolumeSettings.class.getCanonicalName()).setTitle(-1).setSourceMetricsCategory(getMetricsCategory()).setArguments(args).launch();
                }
                return true;
            } else if (vol.getType() == 0) {
                return handlePublicVolumeClick(getContext(), vol);
            } else {
                return false;
            }
        } else if (key.startsWith("disk:")) {
            DiskInitFragment.show(this, R.string.storage_dialog_unsupported, key);
            return true;
        } else {
            Bundle args2 = new Bundle();
            args2.putString("android.os.storage.extra.FS_UUID", key);
            new SubSettingLauncher(getContext()).setDestination(PrivateVolumeForget.class.getCanonicalName()).setTitle((int) R.string.storage_menu_forget).setSourceMetricsCategory(getMetricsCategory()).setArguments(args2).launch();
            return true;
        }
    }

    @VisibleForTesting
    static boolean handlePublicVolumeClick(Context context, VolumeInfo vol) {
        Intent intent = vol.buildBrowseIntent();
        if (!vol.isMountedReadable() || intent == null) {
            Bundle args = new Bundle();
            args.putString("android.os.storage.extra.VOLUME_ID", vol.getId());
            new SubSettingLauncher(context).setDestination(PublicVolumeSettings.class.getCanonicalName()).setTitle(-1).setSourceMetricsCategory(42).setArguments(args).launch();
            return true;
        }
        context.startActivity(intent);
        return true;
    }
}
