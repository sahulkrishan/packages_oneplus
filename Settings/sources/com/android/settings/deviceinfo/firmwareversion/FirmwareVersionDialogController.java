package com.android.settings.deviceinfo.firmwareversion;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.SystemClock;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import com.android.internal.app.PlatLogoActivity;
import com.android.settings.R;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class FirmwareVersionDialogController implements OnClickListener {
    private static final int ACTIVITY_TRIGGER_COUNT = 3;
    private static final int DELAY_TIMER_MILLIS = 500;
    @VisibleForTesting
    static final int FIRMWARE_VERSION_LABEL_ID = 2131362300;
    @VisibleForTesting
    static final int FIRMWARE_VERSION_VALUE_ID = 2131362301;
    private static final String TAG = "firmwareDialogCtrl";
    private final Context mContext;
    private final FirmwareVersionDialogFragment mDialog;
    private EnforcedAdmin mFunDisallowedAdmin;
    private boolean mFunDisallowedBySystem;
    private final long[] mHits = new long[3];
    private final UserManager mUserManager;

    public FirmwareVersionDialogController(FirmwareVersionDialogFragment dialog) {
        this.mDialog = dialog;
        this.mContext = dialog.getContext();
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
    }

    public void onClick(View v) {
        arrayCopy();
        this.mHits[this.mHits.length - 1] = SystemClock.uptimeMillis();
        if (this.mHits[0] >= SystemClock.uptimeMillis() - 500) {
            if (this.mUserManager.hasUserRestriction("no_fun")) {
                if (!(this.mFunDisallowedAdmin == null || this.mFunDisallowedBySystem)) {
                    RestrictedLockUtils.sendShowAdminSupportDetailsIntent(this.mContext, this.mFunDisallowedAdmin);
                }
                Log.d(TAG, "Sorry, no fun for you!");
                return;
            }
            Intent intent = new Intent("android.intent.action.MAIN").setClassName("android", PlatLogoActivity.class.getName());
            try {
                this.mContext.startActivity(intent);
            } catch (Exception e) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Unable to start activity ");
                stringBuilder.append(intent.toString());
                Log.e(str, stringBuilder.toString());
            }
        }
    }

    public void initialize() {
        initializeAdminPermissions();
        registerClickListeners();
        this.mDialog.setText(R.id.firmware_version_value, VERSION.RELEASE);
    }

    private void registerClickListeners() {
        this.mDialog.registerClickListener(R.id.firmware_version_label, this);
        this.mDialog.registerClickListener(R.id.firmware_version_value, this);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void arrayCopy() {
        System.arraycopy(this.mHits, 1, this.mHits, 0, this.mHits.length - 1);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void initializeAdminPermissions() {
        this.mFunDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(this.mContext, "no_fun", UserHandle.myUserId());
        this.mFunDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(this.mContext, "no_fun", UserHandle.myUserId());
    }
}
