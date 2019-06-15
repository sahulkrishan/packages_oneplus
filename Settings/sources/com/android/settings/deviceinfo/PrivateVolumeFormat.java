package com.android.settings.deviceinfo;

import android.content.Intent;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.InstrumentedPreferenceFragment;

public class PrivateVolumeFormat extends InstrumentedPreferenceFragment {
    private final OnClickListener mConfirmListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent(PrivateVolumeFormat.this.getActivity(), StorageWizardFormatProgress.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", PrivateVolumeFormat.this.mDisk.getId());
            intent.putExtra("format_private", false);
            intent.putExtra("format_forget_uuid", PrivateVolumeFormat.this.mVolume.getFsUuid());
            PrivateVolumeFormat.this.startActivity(intent);
            PrivateVolumeFormat.this.getActivity().finish();
        }
    };
    private DiskInfo mDisk;
    private VolumeInfo mVolume;

    public int getMetricsCategory() {
        return 42;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        StorageManager storage = (StorageManager) getActivity().getSystemService(StorageManager.class);
        this.mVolume = storage.findVolumeById(getArguments().getString("android.os.storage.extra.VOLUME_ID"));
        this.mDisk = storage.findDiskById(this.mVolume.getDiskId());
        View view = inflater.inflate(R.layout.storage_internal_format, container, false);
        Button confirm = (Button) view.findViewById(R.id.confirm);
        ((TextView) view.findViewById(R.id.body)).setText(TextUtils.expandTemplate(getText(R.string.storage_internal_format_details), new CharSequence[]{this.mDisk.getDescription()}));
        confirm.setOnClickListener(this.mConfirmListener);
        return view;
    }
}
