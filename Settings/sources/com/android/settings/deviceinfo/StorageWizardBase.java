package com.android.settings.deviceinfo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settingslib.Utils;
import com.android.setupwizardlib.GlifLayout;
import java.text.NumberFormat;
import java.util.Objects;

public abstract class StorageWizardBase extends Activity {
    protected static final String EXTRA_FORMAT_FORGET_UUID = "format_forget_uuid";
    protected static final String EXTRA_FORMAT_PRIVATE = "format_private";
    protected static final String EXTRA_FORMAT_SLOW = "format_slow";
    protected static final String EXTRA_MIGRATE_SKIP = "migrate_skip";
    private Button mBack;
    protected DiskInfo mDisk;
    private Button mNext;
    protected StorageManager mStorage;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onDiskDestroyed(DiskInfo disk) {
            if (StorageWizardBase.this.mDisk.id.equals(disk.id)) {
                StorageWizardBase.this.finish();
            }
        }
    };
    protected VolumeInfo mVolume;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mStorage = (StorageManager) getSystemService(StorageManager.class);
        String volumeId = getIntent().getStringExtra("android.os.storage.extra.VOLUME_ID");
        if (!TextUtils.isEmpty(volumeId)) {
            this.mVolume = this.mStorage.findVolumeById(volumeId);
        }
        String diskId = getIntent().getStringExtra("android.os.storage.extra.DISK_ID");
        if (!TextUtils.isEmpty(diskId)) {
            this.mDisk = this.mStorage.findDiskById(diskId);
        } else if (this.mVolume != null) {
            this.mDisk = this.mVolume.getDisk();
        }
        if (this.mDisk != null) {
            this.mStorage.registerListener(this.mStorageListener);
        }
    }

    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        this.mBack = (Button) requireViewById(R.id.storage_back_button);
        this.mNext = (Button) requireViewById(R.id.storage_next_button);
        setIcon(17302733);
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        this.mStorage.unregisterListener(this.mStorageListener);
        super.onDestroy();
    }

    /* Access modifiers changed, original: protected */
    public Button getBackButton() {
        return this.mBack;
    }

    /* Access modifiers changed, original: protected */
    public Button getNextButton() {
        return this.mNext;
    }

    /* Access modifiers changed, original: protected */
    public GlifLayout getGlifLayout() {
        return (GlifLayout) requireViewById(R.id.setup_wizard_layout);
    }

    /* Access modifiers changed, original: protected */
    public ProgressBar getProgressBar() {
        return (ProgressBar) requireViewById(R.id.storage_wizard_progress);
    }

    /* Access modifiers changed, original: protected */
    public void setCurrentProgress(int progress) {
        getProgressBar().setProgress(progress);
        ((TextView) requireViewById(R.id.storage_wizard_progress_summary)).setText(NumberFormat.getPercentInstance().format(((double) progress) / 100.0d));
    }

    /* Access modifiers changed, original: protected|varargs */
    public void setHeaderText(int resId, CharSequence... args) {
        CharSequence headerText = TextUtils.expandTemplate(getText(resId), args);
        getGlifLayout().setHeaderText(headerText);
        setTitle(headerText);
    }

    /* Access modifiers changed, original: protected|varargs */
    public void setBodyText(int resId, CharSequence... args) {
        TextView body = (TextView) requireViewById(R.id.storage_wizard_body);
        body.setText(TextUtils.expandTemplate(getText(resId), args));
        body.setVisibility(0);
    }

    /* Access modifiers changed, original: protected */
    public void setAuxChecklist() {
        FrameLayout aux = (FrameLayout) requireViewById(R.id.storage_wizard_aux);
        aux.addView(LayoutInflater.from(aux.getContext()).inflate(R.layout.storage_wizard_checklist, aux, false));
        aux.setVisibility(0);
        ((TextView) aux.requireViewById(R.id.storage_wizard_migrate_v2_checklist_media)).setText(TextUtils.expandTemplate(getText(R.string.storage_wizard_migrate_v2_checklist_media), new CharSequence[]{getDiskShortDescription()}));
    }

    /* Access modifiers changed, original: protected|varargs */
    public void setBackButtonText(int resId, CharSequence... args) {
        this.mBack.setText(TextUtils.expandTemplate(getText(resId), args));
        this.mBack.setVisibility(0);
    }

    /* Access modifiers changed, original: protected|varargs */
    public void setNextButtonText(int resId, CharSequence... args) {
        this.mNext.setText(TextUtils.expandTemplate(getText(resId), args));
        this.mNext.setVisibility(0);
    }

    /* Access modifiers changed, original: protected */
    public void setIcon(int resId) {
        GlifLayout layout = getGlifLayout();
        Drawable icon = getDrawable(resId).mutate();
        icon.setTint(Utils.getColorAccent(layout.getContext()));
        layout.setIcon(icon);
    }

    /* Access modifiers changed, original: protected */
    public void setKeepScreenOn(boolean keepScreenOn) {
        getGlifLayout().setKeepScreenOn(keepScreenOn);
    }

    public void onNavigateBack(View view) {
        throw new UnsupportedOperationException();
    }

    public void onNavigateNext(View view) {
        throw new UnsupportedOperationException();
    }

    private void copyStringExtra(Intent from, Intent to, String key) {
        if (from.hasExtra(key) && !to.hasExtra(key)) {
            to.putExtra(key, from.getStringExtra(key));
        }
    }

    private void copyBooleanExtra(Intent from, Intent to, String key) {
        if (from.hasExtra(key) && !to.hasExtra(key)) {
            to.putExtra(key, from.getBooleanExtra(key, false));
        }
    }

    public void startActivity(Intent intent) {
        Intent from = getIntent();
        Intent to = intent;
        copyStringExtra(from, to, "android.os.storage.extra.DISK_ID");
        copyStringExtra(from, to, "android.os.storage.extra.VOLUME_ID");
        copyStringExtra(from, to, EXTRA_FORMAT_FORGET_UUID);
        copyBooleanExtra(from, to, EXTRA_FORMAT_PRIVATE);
        copyBooleanExtra(from, to, EXTRA_FORMAT_SLOW);
        copyBooleanExtra(from, to, EXTRA_MIGRATE_SKIP);
        super.startActivity(intent);
    }

    /* Access modifiers changed, original: protected */
    public VolumeInfo findFirstVolume(int type) {
        return findFirstVolume(type, 1);
    }

    /* Access modifiers changed, original: protected */
    public VolumeInfo findFirstVolume(int type, int attempts) {
        while (true) {
            for (VolumeInfo vol : this.mStorage.getVolumes()) {
                if (Objects.equals(this.mDisk.getId(), vol.getDiskId()) && vol.getType() == type && vol.getState() == 2) {
                    return vol;
                }
            }
            attempts--;
            if (attempts <= 0) {
                return null;
            }
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Missing mounted volume of type ");
            stringBuilder.append(type);
            stringBuilder.append(" hosted by disk ");
            stringBuilder.append(this.mDisk.getId());
            stringBuilder.append("; trying again");
            Log.w("StorageSettings", stringBuilder.toString());
            SystemClock.sleep(250);
        }
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getDiskDescription() {
        if (this.mDisk != null) {
            return this.mDisk.getDescription();
        }
        if (this.mVolume != null) {
            return this.mVolume.getDescription();
        }
        return getText(R.string.unknown);
    }

    /* Access modifiers changed, original: protected */
    public CharSequence getDiskShortDescription() {
        if (this.mDisk != null) {
            return this.mDisk.getShortDescription();
        }
        if (this.mVolume != null) {
            return this.mVolume.getDescription();
        }
        return getText(R.string.unknown);
    }
}
