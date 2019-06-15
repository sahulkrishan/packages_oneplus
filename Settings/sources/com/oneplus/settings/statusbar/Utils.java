package com.oneplus.settings.statusbar;

import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings.Secure;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;

public class Utils {
    public static final String CLOCK_SECONDS = "clock_seconds";
    public static final String ICON_BLACKLIST = "icon_blacklist";
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrentUser;

    public Utils(Context context) {
        this.mContext = context;
        if (this.mContext != null) {
            this.mContentResolver = this.mContext.getContentResolver();
        }
        this.mCurrentUser = ActivityManager.getCurrentUser();
    }

    public static ArraySet<String> getIconBlacklist(String blackListStr) {
        ArraySet<String> ret = new ArraySet();
        if (blackListStr == null) {
            blackListStr = "rotate,networkspeed";
            TelephonyManager mTM = TelephonyManager.getDefault();
            if (mTM != null && TextUtils.equals(mTM.getSimOperatorNumeric(SubscriptionManager.getDefaultDataSubscriptionId()), "23410")) {
                Log.d("Utils", "O2 UK sim, add volte/vowifi to blacklist by default");
                blackListStr = "rotate,networkspeed,volte,vowifi";
            }
        }
        for (String slot : blackListStr.split(",")) {
            if (!TextUtils.isEmpty(slot)) {
                ret.add(slot);
            }
        }
        return ret;
    }

    public String getValue(String setting) {
        return Secure.getStringForUser(this.mContentResolver, setting, this.mCurrentUser);
    }

    public void setValue(String setting, String value) {
        Secure.putStringForUser(this.mContentResolver, setting, value, this.mCurrentUser);
    }

    public int getValue(String setting, int def) {
        return Secure.getIntForUser(this.mContentResolver, setting, def, this.mCurrentUser);
    }

    public void setValue(String setting, int value) {
        Secure.putIntForUser(this.mContentResolver, setting, value, this.mCurrentUser);
    }
}
