package com.android.settings.fuelgauge;

import android.content.Context;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import com.android.internal.os.BatteryStatsHelper;
import com.android.settingslib.utils.AsyncLoader;

public class BatteryStatsHelperLoader extends AsyncLoader<BatteryStatsHelper> {
    @VisibleForTesting
    BatteryUtils mBatteryUtils;
    @VisibleForTesting
    UserManager mUserManager;

    public BatteryStatsHelperLoader(Context context) {
        super(context);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mBatteryUtils = BatteryUtils.getInstance(context);
    }

    public BatteryStatsHelper loadInBackground() {
        BatteryStatsHelper statsHelper = new BatteryStatsHelper(getContext(), true);
        this.mBatteryUtils.initBatteryStatsHelper(statsHelper, null, this.mUserManager);
        return statsHelper;
    }

    /* Access modifiers changed, original: protected */
    public void onDiscardResult(BatteryStatsHelper result) {
    }
}
