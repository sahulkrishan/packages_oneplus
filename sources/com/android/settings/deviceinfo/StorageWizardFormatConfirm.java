package com.android.settings.deviceinfo;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;

public class StorageWizardFormatConfirm extends InstrumentedDialogFragment {
    private static final String TAG_FORMAT_WARNING = "format_warning";

    public static void showPublic(Activity activity, String diskId) {
        show(activity, diskId, null, false);
    }

    public static void showPublic(Activity activity, String diskId, String forgetUuid) {
        show(activity, diskId, forgetUuid, false);
    }

    public static void showPrivate(Activity activity, String diskId) {
        show(activity, diskId, null, true);
    }

    private static void show(Activity activity, String diskId, String formatForgetUuid, boolean formatPrivate) {
        Bundle args = new Bundle();
        args.putString("android.os.storage.extra.DISK_ID", diskId);
        args.putString("format_forget_uuid", formatForgetUuid);
        args.putBoolean("format_private", formatPrivate);
        StorageWizardFormatConfirm fragment = new StorageWizardFormatConfirm();
        fragment.setArguments(args);
        fragment.showAllowingStateLoss(activity.getFragmentManager(), TAG_FORMAT_WARNING);
    }

    public int getMetricsCategory() {
        return 1375;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = getContext();
        Bundle args = getArguments();
        String diskId = args.getString("android.os.storage.extra.DISK_ID");
        String formatForgetUuid = args.getString("format_forget_uuid");
        boolean formatPrivate = args.getBoolean("format_private", false);
        DiskInfo disk = ((StorageManager) context.getSystemService(StorageManager.class)).findDiskById(diskId);
        Builder builder = new Builder(context);
        builder.setTitle(TextUtils.expandTemplate(getText(R.string.storage_wizard_format_confirm_v2_title), new CharSequence[]{disk.getShortDescription()}));
        builder.setMessage(TextUtils.expandTemplate(getText(R.string.storage_wizard_format_confirm_v2_body), new CharSequence[]{disk.getDescription(), disk.getShortDescription(), disk.getShortDescription()}));
        builder.setNegativeButton(17039360, null);
        builder.setPositiveButton(TextUtils.expandTemplate(getText(R.string.storage_wizard_format_confirm_v2_action), new CharSequence[]{disk.getShortDescription()}), new -$$Lambda$StorageWizardFormatConfirm$c4jIKjriuaEtVR7ERojcHILapk8(context, diskId, formatForgetUuid, formatPrivate));
        return builder.create();
    }

    static /* synthetic */ void lambda$onCreateDialog$0(Context context, String diskId, String formatForgetUuid, boolean formatPrivate, DialogInterface dialog, int which) {
        Intent intent = new Intent(context, StorageWizardFormatProgress.class);
        intent.putExtra("android.os.storage.extra.DISK_ID", diskId);
        intent.putExtra("format_forget_uuid", formatForgetUuid);
        intent.putExtra("format_private", formatPrivate);
        context.startActivity(intent);
    }
}
