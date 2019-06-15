package com.oneplus.settings;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.System;
import android.util.Log;
import com.oneplus.display.ColorBalanceManager;

public class OneplusColorManager {
    private static final int OP_DCIP3_MODE_LEVEL = 8;
    private static final int OP_DEFAULT_MODE_LEVEL = 0;
    private static final int OP_REVERT_STATUS = 12;
    private static final int OP_SRGB_MODE_LEVEL = 7;
    private static final String OP_SYS_DCIP3_PROPERTY = "sys.dci3p";
    private static final String OP_SYS_SRGB_PROPERTY = "sys.srgb";
    private static final String TAG = "OneplusColorManager";
    private static ColorBalanceManager mCBM;
    private static OneplusColorManager mOneplusColorManager;
    private boolean isSupportReadingMode = this.mContext.getPackageManager().hasSystemFeature("oem.read_mode.support");
    private Context mContext;

    public static OneplusColorManager getInstance(Context context) {
        if (mOneplusColorManager == null) {
            mOneplusColorManager = new OneplusColorManager(context);
        }
        return mOneplusColorManager;
    }

    public OneplusColorManager(Context context) {
        this.mContext = context;
        if (mCBM == null) {
            mCBM = new ColorBalanceManager(this.mContext);
        }
    }

    public void setNotActivetNightMode() {
        SystemProperties.set(OP_SYS_SRGB_PROPERTY, "0");
        if (mCBM != null) {
            mCBM.setActiveMode(0);
            mCBM.setDefaultMode(0);
        }
    }

    public void setActivetNightMode() {
        SystemProperties.set(OP_SYS_SRGB_PROPERTY, "1");
        if (mCBM != null) {
            mCBM.setActiveMode(1);
            mCBM.setDefaultMode(1);
        }
    }

    public void setColorBalance(int value) {
        if (mCBM != null) {
            mCBM.setColorBalance(value);
        }
    }

    private void saveColorManagerMode() {
        if (mCBM != null) {
            mCBM.sendMsg(4);
        }
    }

    public void saveScreenBetter() {
        saveColorManagerMode();
    }

    public void restoreScreenBetterDisplay() {
        if (mCBM != null) {
            Log.d(TAG, "orestoreScreenBetterDisplay");
            mCBM.setColorBalance(100 - System.getInt(this.mContext.getContentResolver(), "oem_screen_better_value", 43));
        }
        saveColorManagerMode();
    }

    public void setsRGB() {
        if (mCBM != null) {
            mCBM.setActiveMode(0);
            mCBM.setDefaultMode(0);
        }
        SystemProperties.set(OP_SYS_SRGB_PROPERTY, "1");
    }

    public void closesRGB() {
        SystemProperties.set(OP_SYS_SRGB_PROPERTY, "0");
        mCBM.setActiveMode(0);
        mCBM.setDefaultMode(0);
    }

    public void setDciP3() {
        if (mCBM != null) {
            mCBM.setActiveMode(0);
            mCBM.setDefaultMode(0);
        }
        SystemProperties.set(OP_SYS_DCIP3_PROPERTY, "1");
    }

    public void closeDciP3() {
        SystemProperties.set(OP_SYS_DCIP3_PROPERTY, "0");
        mCBM.setActiveMode(0);
        mCBM.setDefaultMode(0);
    }

    public void setNightModeLevel(int value) {
        if (mCBM != null) {
            mCBM.setActiveMode(value);
            mCBM.setDefaultMode(value);
        }
    }

    public void resetScreenBetterDisplay() {
        if (mCBM != null) {
            if (this.isSupportReadingMode) {
                mCBM.setActiveMode(0);
                mCBM.setColorBalance(43);
            } else {
                mCBM.setActiveMode(0);
            }
            saveColorManagerMode();
        }
    }

    public void setActiveMode(int value) {
        if (mCBM != null) {
            mCBM.setActiveMode(value);
        }
    }

    public void revertStatus() {
        if (mCBM != null) {
            mCBM.sendMsg(12);
        }
    }

    public void releaseColorManager() {
    }
}
