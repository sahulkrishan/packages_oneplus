package com.android.settingslib.core.instrumentation;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class SharedPreferencesLogger implements SharedPreferences {
    private static final String LOG_TAG = "SharedPreferencesLogger";
    private final Context mContext;
    private final MetricsFeatureProvider mMetricsFeature;
    private final Set<String> mPreferenceKeySet = new ConcurrentSkipListSet();
    private final String mTag;

    private class AsyncPackageCheck extends AsyncTask<String, Void, Void> {
        private AsyncPackageCheck() {
        }

        /* Access modifiers changed, original: protected|varargs */
        public Void doInBackground(String... params) {
            String key = params[null];
            String value = params[1];
            PackageManager pm = SharedPreferencesLogger.this.mContext.getPackageManager();
            try {
                ComponentName name = ComponentName.unflattenFromString(value);
                if (value != null) {
                    value = name.getPackageName();
                }
            } catch (Exception e) {
            }
            try {
                pm.getPackageInfo(value, 4194304);
                SharedPreferencesLogger.this.logPackageName(key, value);
            } catch (NameNotFoundException e2) {
                SharedPreferencesLogger.this.logValue(key, value, true);
            }
            return null;
        }
    }

    public class EditorLogger implements Editor {
        public Editor putString(String key, String value) {
            SharedPreferencesLogger.this.safeLogValue(key, value);
            return this;
        }

        public Editor putStringSet(String key, Set<String> values) {
            SharedPreferencesLogger.this.safeLogValue(key, TextUtils.join(",", values));
            return this;
        }

        public Editor putInt(String key, int value) {
            SharedPreferencesLogger.this.logValue(key, Integer.valueOf(value));
            return this;
        }

        public Editor putLong(String key, long value) {
            SharedPreferencesLogger.this.logValue(key, Long.valueOf(value));
            return this;
        }

        public Editor putFloat(String key, float value) {
            SharedPreferencesLogger.this.logValue(key, Float.valueOf(value));
            return this;
        }

        public Editor putBoolean(String key, boolean value) {
            SharedPreferencesLogger.this.logValue(key, Boolean.valueOf(value));
            return this;
        }

        public Editor remove(String key) {
            return this;
        }

        public Editor clear() {
            return this;
        }

        public boolean commit() {
            return true;
        }

        public void apply() {
        }
    }

    public SharedPreferencesLogger(Context context, String tag, MetricsFeatureProvider metricsFeature) {
        this.mContext = context;
        this.mTag = tag;
        this.mMetricsFeature = metricsFeature;
    }

    public Map<String, ?> getAll() {
        return null;
    }

    public String getString(String key, String defValue) {
        return defValue;
    }

    public Set<String> getStringSet(String key, Set<String> defValues) {
        return defValues;
    }

    public int getInt(String key, int defValue) {
        return defValue;
    }

    public long getLong(String key, long defValue) {
        return defValue;
    }

    public float getFloat(String key, float defValue) {
        return defValue;
    }

    public boolean getBoolean(String key, boolean defValue) {
        return defValue;
    }

    public boolean contains(String key) {
        return false;
    }

    public Editor edit() {
        return new EditorLogger();
    }

    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
    }

    private void logValue(String key, Object value) {
        logValue(key, value, false);
    }

    private void logValue(String key, Object value, boolean forceLog) {
        String prefKey = buildPrefKey(this.mTag, key);
        if (forceLog || this.mPreferenceKeySet.contains(prefKey)) {
            Pair<Integer, Object> valueData;
            this.mMetricsFeature.count(this.mContext, buildCountName(prefKey, value), 1);
            String str;
            StringBuilder stringBuilder;
            if (value instanceof Long) {
                int intVal;
                Long longVal = (Long) value;
                if (longVal.longValue() > 2147483647L) {
                    intVal = Integer.MAX_VALUE;
                } else if (longVal.longValue() < -2147483648L) {
                    intVal = Integer.MIN_VALUE;
                } else {
                    intVal = longVal.intValue();
                }
                valueData = Pair.create(Integer.valueOf(1089), Integer.valueOf(intVal));
            } else if (value instanceof Integer) {
                valueData = Pair.create(Integer.valueOf(1089), value);
            } else if (value instanceof Boolean) {
                valueData = Pair.create(Integer.valueOf(1089), Integer.valueOf(((Boolean) value).booleanValue()));
            } else if (value instanceof Float) {
                valueData = Pair.create(Integer.valueOf(995), value);
            } else if (value instanceof String) {
                str = LOG_TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Tried to log string preference ");
                stringBuilder.append(prefKey);
                stringBuilder.append(" = ");
                stringBuilder.append(value);
                Log.d(str, stringBuilder.toString());
                valueData = null;
            } else {
                str = LOG_TAG;
                stringBuilder = new StringBuilder();
                stringBuilder.append("Tried to log unloggable object");
                stringBuilder.append(value);
                Log.w(str, stringBuilder.toString());
                valueData = null;
            }
            if (valueData != null) {
                this.mMetricsFeature.action(this.mContext, 853, Pair.create(Integer.valueOf(854), prefKey), valueData);
            }
            return;
        }
        this.mPreferenceKeySet.add(prefKey);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void logPackageName(String key, String value) {
        String prefKey = new StringBuilder();
        prefKey.append(this.mTag);
        prefKey.append("/");
        prefKey.append(key);
        prefKey = prefKey.toString();
        this.mMetricsFeature.action(this.mContext, 853, value, Pair.create(Integer.valueOf(854), prefKey));
    }

    private void safeLogValue(String key, String value) {
        new AsyncPackageCheck().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new String[]{key, value});
    }

    public static String buildCountName(String prefKey, Object value) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(prefKey);
        stringBuilder.append("|");
        stringBuilder.append(value);
        return stringBuilder.toString();
    }

    public static String buildPrefKey(String tag, String key) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tag);
        stringBuilder.append("/");
        stringBuilder.append(key);
        return stringBuilder.toString();
    }
}
