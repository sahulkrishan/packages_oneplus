package com.android.settings.development;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.IStorageManager;
import android.os.storage.IStorageManager.Stub;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.development.DeveloperOptionsPreferenceController;

public class FileEncryptionPreferenceController extends DeveloperOptionsPreferenceController implements PreferenceControllerMixin {
    @VisibleForTesting
    static final String FILE_ENCRYPTION_PROPERTY_KEY = "ro.crypto.type";
    private static final String KEY_CONVERT_FBE = "convert_to_file_encryption";
    private static final String KEY_STORAGE_MANAGER = "mount";
    private final IStorageManager mStorageManager = getStorageManager();

    public FileEncryptionPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        if (this.mStorageManager == null) {
            return false;
        }
        try {
            return this.mStorageManager.isConvertibleToFBE();
        } catch (RemoteException e) {
            return false;
        }
    }

    public String getPreferenceKey() {
        return KEY_CONVERT_FBE;
    }

    public void updateState(Preference preference) {
        if (TextUtils.equals("file", SystemProperties.get(FILE_ENCRYPTION_PROPERTY_KEY, "none"))) {
            this.mPreference.setEnabled(false);
            this.mPreference.setSummary(this.mContext.getResources().getString(R.string.convert_to_file_encryption_done));
        }
    }

    private IStorageManager getStorageManager() {
        try {
            return Stub.asInterface(ServiceManager.getService(KEY_STORAGE_MANAGER));
        } catch (VerifyError e) {
            return null;
        }
    }
}
