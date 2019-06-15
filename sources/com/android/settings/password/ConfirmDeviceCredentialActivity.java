package com.android.settings.password;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;

public class ConfirmDeviceCredentialActivity extends Activity {
    public static final String TAG = ConfirmDeviceCredentialActivity.class.getSimpleName();

    public static class InternalActivity extends ConfirmDeviceCredentialActivity {
    }

    public static Intent createIntent(CharSequence title, CharSequence details) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra("android.app.extra.TITLE", title);
        intent.putExtra("android.app.extra.DESCRIPTION", details);
        return intent;
    }

    public static Intent createIntent(CharSequence title, CharSequence details, long challenge) {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", ConfirmDeviceCredentialActivity.class.getName());
        intent.putExtra("android.app.extra.TITLE", title);
        intent.putExtra("android.app.extra.DESCRIPTION", details);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE, challenge);
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_HAS_CHALLENGE, true);
        return intent;
    }

    /* JADX WARNING: Removed duplicated region for block: B:14:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x005f  */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x009e  */
    public void onCreate(android.os.Bundle r21) {
        /*
        r20 = this;
        r1 = r20;
        super.onCreate(r21);
        r2 = r20.getIntent();
        r0 = "android.app.extra.TITLE";
        r3 = r2.getStringExtra(r0);
        r0 = "android.app.extra.DESCRIPTION";
        r13 = r2.getStringExtra(r0);
        r0 = "android.app.extra.ALTERNATE_BUTTON_LABEL";
        r14 = r2.getStringExtra(r0);
        r0 = "android.app.action.CONFIRM_FRP_CREDENTIAL";
        r4 = r2.getAction();
        r15 = r0.equals(r4);
        r4 = com.android.settings.Utils.getCredentialOwnerUserId(r20);
        r0 = r20.isInternalActivity();
        if (r0 == 0) goto L_0x0040;
    L_0x002f:
        r0 = r2.getExtras();	 Catch:{ SecurityException -> 0x0038 }
        r0 = com.android.settings.Utils.getUserIdFromBundle(r1, r0);	 Catch:{ SecurityException -> 0x0038 }
        goto L_0x0041;
    L_0x0038:
        r0 = move-exception;
        r5 = TAG;
        r6 = "Invalid intent extra";
        android.util.Log.e(r5, r6, r0);
    L_0x0040:
        r0 = r4;
    L_0x0041:
        r4 = android.os.UserManager.get(r20);
        r16 = r4.isManagedProfile(r0);
        if (r3 != 0) goto L_0x0051;
    L_0x004b:
        if (r16 == 0) goto L_0x0051;
    L_0x004d:
        r3 = r1.getTitleFromOrganizationName(r0);
    L_0x0051:
        r4 = new com.android.settings.password.ChooseLockSettingsHelper;
        r4.<init>(r1);
        r12 = r4;
        r4 = new com.android.internal.widget.LockPatternUtils;
        r4.<init>(r1);
        r10 = r4;
        if (r15 == 0) goto L_0x0069;
    L_0x005f:
        r4 = 0;
        r4 = r12.launchFrpConfirmationActivity(r4, r3, r13, r14);
        r19 = r10;
        r17 = r12;
        goto L_0x009c;
    L_0x0069:
        if (r16 == 0) goto L_0x008b;
    L_0x006b:
        r4 = r20.isInternalActivity();
        if (r4 == 0) goto L_0x008b;
    L_0x0071:
        r4 = r10.isSeparateProfileChallengeEnabled(r0);
        if (r4 != 0) goto L_0x008b;
    L_0x0077:
        r5 = 0;
        r6 = 0;
        r9 = 1;
        r17 = 0;
        r4 = r12;
        r7 = r3;
        r8 = r13;
        r19 = r10;
        r10 = r17;
        r17 = r12;
        r12 = r0;
        r4 = r4.launchConfirmationActivityWithExternalAndChallenge(r5, r6, r7, r8, r9, r10, r12);
        goto L_0x009c;
    L_0x008b:
        r19 = r10;
        r17 = r12;
        r5 = 0;
        r6 = 0;
        r9 = 0;
        r10 = 1;
        r4 = r17;
        r7 = r3;
        r8 = r13;
        r11 = r0;
        r4 = r4.launchConfirmationActivity(r5, r6, r7, r8, r9, r10, r11);
    L_0x009c:
        if (r4 != 0) goto L_0x00a9;
    L_0x009e:
        r5 = TAG;
        r6 = "No pattern, password or PIN set.";
        android.util.Log.d(r5, r6);
        r5 = -1;
        r1.setResult(r5);
    L_0x00a9:
        r20.finish();
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.password.ConfirmDeviceCredentialActivity.onCreate(android.os.Bundle):void");
    }

    private boolean isInternalActivity() {
        return this instanceof InternalActivity;
    }

    private String getTitleFromOrganizationName(int userId) {
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService("device_policy");
        CharSequence organizationNameForUser = dpm != null ? dpm.getOrganizationNameForUser(userId) : null;
        if (organizationNameForUser != null) {
            return organizationNameForUser.toString();
        }
        return null;
    }
}
