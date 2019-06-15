package com.android.settingslib.core.instrumentation;

public interface Instrumentable {
    public static final int METRICS_CATEGORY_UNKNOWN = 0;

    int getMetricsCategory();
}
