package com.android.settings.wifi.calling;

import android.content.Context;
import com.android.ims.ImsManager;
import com.android.settings.SettingsActivity;

public class WifiCallingSuggestionActivity extends SettingsActivity {
    public static boolean isSuggestionComplete(Context context) {
        boolean z = true;
        if (!ImsManager.isWfcEnabledByPlatform(context) || !ImsManager.isWfcProvisionedOnDevice(context)) {
            return true;
        }
        if (!(ImsManager.isWfcEnabledByUser(context) && ImsManager.isNonTtyOrTtyOnVolteEnabled(context))) {
            z = false;
        }
        return z;
    }
}
