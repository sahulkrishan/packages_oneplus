package com.android.settings.enterprise;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class ActionDisabledByAdminDialog extends Activity implements OnDismissListener {
    private ActionDisabledByAdminDialogHelper mDialogHelper;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EnforcedAdmin enforcedAdmin = getAdminDetailsFromIntent(getIntent());
        String restriction = getRestrictionFromIntent(getIntent());
        this.mDialogHelper = new ActionDisabledByAdminDialogHelper(this);
        this.mDialogHelper.prepareDialogBuilder(restriction, enforcedAdmin).setOnDismissListener(this).show();
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        EnforcedAdmin admin = getAdminDetailsFromIntent(intent);
        this.mDialogHelper.updateDialog(getRestrictionFromIntent(intent), admin);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public EnforcedAdmin getAdminDetailsFromIntent(Intent intent) {
        EnforcedAdmin admin = new EnforcedAdmin(null, UserHandle.myUserId());
        if (intent == null) {
            return admin;
        }
        admin.component = (ComponentName) intent.getParcelableExtra("android.app.extra.DEVICE_ADMIN");
        admin.userId = intent.getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        return admin;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getRestrictionFromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra("android.app.extra.RESTRICTION");
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
