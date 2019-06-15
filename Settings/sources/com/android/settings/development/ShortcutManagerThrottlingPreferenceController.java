package com.android.settings.development;

import android.content.Context;
import android.content.pm.IShortcutService;
import android.content.pm.IShortcutService.Stub;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.inputmethod.UserDictionaryAddWordContents;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class ShortcutManagerThrottlingPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    private static final String SHORTCUT_MANAGER_RESET_KEY = "reset_shortcut_manager_throttling";
    private static final String TAG = "ShortcutMgrPrefCtrl";
    private final IShortcutService mShortcutService = getShortCutService();

    public ShortcutManagerThrottlingPreferenceController(Context context) {
        super(context);
    }

    public String getPreferenceKey() {
        return SHORTCUT_MANAGER_RESET_KEY;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(SHORTCUT_MANAGER_RESET_KEY, preference.getKey())) {
            return false;
        }
        resetShortcutManagerThrottling();
        return true;
    }

    private void resetShortcutManagerThrottling() {
        if (this.mShortcutService != null) {
            try {
                this.mShortcutService.resetThrottling();
                Toast.makeText(this.mContext, R.string.reset_shortcut_manager_throttling_complete, 0).show();
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to reset rate limiting", e);
            }
        }
    }

    private IShortcutService getShortCutService() {
        try {
            return Stub.asInterface(ServiceManager.getService(UserDictionaryAddWordContents.EXTRA_SHORTCUT));
        } catch (VerifyError e) {
            return null;
        }
    }
}
