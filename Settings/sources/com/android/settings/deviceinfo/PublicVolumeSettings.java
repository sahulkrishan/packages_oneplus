package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.DocumentsContract;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import com.android.internal.util.Preconditions;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.deviceinfo.StorageSettings.MountTask;
import com.android.settings.deviceinfo.StorageSettings.UnmountTask;
import java.io.File;
import java.util.Objects;

public class PublicVolumeSettings extends SettingsPreferenceFragment {
    private DiskInfo mDisk;
    private Preference mFormatPrivate;
    private Preference mFormatPublic;
    private boolean mIsPermittedToAdopt;
    private Preference mMount;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (Objects.equals(PublicVolumeSettings.this.mVolume.getId(), vol.getId())) {
                PublicVolumeSettings.this.mVolume = vol;
                PublicVolumeSettings.this.update();
            }
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            if (Objects.equals(PublicVolumeSettings.this.mVolume.getFsUuid(), rec.getFsUuid())) {
                PublicVolumeSettings.this.mVolume = PublicVolumeSettings.this.mStorageManager.findVolumeById(PublicVolumeSettings.this.mVolumeId);
                PublicVolumeSettings.this.update();
            }
        }
    };
    private StorageManager mStorageManager;
    private StorageSummaryPreference mSummary;
    private Button mUnmount;
    private final OnClickListener mUnmountListener = new OnClickListener() {
        public void onClick(View v) {
            new UnmountTask(PublicVolumeSettings.this.getActivity(), PublicVolumeSettings.this.mVolume).execute(new Void[0]);
        }
    };
    private VolumeInfo mVolume;
    private String mVolumeId;

    private boolean isVolumeValid() {
        return this.mVolume != null && this.mVolume.getType() == 0 && this.mVolume.isMountedReadable();
    }

    public int getMetricsCategory() {
        return 42;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        boolean z = UserManager.get(context).isAdminUser() && !ActivityManager.isUserAMonkey();
        this.mIsPermittedToAdopt = z;
        this.mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        if ("android.provider.action.DOCUMENT_ROOT_SETTINGS".equals(getActivity().getIntent().getAction())) {
            this.mVolume = this.mStorageManager.findVolumeByUuid(DocumentsContract.getRootId(getActivity().getIntent().getData()));
        } else {
            String volId = getArguments().getString("android.os.storage.extra.VOLUME_ID");
            if (volId != null) {
                this.mVolume = this.mStorageManager.findVolumeById(volId);
            }
        }
        if (isVolumeValid()) {
            this.mDisk = this.mStorageManager.findDiskById(this.mVolume.getDiskId());
            Preconditions.checkNotNull(this.mDisk);
            this.mVolumeId = this.mVolume.getId();
            addPreferencesFromResource(R.xml.device_info_storage_volume);
            getPreferenceScreen().setOrderingAsAdded(true);
            this.mSummary = new StorageSummaryPreference(getPrefContext());
            this.mMount = buildAction(R.string.storage_menu_mount);
            this.mUnmount = new Button(getActivity());
            this.mUnmount.setText(R.string.storage_menu_unmount);
            this.mUnmount.setOnClickListener(this.mUnmountListener);
            this.mFormatPublic = buildAction(R.string.storage_menu_format);
            if (this.mIsPermittedToAdopt) {
                this.mFormatPrivate = buildAction(R.string.storage_menu_format_private);
            }
            return;
        }
        getActivity().finish();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isVolumeValid()) {
            int padding = getResources().getDimensionPixelSize(R.dimen.unmount_button_padding);
            ViewGroup buttonBar = getButtonBar();
            buttonBar.removeAllViews();
            buttonBar.setPadding(padding, padding, padding, padding);
            buttonBar.addView(this.mUnmount, new LayoutParams(-1, -2));
        }
    }

    public void update() {
        if (isVolumeValid()) {
            getActivity().setTitle(this.mStorageManager.getBestVolumeDescription(this.mVolume));
            Context context = getActivity();
            getPreferenceScreen().removeAll();
            if (this.mVolume.isMountedReadable()) {
                addPreference(this.mSummary);
                File file = this.mVolume.getPath();
                long totalBytes = file.getTotalSpace();
                long usedBytes = totalBytes - file.getFreeSpace();
                BytesResult result = Formatter.formatBytes(getResources(), usedBytes, 0);
                this.mSummary.setTitle(TextUtils.expandTemplate(getText(R.string.storage_size_large), new CharSequence[]{result.value, result.units}));
                this.mSummary.setSummary((CharSequence) getString(R.string.storage_volume_used, new Object[]{Formatter.formatFileSize(context, totalBytes)}));
                this.mSummary.setPercent(usedBytes, totalBytes);
            }
            if (this.mVolume.getState() == 0) {
                addPreference(this.mMount);
            }
            if (this.mVolume.isMountedReadable()) {
                getButtonBar().setVisibility(0);
            }
            addPreference(this.mFormatPublic);
            if (this.mDisk.isAdoptable() && this.mIsPermittedToAdopt) {
                addPreference(this.mFormatPrivate);
            }
            return;
        }
        getActivity().finish();
    }

    private void addPreference(Preference pref) {
        pref.setOrder(Integer.MAX_VALUE);
        getPreferenceScreen().addPreference(pref);
    }

    private Preference buildAction(int titleRes) {
        Preference pref = new Preference(getPrefContext());
        pref.setTitle(titleRes);
        return pref;
    }

    public void onResume() {
        super.onResume();
        this.mVolume = this.mStorageManager.findVolumeById(this.mVolumeId);
        if (isVolumeValid()) {
            this.mStorageManager.registerListener(this.mStorageListener);
            update();
            return;
        }
        getActivity().finish();
    }

    public void onPause() {
        super.onPause();
        this.mStorageManager.unregisterListener(this.mStorageListener);
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        if (pref == this.mMount) {
            new MountTask(getActivity(), this.mVolume).execute(new Void[0]);
        } else if (pref == this.mFormatPublic) {
            StorageWizardFormatConfirm.showPublic(getActivity(), this.mDisk.getId());
        } else if (pref == this.mFormatPrivate) {
            StorageWizardFormatConfirm.showPrivate(getActivity(), this.mDisk.getId());
        }
        return super.onPreferenceTreeClick(pref);
    }
}
