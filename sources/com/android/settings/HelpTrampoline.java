package com.android.settings;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.android.settingslib.HelpUtils;

public class HelpTrampoline extends Activity {
    private static final String TAG = "HelpTrampoline";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            String name = getIntent().getStringExtra("android.intent.extra.TEXT");
            if (TextUtils.isEmpty(name)) {
                finishAndRemoveTask();
                return;
            }
            Intent intent = HelpUtils.getHelpIntent(this, getResources().getString(getResources().getIdentifier(name, "string", getPackageName())), null);
            if (intent != null) {
                startActivityForResult(intent, 0);
            }
            finish();
        } catch (ActivityNotFoundException | NotFoundException e) {
            Log.w(TAG, "Failed to resolve help", e);
        }
    }
}
