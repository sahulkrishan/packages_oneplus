package com.android.settingslib.core.instrumentation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Pair;
import java.util.ArrayList;
import java.util.List;

public class MetricsFeatureProvider {
    private List<LogWriter> mLoggerWriters = new ArrayList();

    public MetricsFeatureProvider() {
        installLogWriters();
    }

    /* Access modifiers changed, original: protected */
    public void installLogWriters() {
        this.mLoggerWriters.add(new EventLogWriter());
    }

    public void visible(Context context, int source, int category) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.visible(context, source, category);
        }
    }

    public void hidden(Context context, int category) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.hidden(context, category);
        }
    }

    public void actionWithSource(Context context, int source, int category) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.actionWithSource(context, source, category);
        }
    }

    public void action(VisibilityLoggerMixin visibilityLogger, int category, int value) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.action(category, value, sinceVisibleTaggedData(visibilityLogger.elapsedTimeSinceVisible()));
        }
    }

    public void action(VisibilityLoggerMixin visibilityLogger, int category, boolean value) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.action(category, value, sinceVisibleTaggedData(visibilityLogger.elapsedTimeSinceVisible()));
        }
    }

    public void action(Context context, int category, Pair<Integer, Object>... taggedData) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.action(context, category, (Pair[]) taggedData);
        }
    }

    @Deprecated
    public void action(Context context, int category, int value) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.action(context, category, value);
        }
    }

    @Deprecated
    public void action(Context context, int category, boolean value) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.action(context, category, value);
        }
    }

    public void action(Context context, int category, String pkg, Pair<Integer, Object>... taggedData) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.action(context, category, pkg, taggedData);
        }
    }

    public void count(Context context, String name, int value) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.count(context, name, value);
        }
    }

    public void histogram(Context context, String name, int bucket) {
        for (LogWriter writer : this.mLoggerWriters) {
            writer.histogram(context, name, bucket);
        }
    }

    public int getMetricsCategory(Object object) {
        if (object == null || !(object instanceof Instrumentable)) {
            return 0;
        }
        return ((Instrumentable) object).getMetricsCategory();
    }

    public void logDashboardStartIntent(Context context, Intent intent, int sourceMetricsCategory) {
        if (intent != null) {
            ComponentName cn = intent.getComponent();
            if (cn == null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    action(context, 830, action, Pair.create(Integer.valueOf(833), Integer.valueOf(sourceMetricsCategory)));
                }
            } else if (!TextUtils.equals(cn.getPackageName(), context.getPackageName())) {
                action(context, 830, cn.flattenToString(), Pair.create(Integer.valueOf(833), Integer.valueOf(sourceMetricsCategory)));
            }
        }
    }

    private Pair<Integer, Object> sinceVisibleTaggedData(long timestamp) {
        return Pair.create(Integer.valueOf(794), Long.valueOf(timestamp));
    }
}
