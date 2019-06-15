package com.android.settings;

import android.content.Context;
import android.content.SharedPreferences;

public class SmqSettings {
    public static final String SMQ_KEY_VALUE = "app_status";
    public static final String SMQ_PREFS_NAME = "smqpreferences";
    private Context mContext;
    private SharedPreferences mSmqPreferences = this.mContext.getSharedPreferences(SMQ_PREFS_NAME, 0);

    public SmqSettings(Context context) {
        this.mContext = context;
        new DBReadAsyncTask(this.mContext).execute(new Void[0]);
    }

    public boolean isShowSmqSettings() {
        boolean z = false;
        if (this.mSmqPreferences.getInt("app_status", 0) > 0) {
            z = true;
        }
        return z;
    }
}
