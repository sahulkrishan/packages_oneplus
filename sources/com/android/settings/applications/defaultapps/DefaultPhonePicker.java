package com.android.settings.applications.defaultapps;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.telecom.DefaultDialerManager;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import com.android.settings.R;
import com.android.settingslib.applications.DefaultAppInfo;
import java.util.ArrayList;
import java.util.List;

public class DefaultPhonePicker extends DefaultAppPickerFragment {
    private DefaultKeyUpdater mDefaultKeyUpdater;

    static class DefaultKeyUpdater {
        private final TelecomManager mTelecomManager;

        public DefaultKeyUpdater(TelecomManager telecomManager) {
            this.mTelecomManager = telecomManager;
        }

        public String getSystemDialerPackage() {
            return this.mTelecomManager.getSystemDialerPackage();
        }

        public String getDefaultDialerApplication(Context context, int uid) {
            return DefaultDialerManager.getDefaultDialerApplication(context, uid);
        }

        public boolean setDefaultDialerApplication(Context context, String key, int uid) {
            return DefaultDialerManager.setDefaultDialerApplication(context, key, uid);
        }
    }

    public int getMetricsCategory() {
        return 788;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.mDefaultKeyUpdater = new DefaultKeyUpdater((TelecomManager) context.getSystemService("telecom"));
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_phone_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        List<DefaultAppInfo> candidates = new ArrayList();
        List<String> dialerPackages = DefaultDialerManager.getInstalledDialerApplications(getContext(), this.mUserId);
        Context context = getContext();
        for (String packageName : dialerPackages) {
            try {
                candidates.add(new DefaultAppInfo(context, this.mPm, this.mPm.getApplicationInfoAsUser(packageName, 0, this.mUserId)));
            } catch (NameNotFoundException e) {
            }
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return this.mDefaultKeyUpdater.getDefaultDialerApplication(getContext(), this.mUserId);
    }

    /* Access modifiers changed, original: protected */
    public String getSystemDefaultKey() {
        return this.mDefaultKeyUpdater.getSystemDialerPackage();
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        if (TextUtils.isEmpty(key) || TextUtils.equals(key, getDefaultKey())) {
            return false;
        }
        return this.mDefaultKeyUpdater.setDefaultDialerApplication(getContext(), key, this.mUserId);
    }
}
