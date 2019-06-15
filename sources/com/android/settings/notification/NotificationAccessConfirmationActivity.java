package com.android.settings.notification;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Slog;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;

public class NotificationAccessConfirmationActivity extends Activity implements DialogInterface {
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "NotificationAccessConfirmationActivity";
    private ComponentName mComponentName;
    private NotificationManager mNm;
    private int mUserId;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNm = (NotificationManager) getSystemService("notification");
        this.mComponentName = (ComponentName) getIntent().getParcelableExtra("component_name");
        this.mUserId = getIntent().getIntExtra("user_id", -10000);
        String pkgTitle = getIntent().getStringExtra("package_title");
        AlertParams p = new AlertParams(this);
        p.mTitle = getString(R.string.notification_listener_security_warning_title, new Object[]{pkgTitle});
        p.mMessage = getString(R.string.notification_listener_security_warning_summary, new Object[]{pkgTitle});
        p.mPositiveButtonText = getString(R.string.allow);
        p.mPositiveButtonListener = new -$$Lambda$NotificationAccessConfirmationActivity$UvveyFMEwlZ6m4ViLmcVExulBE8(this);
        p.mNegativeButtonText = getString(R.string.deny);
        p.mNegativeButtonListener = new -$$Lambda$NotificationAccessConfirmationActivity$hd7i7CSD_dVpjvK__hXE8eDM2I0(this);
        AlertController.create(this, this, getWindow()).installContent(p);
        getWindow().setCloseOnTouchOutside(false);
    }

    public void onResume() {
        super.onResume();
        getWindow().addFlags(524288);
    }

    public void onPause() {
        getWindow().clearFlags(524288);
        super.onPause();
    }

    private void onAllow() {
        String requiredPermission = "android.permission.BIND_NOTIFICATION_LISTENER_SERVICE";
        String str;
        StringBuilder stringBuilder;
        try {
            if (requiredPermission.equals(getPackageManager().getServiceInfo(this.mComponentName, 0).permission)) {
                this.mNm.setNotificationListenerAccessGranted(this.mComponentName, true);
                finish();
                return;
            }
            str = LOG_TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Service ");
            stringBuilder.append(this.mComponentName);
            stringBuilder.append(" lacks permission ");
            stringBuilder.append(requiredPermission);
            Slog.e(str, stringBuilder.toString());
        } catch (NameNotFoundException e) {
            str = LOG_TAG;
            stringBuilder = new StringBuilder();
            stringBuilder.append("Failed to get service info for ");
            stringBuilder.append(this.mComponentName);
            Slog.e(str, stringBuilder.toString(), e);
        }
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return AlertActivity.dispatchPopulateAccessibilityEvent(this, event);
    }

    public void onBackPressed() {
    }

    public void cancel() {
        finish();
    }

    public void dismiss() {
        if (!isFinishing()) {
            finish();
        }
    }
}
