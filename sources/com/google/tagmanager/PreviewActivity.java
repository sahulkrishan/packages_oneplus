package com.google.tagmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class PreviewActivity extends Activity {
    public void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            Log.i("Preview activity");
            Uri data = getIntent().getData();
            if (!TagManager.getInstance(this).setPreviewData(data)) {
                String message = new StringBuilder();
                message.append("Cannot preview the app with the uri: ");
                message.append(data);
                message.append(". Launching current version instead.");
                message = message.toString();
                Log.w(message);
                displayAlert("Preview failure", message, "Continue");
            }
            Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
            StringBuilder stringBuilder;
            if (intent != null) {
                stringBuilder = new StringBuilder();
                stringBuilder.append("Invoke the launch activity for package name: ");
                stringBuilder.append(getPackageName());
                Log.i(stringBuilder.toString());
                startActivity(intent);
                return;
            }
            stringBuilder = new StringBuilder();
            stringBuilder.append("No launch activity found for package name: ");
            stringBuilder.append(getPackageName());
            Log.i(stringBuilder.toString());
        } catch (Exception e) {
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Calling preview threw an exception: ");
            stringBuilder2.append(e.getMessage());
            Log.e(stringBuilder2.toString());
        }
    }

    private void displayAlert(String title, String message, String buttonLabel) {
        AlertDialog alertDialog = new Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(-1, buttonLabel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.show();
    }
}
