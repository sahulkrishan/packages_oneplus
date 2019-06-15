package com.android.settings.system;

import android.content.Context;
import android.content.Intent;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.SystemUpdateManager;
import android.os.UserManager;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.CarrierConfigManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.core.BasePreferenceController;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class SystemUpdatePreferenceController extends BasePreferenceController {
    private static final String KEY_SYSTEM_UPDATE_SETTINGS = "system_update_settings";
    private static final String TAG = "SysUpdatePrefContr";
    private final UserManager mUm;
    private final SystemUpdateManager mUpdateManager;

    public SystemUpdatePreferenceController(Context context) {
        super(context, KEY_SYSTEM_UPDATE_SETTINGS);
        this.mUm = UserManager.get(context);
        this.mUpdateManager = (SystemUpdateManager) context.getSystemService("system_update");
    }

    public int getAvailabilityStatus() {
        if (this.mContext.getResources().getBoolean(R.bool.config_show_system_update_settings) && this.mContext.getResources().getBoolean(R.bool.config_use_gota) && this.mUm.isAdminUser()) {
            return 0;
        }
        return 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            Utils.updatePreferenceToSpecificActivityOrRemove(this.mContext, screen, getPreferenceKey(), 1);
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (TextUtils.equals(getPreferenceKey(), preference.getKey())) {
            PersistableBundle b = ((CarrierConfigManager) this.mContext.getSystemService("carrier_config")).getConfig();
            if (b != null && b.getBoolean("ci_action_on_sys_update_bool")) {
                ciActionOnSysUpdate(b);
            }
        }
        return false;
    }

    public CharSequence getSummary() {
        CharSequence summary = this.mContext.getString(R.string.android_version_summary, new Object[]{VERSION.RELEASE});
        FutureTask<Bundle> bundleFutureTask = new FutureTask(new -$$Lambda$SystemUpdatePreferenceController$XHnSEfghEOzLX1wZid9rCEinHuU(this));
        try {
            bundleFutureTask.run();
            Bundle updateInfo = (Bundle) bundleFutureTask.get();
            switch (updateInfo.getInt(NotificationCompat.CATEGORY_STATUS)) {
                case 0:
                    Log.d(TAG, "Update statue unknown");
                    break;
                case 1:
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    summary = this.mContext.getText(R.string.android_version_pending_update_summary);
                    break;
            }
            if (!TextUtils.isEmpty(updateInfo.getString("title"))) {
                summary = this.mContext.getString(R.string.android_version_summary, new Object[]{version});
            }
            return summary;
        } catch (InterruptedException | ExecutionException e) {
            Log.w(TAG, "Error getting system update info.");
            return summary;
        }
    }

    private void ciActionOnSysUpdate(PersistableBundle b) {
        String intentStr = b.getString("ci_action_on_sys_update_intent_string");
        if (!TextUtils.isEmpty(intentStr)) {
            String extra = b.getString("ci_action_on_sys_update_extra_string");
            String extraVal = b.getString("ci_action_on_sys_update_extra_val_string");
            Intent intent = new Intent(intentStr);
            if (!TextUtils.isEmpty(extra)) {
                intent.putExtra(extra, extraVal);
            }
            String str = TAG;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("ciActionOnSysUpdate: broadcasting intent ");
            stringBuilder.append(intentStr);
            stringBuilder.append(" with extra ");
            stringBuilder.append(extra);
            stringBuilder.append(", ");
            stringBuilder.append(extraVal);
            Log.d(str, stringBuilder.toString());
            this.mContext.getApplicationContext().sendBroadcast(intent);
        }
    }
}
