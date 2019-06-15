package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.UserHandle;
import android.util.Log;

public class TetherProvisioningActivity extends Activity {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final String EXTRA_TETHER_TYPE = "TETHER_TYPE";
    private static final int PROVISION_REQUEST = 0;
    private static final String TAG = "TetherProvisioningAct";
    private ResultReceiver mResultReceiver;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mResultReceiver = (ResultReceiver) getIntent().getParcelableExtra("extraProvisionCallback");
        int tetherType = getIntent().getIntExtra("extraAddTetherType", -1);
        String[] provisionApp = getResources().getStringArray(17236023);
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClassName(provisionApp[0], provisionApp[1]);
        intent.putExtra(EXTRA_TETHER_TYPE, tetherType);
        if (DEBUG) {
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Starting provisioning app: ");
            stringBuilder.append(provisionApp[0]);
            stringBuilder.append(".");
            stringBuilder.append(provisionApp[1]);
            Log.d(str, stringBuilder.toString());
        }
        if (getPackageManager().queryIntentActivities(intent, 65536).isEmpty()) {
            Log.e(TAG, "Provisioning app is configured, but not available.");
            this.mResultReceiver.send(11, null);
            finish();
            return;
        }
        startActivityForResultAsUser(intent, 0, UserHandle.CURRENT);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 0) {
            int result;
            if (DEBUG) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Got result from app: ");
                stringBuilder.append(resultCode);
                Log.d(str, stringBuilder.toString());
            }
            if (resultCode == -1) {
                result = 0;
            } else {
                result = 11;
            }
            this.mResultReceiver.send(result, null);
            finish();
        }
    }
}
