package com.oneplus.settings.system;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;
import java.lang.reflect.Method;

public class OPCarrierUiccUnlockController extends BasePreferenceController {
    private static final String KEY_UICC_UNLOCK = "uicc_unlock";
    private static final String TAG = "OPCarrierUiccUnlockCont";
    private Context mContext;
    private Preference unlockPreference;

    public OPCarrierUiccUnlockController(Context context) {
        super(context, KEY_UICC_UNLOCK);
        this.mContext = context;
    }

    public int getAvailabilityStatus() {
        return 0;
    }

    public String getPreferenceKey() {
        return KEY_UICC_UNLOCK;
    }

    public void updateState(Preference preference) {
        if (OPUtils.isSupportUss()) {
            if (this.unlockPreference != null) {
                this.unlockPreference.setSummary(getSummary());
            }
            Log.d(TAG, "updateState");
        }
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        this.unlockPreference = screen.findPreference(getPreferenceKey());
    }

    public String getSummary() {
        if (getSimLockStatus() == 0) {
            return this.mContext.getResources().getString(R.string.uicc_unlock_summary);
        }
        if (TextUtils.equals(getType(), "1")) {
            return this.mContext.getResources().getString(R.string.uicc_all_lock_summary);
        }
        return this.mContext.getResources().getString(R.string.uicc_lock_summary);
    }

    public String getType() {
        String type = "";
        type = queryParamstore("value");
        if (TextUtils.isEmpty(type)) {
            return queryParamstore("defaultvalue");
        }
        return type;
    }

    public String queryParamstore(String item) {
        String value = "";
        Uri uri = Uri.parse("content://com.redbend.app.provider");
        try {
            Cursor cursor = this.mContext.getContentResolver().query(uri, null, null, new String[]{"sim_rssb_indicator", item, "0"}, null);
            if (cursor != null && cursor.getCount() == 1 && cursor.moveToFirst()) {
                value = cursor.getString(0);
                cursor.close();
            }
            return value;
        } catch (Exception e) {
            e.printStackTrace();
            return value;
        }
    }

    private int getSimLockStatus() {
        try {
            Class clazz = Class.forName("com.oneplus.android.telephony.OPSprintReqManager");
            Method defaultMethod = clazz.getDeclaredMethod("getDefault", new Class[0]);
            defaultMethod.setAccessible(true);
            return ((Integer) clazz.getDeclaredMethod("getSimLockStatus", new Class[0]).invoke(defaultMethod.invoke(clazz.newInstance(), new Object[0]), new Object[0])).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }
}
