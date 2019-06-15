package com.android.settings.fuelgauge;

import android.os.BatteryStats.HistoryItem;

public class BatteryWifiParser extends BatteryFlagParser {
    public BatteryWifiParser(int accentColor) {
        super(accentColor, false, 0);
    }

    /* Access modifiers changed, original: protected */
    public boolean isSet(HistoryItem record) {
        int i = (record.states2 & 15) >> 0;
        switch (i) {
            case 0:
            case 1:
            case 2:
            case 3:
                break;
            default:
                switch (i) {
                    case 11:
                    case 12:
                        break;
                    default:
                        return true;
                }
        }
        return false;
    }
}
