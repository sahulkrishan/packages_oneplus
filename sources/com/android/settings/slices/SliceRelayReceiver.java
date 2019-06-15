package com.android.settings.slices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import com.android.settingslib.SliceBroadcastRelay;

public class SliceRelayReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        String uriString = intent.getStringExtra(SliceBroadcastRelay.EXTRA_URI);
        if (!TextUtils.isEmpty(uriString)) {
            context.getContentResolver().notifyChange(Uri.parse(uriString), null);
        }
    }
}
