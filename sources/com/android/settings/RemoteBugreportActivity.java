package com.android.settings;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.util.Log;

public class RemoteBugreportActivity extends Activity {
    private static final String TAG = "RemoteBugreportActivity";

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int notificationType = getIntent().getIntExtra("android.app.extra.bugreport_notification_type", -1);
        if (notificationType == 2) {
            new Builder(this).setMessage(R.string.sharing_remote_bugreport_dialog_message).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    RemoteBugreportActivity.this.finish();
                }
            }).setNegativeButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RemoteBugreportActivity.this.finish();
                }
            }).create().show();
        } else if (notificationType == 1 || notificationType == 3) {
            int i;
            Builder title = new Builder(this).setTitle(R.string.share_remote_bugreport_dialog_title);
            if (notificationType == 1) {
                i = R.string.share_remote_bugreport_dialog_message;
            } else {
                i = R.string.share_remote_bugreport_dialog_message_finished;
            }
            title.setMessage(i).setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    RemoteBugreportActivity.this.finish();
                }
            }).setNegativeButton(R.string.decline_remote_bugreport_action, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RemoteBugreportActivity.this.sendBroadcastAsUser(new Intent("com.android.server.action.REMOTE_BUGREPORT_SHARING_DECLINED"), UserHandle.SYSTEM, "android.permission.DUMP");
                    RemoteBugreportActivity.this.finish();
                }
            }).setPositiveButton(R.string.share_remote_bugreport_action, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    RemoteBugreportActivity.this.sendBroadcastAsUser(new Intent("com.android.server.action.REMOTE_BUGREPORT_SHARING_ACCEPTED"), UserHandle.SYSTEM, "android.permission.DUMP");
                    RemoteBugreportActivity.this.finish();
                }
            }).create().show();
        } else {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Incorrect dialog type, no dialog shown. Received: ");
            stringBuilder.append(notificationType);
            Log.e(str, stringBuilder.toString());
        }
    }
}
