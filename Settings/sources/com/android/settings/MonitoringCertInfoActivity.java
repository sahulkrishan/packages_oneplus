package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.admin.DevicePolicyManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import com.android.settingslib.RestrictedLockUtils;

public class MonitoringCertInfoActivity extends Activity implements OnClickListener, OnDismissListener {
    private int mUserId;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedStates) {
        super.onCreate(savedStates);
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(DevicePolicyManager.class);
        int numberOfCertificates = getIntent().getIntExtra("android.settings.extra.number_of_certificates", 1);
        CharSequence title = getResources().getQuantityText(RestrictedLockUtils.getProfileOrDeviceOwner(this, this.mUserId) != null ? R.plurals.ssl_ca_cert_settings_button : R.plurals.ssl_ca_cert_dialog_title, numberOfCertificates);
        setTitle(title);
        Builder builder = new Builder(this);
        builder.setTitle(title);
        builder.setCancelable(true);
        builder.setPositiveButton(getResources().getQuantityText(R.plurals.ssl_ca_cert_settings_button, numberOfCertificates), this);
        builder.setNeutralButton(R.string.cancel, null);
        builder.setOnDismissListener(this);
        if (dpm.getProfileOwnerAsUser(this.mUserId) != null) {
            builder.setMessage(getResources().getQuantityString(R.plurals.ssl_ca_cert_info_message, numberOfCertificates, new Object[]{dpm.getProfileOwnerNameAsUser(this.mUserId)}));
        } else if (dpm.getDeviceOwnerComponentOnCallingUser() != null) {
            builder.setMessage(getResources().getQuantityString(R.plurals.ssl_ca_cert_info_message_device_owner, numberOfCertificates, new Object[]{dpm.getDeviceOwnerNameOnAnyUser()}));
        } else {
            builder.setIcon(17301624);
            builder.setMessage(R.string.ssl_ca_cert_warning_message);
        }
        builder.show();
    }

    public void onClick(DialogInterface dialog, int which) {
        Intent intent = new Intent("com.android.settings.TRUSTED_CREDENTIALS_USER");
        intent.setFlags(335544320);
        intent.putExtra(TrustedCredentialsSettings.ARG_SHOW_NEW_FOR_USER, this.mUserId);
        startActivity(intent);
        finish();
    }

    public void onDismiss(DialogInterface dialogInterface) {
        finish();
    }
}
