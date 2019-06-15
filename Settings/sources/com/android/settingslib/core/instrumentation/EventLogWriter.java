package com.android.settingslib.core.instrumentation;

import android.content.Context;
import android.metrics.LogMaker;
import android.util.Pair;
import com.android.internal.logging.MetricsLogger;

public class EventLogWriter implements LogWriter {
    private final MetricsLogger mMetricsLogger = new MetricsLogger();

    public void visible(Context context, int source, int category) {
        MetricsLogger.action(new LogMaker(category).setType(1).addTaggedData(833, Integer.valueOf(source)));
    }

    public void hidden(Context context, int category) {
        MetricsLogger.hidden(context, category);
    }

    public void action(int category, int value, Pair<Integer, Object>... taggedData) {
        if (taggedData == null || taggedData.length == 0) {
            this.mMetricsLogger.action(category, value);
            return;
        }
        LogMaker logMaker = new LogMaker(category).setType(4).setSubtype(value);
        for (Pair<Integer, Object> pair : taggedData) {
            logMaker.addTaggedData(((Integer) pair.first).intValue(), pair.second);
        }
        this.mMetricsLogger.write(logMaker);
    }

    public void action(int category, boolean value, Pair<Integer, Object>... taggedData) {
        action(category, (int) value, (Pair[]) taggedData);
    }

    public void action(Context context, int category, Pair<Integer, Object>... taggedData) {
        action(context, category, "", taggedData);
    }

    public void actionWithSource(Context context, int source, int category) {
        LogMaker logMaker = new LogMaker(category).setType(4);
        if (source != 0) {
            logMaker.addTaggedData(833, Integer.valueOf(source));
        }
        MetricsLogger.action(logMaker);
    }

    @Deprecated
    public void action(Context context, int category, int value) {
        MetricsLogger.action(context, category, value);
    }

    @Deprecated
    public void action(Context context, int category, boolean value) {
        MetricsLogger.action(context, category, value);
    }

    public void action(Context context, int category, String pkg, Pair<Integer, Object>... taggedData) {
        if (taggedData == null || taggedData.length == 0) {
            MetricsLogger.action(context, category, pkg);
            return;
        }
        LogMaker logMaker = new LogMaker(category).setType(4).setPackageName(pkg);
        for (Pair<Integer, Object> pair : taggedData) {
            logMaker.addTaggedData(((Integer) pair.first).intValue(), pair.second);
        }
        MetricsLogger.action(logMaker);
    }

    public void count(Context context, String name, int value) {
        MetricsLogger.count(context, name, value);
    }

    public void histogram(Context context, String name, int bucket) {
        MetricsLogger.histogram(context, name, bucket);
    }
}
