package com.android.settings.datetime;

import android.content.Context;

public interface UpdateTimeAndDateCallback {
    public static final long MIN_DATE = 1194220800000L;

    void updateTimeAndDateDisplay(Context context);
}
