package com.oneplus.settings.packageuninstaller;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDeleteObserver2.Stub;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.android.settings.R;

public class UninstallUninstalling extends Activity implements EventResultObserver {
    private static final String BROADCAST_ACTION = "com.android.packageinstaller.ACTION_UNINSTALL_COMMIT";
    static final String EXTRA_APP_LABEL = "com.android.packageinstaller.extra.APP_LABEL";
    private static final String LOG_TAG = UninstallUninstalling.class.getSimpleName();
    private static final String UNINSTALL_ID = "com.android.packageinstaller.UNINSTALL_ID";
    private ApplicationInfo mAppInfo;
    private IBinder mCallback;
    private String mLabel;
    private boolean mReturnResult;
    private int mUninstallId;

    public static class UninstallUninstallingFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder dialogBuilder = new Builder(getActivity());
            dialogBuilder.setCancelable(false);
            dialogBuilder.setMessage(getActivity().getString(R.string.uninstalling, new Object[]{((UninstallUninstalling) getActivity()).mLabel}));
            Dialog dialog = dialogBuilder.create();
            dialog.setCanceledOnTouchOutside(false);
            return dialog;
        }
    }

    /* Access modifiers changed, original: protected */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x00d0 A:{Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0 }, ExcHandler: OutOfIdsException | IllegalArgumentException (r0_4 'e' java.lang.Exception A:{Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0 }}), Splitter:B:2:0x0040} */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing block: B:18:0x00d0, code skipped:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x00e0, code skipped:
            android.util.Log.e(LOG_TAG, "Fails to start uninstall", r0);
            onResult(1, -1, null);
     */
    /* JADX WARNING: Missing block: B:22:?, code skipped:
            return;
     */
    public void onCreate(@android.support.annotation.Nullable android.os.Bundle r19) {
        /*
        r18 = this;
        r1 = r18;
        r2 = r19;
        super.onCreate(r19);
        r0 = 0;
        r1.setFinishOnTouchOutside(r0);
        r3 = r18.getIntent();
        r4 = "com.android.packageinstaller.applicationInfo";
        r3 = r3.getParcelableExtra(r4);
        r3 = (android.content.pm.ApplicationInfo) r3;
        r1.mAppInfo = r3;
        r3 = r18.getIntent();
        r4 = "android.content.pm.extra.CALLBACK";
        r3 = r3.getIBinderExtra(r4);
        r1.mCallback = r3;
        r3 = r18.getIntent();
        r4 = "android.intent.extra.RETURN_RESULT";
        r3 = r3.getBooleanExtra(r4, r0);
        r1.mReturnResult = r3;
        r3 = r18.getIntent();
        r4 = "com.android.packageinstaller.extra.APP_LABEL";
        r3 = r3.getStringExtra(r4);
        r1.mLabel = r3;
        r3 = -1;
        if (r2 != 0) goto L_0x00d2;
    L_0x0040:
        r4 = r18.getIntent();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r5 = "android.intent.extra.UNINSTALL_ALL_USERS";
        r4 = r4.getBooleanExtra(r5, r0);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r5 = r18.getIntent();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r6 = "android.intent.extra.USER";
        r5 = r5.getParcelableExtra(r6);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r5 = (android.os.UserHandle) r5;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r6 = r18.getFragmentManager();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r6 = r6.beginTransaction();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r7 = r18.getFragmentManager();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r8 = "dialog";
        r7 = r7.findFragmentByTag(r8);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        if (r7 == 0) goto L_0x006d;
    L_0x006a:
        r6.remove(r7);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
    L_0x006d:
        r8 = new com.oneplus.settings.packageuninstaller.UninstallUninstalling$UninstallUninstallingFragment;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r8.<init>();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r8.setCancelable(r0);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r9 = "dialog";
        r8.show(r6, r9);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r9 = -2147483648; // 0xffffffff80000000 float:-0.0 double:NaN;
        r9 = com.oneplus.settings.packageuninstaller.UninstallEventReceiver.addObserver(r1, r9, r1);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r1.mUninstallId = r9;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r9 = new android.content.Intent;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r10 = "com.android.packageinstaller.ACTION_UNINSTALL_COMMIT";
        r9.<init>(r10);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r10 = 268435456; // 0x10000000 float:2.5243549E-29 double:1.32624737E-315;
        r9.setFlags(r10);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r10 = "EventResultPersister.EXTRA_ID";
        r11 = r1.mUninstallId;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r9.putExtra(r10, r11);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r10 = r18.getPackageName();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r9.setPackage(r10);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r10 = r1.mUninstallId;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r11 = 134217728; // 0x8000000 float:3.85186E-34 double:6.63123685E-316;
        r10 = android.app.PendingIntent.getBroadcast(r1, r10, r9, r11);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r11 = android.app.ActivityThread.getPackageManager();	 Catch:{ RemoteException -> 0x00cb }
        r12 = r11.getPackageInstaller();	 Catch:{ RemoteException -> 0x00cb }
        r13 = new android.content.pm.VersionedPackage;	 Catch:{ RemoteException -> 0x00cb }
        r11 = r1.mAppInfo;	 Catch:{ RemoteException -> 0x00cb }
        r11 = r11.packageName;	 Catch:{ RemoteException -> 0x00cb }
        r13.<init>(r11, r3);	 Catch:{ RemoteException -> 0x00cb }
        r14 = r18.getPackageName();	 Catch:{ RemoteException -> 0x00cb }
        if (r4 == 0) goto L_0x00be;
    L_0x00bb:
        r0 = 2;
    L_0x00bc:
        r15 = r0;
        goto L_0x00bf;
    L_0x00be:
        goto L_0x00bc;
    L_0x00bf:
        r16 = r10.getIntentSender();	 Catch:{ RemoteException -> 0x00cb }
        r17 = r5.getIdentifier();	 Catch:{ RemoteException -> 0x00cb }
        r12.uninstall(r13, r14, r15, r16, r17);	 Catch:{ RemoteException -> 0x00cb }
        goto L_0x00cf;
    L_0x00cb:
        r0 = move-exception;
        r0.rethrowFromSystemServer();	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
    L_0x00cf:
        goto L_0x00df;
    L_0x00d0:
        r0 = move-exception;
        goto L_0x00e0;
    L_0x00d2:
        r0 = "com.android.packageinstaller.UNINSTALL_ID";
        r0 = r2.getInt(r0);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r1.mUninstallId = r0;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        r0 = r1.mUninstallId;	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
        com.oneplus.settings.packageuninstaller.UninstallEventReceiver.addObserver(r1, r0, r1);	 Catch:{ OutOfIdsException | IllegalArgumentException -> 0x00d0, OutOfIdsException | IllegalArgumentException -> 0x00d0 }
    L_0x00df:
        goto L_0x00ed;
        r4 = LOG_TAG;
        r5 = "Fails to start uninstall";
        android.util.Log.e(r4, r5, r0);
        r4 = 1;
        r5 = 0;
        r1.onResult(r4, r3, r5);
    L_0x00ed:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oneplus.settings.packageuninstaller.UninstallUninstalling.onCreate(android.os.Bundle):void");
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(UNINSTALL_ID, this.mUninstallId);
    }

    public void onBackPressed() {
    }

    public void onResult(int status, int legacyStatus, @Nullable String message) {
        if (this.mCallback != null) {
            try {
                Stub.asInterface(this.mCallback).onPackageDeleted(this.mAppInfo.packageName, legacyStatus, message);
            } catch (RemoteException e) {
            }
        } else {
            int i = 1;
            if (this.mReturnResult) {
                Intent result = new Intent();
                result.putExtra("android.intent.extra.INSTALL_RESULT", legacyStatus);
                if (status == 0) {
                    i = -1;
                }
                setResult(i, result);
            } else if (status != 0) {
                Toast.makeText(this, getString(R.string.uninstall_failed_app, new Object[]{this.mLabel}), 1).show();
            }
        }
        finish();
    }

    /* Access modifiers changed, original: protected */
    public void onDestroy() {
        UninstallEventReceiver.removeObserver(this, this.mUninstallId);
        super.onDestroy();
    }
}
