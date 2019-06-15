package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.DocumentsContract;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.android.settings.R;
import com.android.settings.Settings.StorageUseActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.applications.manageapplications.ManageApplications;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settings.deviceinfo.StorageSettings.MountTask;
import com.android.settingslib.deviceinfo.StorageMeasurement;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementDetails;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementReceiver;
import com.google.android.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PrivateVolumeSettings extends SettingsPreferenceFragment {
    private static final String AUTHORITY_MEDIA = "com.android.providers.media.documents";
    private static final String EXTRA_VOLUME_SIZE = "volume_size";
    private static final int[] ITEMS_NO_SHOW_SHARED = new int[]{R.string.storage_detail_apps, R.string.storage_detail_system};
    private static final int[] ITEMS_SHOW_SHARED = new int[]{R.string.storage_detail_apps, R.string.storage_detail_images, R.string.storage_detail_videos, R.string.storage_detail_audio, R.string.storage_detail_system, R.string.storage_detail_other};
    private static final boolean LOGV = false;
    private static final String TAG = "PrivateVolumeSettings";
    private static final String TAG_CONFIRM_CLEAR_CACHE = "confirmClearCache";
    private static final String TAG_OTHER_INFO = "otherInfo";
    private static final String TAG_RENAME = "rename";
    private static final String TAG_SYSTEM_INFO = "systemInfo";
    private static final String TAG_USER_INFO = "userInfo";
    private UserInfo mCurrentUser;
    private Preference mExplore;
    private int mHeaderPoolIndex;
    private List<PreferenceCategory> mHeaderPreferencePool = Lists.newArrayList();
    private int mItemPoolIndex;
    private List<StorageItemPreference> mItemPreferencePool = Lists.newArrayList();
    private StorageMeasurement mMeasure;
    private boolean mNeedsUpdate;
    private final MeasurementReceiver mReceiver = new MeasurementReceiver() {
        public void onDetailsChanged(MeasurementDetails details) {
            PrivateVolumeSettings.this.updateDetails(details);
        }
    };
    private VolumeInfo mSharedVolume;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (Objects.equals(PrivateVolumeSettings.this.mVolume.getId(), vol.getId())) {
                PrivateVolumeSettings.this.mVolume = vol;
                PrivateVolumeSettings.this.update();
            }
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            if (Objects.equals(PrivateVolumeSettings.this.mVolume.getFsUuid(), rec.getFsUuid())) {
                PrivateVolumeSettings.this.mVolume = PrivateVolumeSettings.this.mStorageManager.findVolumeById(PrivateVolumeSettings.this.mVolumeId);
                PrivateVolumeSettings.this.update();
            }
        }
    };
    private StorageManager mStorageManager;
    private StorageSummaryPreference mSummary;
    private long mSystemSize;
    private long mTotalSize;
    private UserManager mUserManager;
    private VolumeInfo mVolume;
    private String mVolumeId;

    private static class ClearCacheObserver extends Stub {
        private int mRemaining;
        private final PrivateVolumeSettings mTarget;

        public ClearCacheObserver(PrivateVolumeSettings target, int remaining) {
            this.mTarget = target;
            this.mRemaining = remaining;
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            synchronized (this) {
                int i = this.mRemaining - 1;
                this.mRemaining = i;
                if (i == 0) {
                    this.mTarget.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            ClearCacheObserver.this.mTarget.update();
                        }
                    });
                }
            }
        }
    }

    public static class ConfirmClearCacheFragment extends InstrumentedDialogFragment {
        public static void show(Fragment parent) {
            if (parent.isAdded()) {
                ConfirmClearCacheFragment dialog = new ConfirmClearCacheFragment();
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), PrivateVolumeSettings.TAG_CONFIRM_CLEAR_CACHE);
            }
        }

        public int getMetricsCategory() {
            return 564;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            Builder builder = new Builder(context);
            builder.setTitle(R.string.memory_clear_cache_title);
            builder.setMessage(getString(R.string.memory_clear_cache_message));
            builder.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PrivateVolumeSettings target = (PrivateVolumeSettings) ConfirmClearCacheFragment.this.getTargetFragment();
                    PackageManager pm = context.getPackageManager();
                    for (int userId : ((UserManager) context.getSystemService(UserManager.class)).getProfileIdsWithDisabled(context.getUserId())) {
                        List<PackageInfo> infos = pm.getInstalledPackagesAsUser(0, userId);
                        ClearCacheObserver observer = new ClearCacheObserver(target, infos.size());
                        for (PackageInfo info : infos) {
                            pm.deleteApplicationCacheFilesAsUser(info.packageName, userId, observer);
                        }
                    }
                }
            });
            builder.setNegativeButton(17039360, null);
            return builder.create();
        }
    }

    public static class OtherInfoFragment extends InstrumentedDialogFragment {
        public static void show(Fragment parent, String title, VolumeInfo sharedVol, int userId) {
            if (parent.isAdded()) {
                OtherInfoFragment dialog = new OtherInfoFragment();
                dialog.setTargetFragment(parent, 0);
                Bundle args = new Bundle();
                args.putString("android.intent.extra.TITLE", title);
                Intent intent = sharedVol.buildBrowseIntent();
                intent.putExtra("android.intent.extra.USER_ID", userId);
                args.putParcelable("android.intent.extra.INTENT", intent);
                dialog.setArguments(args);
                dialog.show(parent.getFragmentManager(), PrivateVolumeSettings.TAG_OTHER_INFO);
            }
        }

        public int getMetricsCategory() {
            return 566;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            String title = getArguments().getString("android.intent.extra.TITLE");
            final Intent intent = (Intent) getArguments().getParcelable("android.intent.extra.INTENT");
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(R.string.storage_detail_dialog_other), new CharSequence[]{title}));
            builder.setPositiveButton(R.string.storage_menu_explore, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Utils.launchIntent(OtherInfoFragment.this, intent);
                }
            });
            builder.setNegativeButton(17039360, null);
            return builder.create();
        }
    }

    public static class RenameFragment extends InstrumentedDialogFragment {
        public static void show(PrivateVolumeSettings parent, VolumeInfo vol) {
            if (parent.isAdded()) {
                RenameFragment dialog = new RenameFragment();
                dialog.setTargetFragment(parent, 0);
                Bundle args = new Bundle();
                args.putString("android.os.storage.extra.FS_UUID", vol.getFsUuid());
                dialog.setArguments(args);
                dialog.show(parent.getFragmentManager(), PrivateVolumeSettings.TAG_RENAME);
            }
        }

        public int getMetricsCategory() {
            return 563;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            final StorageManager storageManager = (StorageManager) context.getSystemService(StorageManager.class);
            final String fsUuid = getArguments().getString("android.os.storage.extra.FS_UUID");
            VolumeInfo vol = storageManager.findVolumeByUuid(fsUuid);
            VolumeRecord rec = storageManager.findRecordByUuid(fsUuid);
            Builder builder = new Builder(context);
            View view = LayoutInflater.from(builder.getContext()).inflate(R.layout.dialog_edittext, null, false);
            final EditText nickname = (EditText) view.findViewById(R.id.edittext);
            nickname.setText(rec.getNickname());
            builder.setTitle(R.string.storage_rename_title);
            builder.setView(view);
            builder.setPositiveButton(R.string.save, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    storageManager.setVolumeNickname(fsUuid, nickname.getText().toString());
                }
            });
            builder.setNegativeButton(R.string.cancel, null);
            return builder.create();
        }
    }

    public static class SystemInfoFragment extends InstrumentedDialogFragment {
        public static void show(Fragment parent) {
            if (parent.isAdded()) {
                SystemInfoFragment dialog = new SystemInfoFragment();
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), PrivateVolumeSettings.TAG_SYSTEM_INFO);
            }
        }

        public int getMetricsCategory() {
            return 565;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setMessage(getContext().getString(R.string.storage_detail_dialog_system, new Object[]{VERSION.RELEASE})).setPositiveButton(17039370, null).create();
        }
    }

    public static class UserInfoFragment extends InstrumentedDialogFragment {
        public static void show(Fragment parent, CharSequence userLabel, CharSequence userSize) {
            if (parent.isAdded()) {
                UserInfoFragment dialog = new UserInfoFragment();
                dialog.setTargetFragment(parent, 0);
                Bundle args = new Bundle();
                args.putCharSequence("android.intent.extra.TITLE", userLabel);
                args.putCharSequence("android.intent.extra.SUBJECT", userSize);
                dialog.setArguments(args);
                dialog.show(parent.getFragmentManager(), PrivateVolumeSettings.TAG_USER_INFO);
            }
        }

        public int getMetricsCategory() {
            return 567;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            CharSequence userLabel = getArguments().getCharSequence("android.intent.extra.TITLE");
            CharSequence userSize = getArguments().getCharSequence("android.intent.extra.SUBJECT");
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(R.string.storage_detail_dialog_user), new CharSequence[]{userLabel, userSize}));
            builder.setPositiveButton(17039370, null);
            return builder.create();
        }
    }

    private boolean isVolumeValid() {
        if (this.mVolume != null && this.mVolume.getType() == 1 && this.mVolume.isMountedReadable()) {
            return true;
        }
        return false;
    }

    public PrivateVolumeSettings() {
        setRetainInstance(true);
    }

    public int getMetricsCategory() {
        return 42;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        this.mVolumeId = getArguments().getString("android.os.storage.extra.VOLUME_ID");
        this.mVolume = this.mStorageManager.findVolumeById(this.mVolumeId);
        long sharedDataSize = this.mVolume.getPath().getTotalSpace();
        this.mTotalSize = getArguments().getLong(EXTRA_VOLUME_SIZE, 0);
        this.mSystemSize = this.mTotalSize - sharedDataSize;
        if (this.mTotalSize <= 0) {
            this.mTotalSize = sharedDataSize;
            this.mSystemSize = 0;
        }
        this.mSharedVolume = this.mStorageManager.findEmulatedForPrivate(this.mVolume);
        this.mMeasure = new StorageMeasurement(context, this.mVolume, this.mSharedVolume);
        this.mMeasure.setReceiver(this.mReceiver);
        if (isVolumeValid()) {
            addPreferencesFromResource(R.xml.device_info_storage_volume);
            getPreferenceScreen().setOrderingAsAdded(true);
            this.mSummary = new StorageSummaryPreference(getPrefContext());
            this.mCurrentUser = this.mUserManager.getUserInfo(UserHandle.myUserId());
            this.mExplore = buildAction(R.string.storage_menu_explore);
            this.mNeedsUpdate = true;
            setHasOptionsMenu(true);
            return;
        }
        getActivity().finish();
    }

    private void setTitle() {
        getActivity().setTitle(this.mStorageManager.getBestVolumeDescription(this.mVolume));
    }

    private void update() {
        if (isVolumeValid()) {
            setTitle();
            getFragmentManager().invalidateOptionsMenu();
            Context context = getActivity();
            PreferenceGroup screen = getPreferenceScreen();
            screen.removeAll();
            addPreference(screen, this.mSummary);
            List<UserInfo> allUsers = this.mUserManager.getUsers();
            int userCount = allUsers.size();
            boolean showHeaders = userCount > 1;
            boolean showShared = this.mSharedVolume != null && this.mSharedVolume.isMountedReadable();
            this.mItemPoolIndex = 0;
            this.mHeaderPoolIndex = 0;
            int addedUserCount = 0;
            for (int userIndex = 0; userIndex < userCount; userIndex++) {
                UserInfo userInfo = (UserInfo) allUsers.get(userIndex);
                if (Utils.isProfileOf(this.mCurrentUser, userInfo)) {
                    addDetailItems(showHeaders ? addCategory(screen, userInfo.name) : screen, showShared, userInfo.id);
                    addedUserCount++;
                }
            }
            if (userCount - addedUserCount > 0) {
                PreferenceGroup otherUsers = addCategory(screen, getText(R.string.storage_other_users));
                for (int userIndex2 = 0; userIndex2 < userCount; userIndex2++) {
                    UserInfo userInfo2 = (UserInfo) allUsers.get(userIndex2);
                    if (!Utils.isProfileOf(this.mCurrentUser, userInfo2)) {
                        addItem(otherUsers, 0, userInfo2.name, userInfo2.id);
                    }
                }
            }
            addItem(screen, R.string.storage_detail_cached, null, -10000);
            if (showShared) {
                addPreference(screen, this.mExplore);
            }
            long usedBytes = this.mTotalSize - this.mVolume.getPath().getFreeSpace();
            BytesResult result = Formatter.formatBytes(getResources(), usedBytes, 0);
            StorageSummaryPreference storageSummaryPreference = this.mSummary;
            CharSequence text = getText(R.string.storage_size_large);
            r6 = new CharSequence[2];
            r6[0] = result.value;
            r6[1] = result.units;
            storageSummaryPreference.setTitle(TextUtils.expandTemplate(text, r6));
            this.mSummary.setSummary((CharSequence) getString(R.string.storage_volume_used, new Object[]{Formatter.formatFileSize(context, this.mTotalSize)}));
            this.mSummary.setPercent(usedBytes, this.mTotalSize);
            this.mMeasure.forceMeasure();
            this.mNeedsUpdate = false;
            return;
        }
        getActivity().finish();
    }

    private void addPreference(PreferenceGroup group, Preference pref) {
        pref.setOrder(Integer.MAX_VALUE);
        group.addPreference(pref);
    }

    private PreferenceCategory addCategory(PreferenceGroup group, CharSequence title) {
        PreferenceCategory category;
        if (this.mHeaderPoolIndex < this.mHeaderPreferencePool.size()) {
            category = (PreferenceCategory) this.mHeaderPreferencePool.get(this.mHeaderPoolIndex);
        } else {
            category = new PreferenceCategory(getPrefContext());
            this.mHeaderPreferencePool.add(category);
        }
        category.setTitle(title);
        category.removeAll();
        addPreference(group, category);
        this.mHeaderPoolIndex++;
        return category;
    }

    private void addDetailItems(PreferenceGroup category, boolean showShared, int userId) {
        int[] itemsToAdd = showShared ? ITEMS_SHOW_SHARED : ITEMS_NO_SHOW_SHARED;
        for (int addItem : itemsToAdd) {
            addItem(category, addItem, null, userId);
        }
    }

    private void addItem(PreferenceGroup group, int titleRes, CharSequence title, int userId) {
        StorageItemPreference item;
        if (titleRes == R.string.storage_detail_system) {
            if (this.mSystemSize <= 0) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping System storage because its size is ");
                stringBuilder.append(this.mSystemSize);
                Log.w(str, stringBuilder.toString());
                return;
            } else if (userId != UserHandle.myUserId()) {
                return;
            }
        }
        if (this.mItemPoolIndex < this.mItemPreferencePool.size()) {
            item = (StorageItemPreference) this.mItemPreferencePool.get(this.mItemPoolIndex);
        } else {
            item = buildItem();
            this.mItemPreferencePool.add(item);
        }
        if (title != null) {
            item.setTitle(title);
            item.setKey(title.toString());
        } else {
            item.setTitle(titleRes);
            item.setKey(Integer.toString(titleRes));
        }
        item.setSummary((int) R.string.memory_calculating_size);
        item.userHandle = userId;
        addPreference(group, item);
        this.mItemPoolIndex++;
    }

    private StorageItemPreference buildItem() {
        StorageItemPreference item = new StorageItemPreference(getPrefContext());
        item.setIcon((int) R.drawable.empty_icon);
        return item;
    }

    private Preference buildAction(int titleRes) {
        Preference pref = new Preference(getPrefContext());
        pref.setTitle(titleRes);
        pref.setKey(Integer.toString(titleRes));
        return pref;
    }

    static void setVolumeSize(Bundle args, long size) {
        args.putLong(EXTRA_VOLUME_SIZE, size);
    }

    public void onResume() {
        super.onResume();
        this.mVolume = this.mStorageManager.findVolumeById(this.mVolumeId);
        if (isVolumeValid()) {
            this.mStorageManager.registerListener(this.mStorageListener);
            if (this.mNeedsUpdate) {
                update();
            } else {
                setTitle();
            }
            return;
        }
        getActivity().finish();
    }

    public void onPause() {
        super.onPause();
        this.mStorageManager.unregisterListener(this.mStorageListener);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mMeasure != null) {
            this.mMeasure.onDestroy();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.storage_volume, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (isVolumeValid()) {
            MenuItem rename = menu.findItem(R.id.storage_rename);
            MenuItem mount = menu.findItem(R.id.storage_mount);
            MenuItem unmount = menu.findItem(R.id.storage_unmount);
            MenuItem format = menu.findItem(R.id.storage_format);
            MenuItem migrate = menu.findItem(R.id.storage_migrate);
            MenuItem manage = menu.findItem(R.id.storage_free);
            boolean z = true;
            if ("private".equals(this.mVolume.getId())) {
                rename.setVisible(false);
                mount.setVisible(false);
                unmount.setVisible(false);
                format.setVisible(false);
                manage.setVisible(getResources().getBoolean(R.bool.config_storage_manager_settings_enabled));
            } else {
                rename.setVisible(this.mVolume.getType() == 1);
                mount.setVisible(this.mVolume.getState() == 0);
                unmount.setVisible(this.mVolume.isMountedReadable());
                format.setVisible(true);
                manage.setVisible(false);
            }
            format.setTitle(R.string.storage_menu_format_public);
            VolumeInfo privateVol = getActivity().getPackageManager().getPrimaryStorageCurrentVolume();
            if (privateVol == null || privateVol.getType() != 1 || Objects.equals(this.mVolume, privateVol)) {
                z = false;
            }
            migrate.setVisible(z);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = getActivity();
        Bundle args = new Bundle();
        switch (item.getItemId()) {
            case R.id.storage_format /*2131363109*/:
                args.putString("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
                new SubSettingLauncher(context).setDestination(PrivateVolumeFormat.class.getCanonicalName()).setTitle((int) R.string.storage_menu_format).setSourceMetricsCategory(getMetricsCategory()).setArguments(args).launch();
                return true;
            case R.id.storage_free /*2131363110*/:
                startActivity(new Intent("android.os.storage.action.MANAGE_STORAGE"));
                return true;
            case R.id.storage_migrate /*2131363112*/:
                Intent intent = new Intent(context, StorageWizardMigrateConfirm.class);
                intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
                startActivity(intent);
                return true;
            case R.id.storage_mount /*2131363113*/:
                new MountTask(context, this.mVolume).execute(new Void[0]);
                return true;
            case R.id.storage_rename /*2131363115*/:
                RenameFragment.show(this, this.mVolume);
                return true;
            case R.id.storage_unmount /*2131363117*/:
                args.putString("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
                new SubSettingLauncher(context).setDestination(PrivateVolumeUnmount.class.getCanonicalName()).setTitle((int) R.string.storage_menu_unmount).setSourceMetricsCategory(getMetricsCategory()).setArguments(args).launch();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        int itemTitleId;
        int userId = pref instanceof StorageItemPreference ? ((StorageItemPreference) pref).userHandle : -1;
        try {
            itemTitleId = Integer.parseInt(pref.getKey());
        } catch (NumberFormatException e) {
            itemTitleId = 0;
        }
        Intent intent = null;
        if (itemTitleId != 0) {
            if (itemTitleId != R.string.storage_menu_explore) {
                switch (itemTitleId) {
                    case R.string.storage_detail_apps /*2131890474*/:
                        Bundle args = new Bundle();
                        args.putString(ManageApplications.EXTRA_CLASSNAME, StorageUseActivity.class.getName());
                        args.putString(ManageApplications.EXTRA_VOLUME_UUID, this.mVolume.getFsUuid());
                        args.putString(ManageApplications.EXTRA_VOLUME_NAME, this.mVolume.getDescription());
                        args.putInt(ManageApplications.EXTRA_STORAGE_TYPE, 2);
                        intent = new SubSettingLauncher(getActivity()).setDestination(ManageApplications.class.getName()).setArguments(args).setTitle((int) R.string.apps_storage).setSourceMetricsCategory(getMetricsCategory()).toIntent();
                        break;
                    case R.string.storage_detail_audio /*2131890475*/:
                        intent = getIntentForStorage(AUTHORITY_MEDIA, "audio_root");
                        break;
                    case R.string.storage_detail_cached /*2131890476*/:
                        ConfirmClearCacheFragment.show(this);
                        return true;
                    default:
                        switch (itemTitleId) {
                            case R.string.storage_detail_images /*2131890481*/:
                                intent = getIntentForStorage(AUTHORITY_MEDIA, "images_root");
                                break;
                            case R.string.storage_detail_other /*2131890482*/:
                                OtherInfoFragment.show(this, this.mStorageManager.getBestVolumeDescription(this.mVolume), this.mSharedVolume, userId);
                                return true;
                            case R.string.storage_detail_system /*2131890483*/:
                                SystemInfoFragment.show(this);
                                return true;
                            case R.string.storage_detail_videos /*2131890484*/:
                                intent = getIntentForStorage(AUTHORITY_MEDIA, "videos_root");
                                break;
                        }
                        break;
                }
            }
            intent = this.mSharedVolume.buildBrowseIntent();
            if (intent == null) {
                return super.onPreferenceTreeClick(pref);
            }
            intent.putExtra("android.intent.extra.USER_ID", userId);
            Utils.launchIntent(this, intent);
            return true;
        }
        UserInfoFragment.show(this, pref.getTitle(), pref.getSummary());
        return true;
    }

    private Intent getIntentForStorage(String authority, String root) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(DocumentsContract.buildRootUri(authority, root), "vnd.android.document/root");
        intent.addCategory("android.intent.category.DEFAULT");
        return intent;
    }

    /* JADX WARNING: Missing block: B:12:0x0038, code skipped:
            r16 = r9;
     */
    /* JADX WARNING: Missing block: B:27:0x00d1, code skipped:
            continue;
     */
    private void updateDetails(com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementDetails r25) {
        /*
        r24 = this;
        r1 = r24;
        r2 = r25;
        r0 = 0;
        r3 = 0;
        r5 = 0;
        r7 = 0;
        r9 = 0;
        r10 = r7;
        r6 = r5;
        r4 = r3;
        r3 = r0;
        r0 = r9;
    L_0x0011:
        r8 = r0;
        r0 = r1.mItemPoolIndex;
        if (r8 >= r0) goto L_0x00d7;
    L_0x0016:
        r0 = r1.mItemPreferencePool;
        r0 = r0.get(r8);
        r12 = r0;
        r12 = (com.android.settings.deviceinfo.StorageItemPreference) r12;
        r0 = r12.userHandle;
        r13 = r0;
        r0 = r12.getKey();	 Catch:{ NumberFormatException -> 0x002b }
        r0 = java.lang.Integer.parseInt(r0);	 Catch:{ NumberFormatException -> 0x002b }
        goto L_0x002e;
    L_0x002b:
        r0 = move-exception;
        r0 = r9;
    L_0x002e:
        if (r0 == 0) goto L_0x00c5;
    L_0x0030:
        r14 = 2;
        r15 = 1;
        switch(r0) {
            case 2131890474: goto L_0x00b2;
            case 2131890475: goto L_0x008e;
            case 2131890476: goto L_0x0082;
            default: goto L_0x0035;
        };
    L_0x0035:
        switch(r0) {
            case 2131890481: goto L_0x006d;
            case 2131890482: goto L_0x0054;
            case 2131890483: goto L_0x004b;
            case 2131890484: goto L_0x003c;
            default: goto L_0x0038;
        };
    L_0x0038:
        r16 = r9;
        goto L_0x00d1;
    L_0x003c:
        r14 = new java.lang.String[r15];
        r15 = android.os.Environment.DIRECTORY_MOVIES;
        r14[r9] = r15;
        r14 = totalValues(r2, r13, r14);
        r1.updatePreference(r12, r14);
        r4 = r4 + r14;
        goto L_0x0038;
    L_0x004b:
        r14 = r1.mSystemSize;
        r1.updatePreference(r12, r14);
        r14 = r1.mSystemSize;
        r4 = r4 + r14;
        goto L_0x0038;
    L_0x0054:
        r14 = new java.lang.String[r15];
        r15 = android.os.Environment.DIRECTORY_DOWNLOADS;
        r14[r9] = r15;
        r14 = totalValues(r2, r13, r14);
        r9 = r2.miscSize;
        r17 = r9.get(r13);
        r10 = r10 + r14;
        r6 = r6 + r17;
        r19 = r17 + r14;
        r4 = r4 + r19;
        r3 = r12;
        goto L_0x008b;
    L_0x006d:
        r9 = new java.lang.String[r14];
        r14 = android.os.Environment.DIRECTORY_DCIM;
        r16 = 0;
        r9[r16] = r14;
        r14 = android.os.Environment.DIRECTORY_PICTURES;
        r9[r15] = r14;
        r14 = totalValues(r2, r13, r9);
        r1.updatePreference(r12, r14);
        r4 = r4 + r14;
        goto L_0x008b;
    L_0x0082:
        r14 = r2.cacheSize;
        r1.updatePreference(r12, r14);
        r14 = r2.cacheSize;
        r4 = r4 + r14;
    L_0x008b:
        r16 = 0;
        goto L_0x00d1;
    L_0x008e:
        r9 = 5;
        r9 = new java.lang.String[r9];
        r17 = android.os.Environment.DIRECTORY_MUSIC;
        r16 = 0;
        r9[r16] = r17;
        r17 = android.os.Environment.DIRECTORY_ALARMS;
        r9[r15] = r17;
        r15 = android.os.Environment.DIRECTORY_NOTIFICATIONS;
        r9[r14] = r15;
        r14 = 3;
        r15 = android.os.Environment.DIRECTORY_RINGTONES;
        r9[r14] = r15;
        r14 = 4;
        r15 = android.os.Environment.DIRECTORY_PODCASTS;
        r9[r14] = r15;
        r14 = totalValues(r2, r13, r9);
        r1.updatePreference(r12, r14);
        r4 = r4 + r14;
        goto L_0x00d1;
    L_0x00b2:
        r16 = r9;
        r9 = r2.appsSize;
        r14 = r9.get(r13);
        r1.updatePreference(r12, r14);
        r9 = r2.appsSize;
        r14 = r9.get(r13);
        r4 = r4 + r14;
        goto L_0x00d1;
    L_0x00c5:
        r16 = r9;
        r9 = r2.usersSize;
        r14 = r9.get(r13);
        r1.updatePreference(r12, r14);
        r4 = r4 + r14;
    L_0x00d1:
        r0 = r8 + 1;
        r9 = r16;
        goto L_0x0011;
    L_0x00d7:
        if (r3 == 0) goto L_0x0140;
    L_0x00d9:
        r8 = r1.mTotalSize;
        r12 = r2.availSize;
        r8 = r8 - r12;
        r12 = r8 - r4;
        r14 = r6 + r10;
        r14 = r14 + r12;
        r0 = "PrivateVolumeSettings";
        r21 = r3;
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r22 = r14;
        r14 = "Other items: \n\tmTotalSize: ";
        r3.append(r14);
        r14 = r1.mTotalSize;
        r3.append(r14);
        r14 = " availSize: ";
        r3.append(r14);
        r14 = r2.availSize;
        r3.append(r14);
        r14 = " usedSize: ";
        r3.append(r14);
        r3.append(r8);
        r14 = "\n\taccountedSize: ";
        r3.append(r14);
        r3.append(r4);
        r14 = " unaccountedSize size: ";
        r3.append(r14);
        r3.append(r12);
        r14 = "\n\ttotalMiscSize: ";
        r3.append(r14);
        r3.append(r6);
        r14 = " totalDownloadsSize: ";
        r3.append(r14);
        r3.append(r10);
        r14 = "\n\tdetails: ";
        r3.append(r14);
        r3.append(r2);
        r3 = r3.toString();
        android.util.Log.v(r0, r3);
        r3 = r21;
        r14 = r22;
        r1.updatePreference(r3, r14);
    L_0x0140:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.deviceinfo.PrivateVolumeSettings.updateDetails(com.android.settingslib.deviceinfo.StorageMeasurement$MeasurementDetails):void");
    }

    private void updatePreference(StorageItemPreference pref, long size) {
        pref.setStorageSize(size, this.mTotalSize);
    }

    private static long totalValues(MeasurementDetails details, int userId, String... keys) {
        long total = 0;
        HashMap<String, Long> map = (HashMap) details.mediaSize.get(userId);
        if (map != null) {
            for (String key : keys) {
                if (map.containsKey(key)) {
                    total += ((Long) map.get(key)).longValue();
                }
            }
        } else {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("MeasurementDetails mediaSize array does not have key for user ");
            stringBuilder.append(userId);
            Log.w(str, stringBuilder.toString());
        }
        return total;
    }
}
