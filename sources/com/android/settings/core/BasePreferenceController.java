package com.android.settings.core;

import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.search.ResultPayload;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.core.AbstractPreferenceController;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class BasePreferenceController extends AbstractPreferenceController {
    public static final int AVAILABLE = 0;
    public static final int CONDITIONALLY_UNAVAILABLE = 1;
    public static final int DISABLED_DEPENDENT_SETTING = 4;
    public static final int DISABLED_FOR_USER = 3;
    private static final String TAG = "SettingsPrefController";
    public static final int UNSUPPORTED_ON_DEVICE = 2;
    protected final String mPreferenceKey;

    @Retention(RetentionPolicy.SOURCE)
    public @interface AvailabilityStatus {
    }

    public abstract int getAvailabilityStatus();

    public static BasePreferenceController createInstance(Context context, String controllerName, String key) {
        try {
            return (BasePreferenceController) Class.forName(controllerName).getConstructor(new Class[]{Context.class, String.class}).newInstance(new Object[]{context, key});
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid preference controller: ");
            stringBuilder.append(controllerName);
            throw new IllegalStateException(stringBuilder.toString(), e);
        }
    }

    public static BasePreferenceController createInstance(Context context, String controllerName) {
        try {
            return (BasePreferenceController) Class.forName(controllerName).getConstructor(new Class[]{Context.class}).newInstance(new Object[]{context});
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Invalid preference controller: ");
            stringBuilder.append(controllerName);
            throw new IllegalStateException(stringBuilder.toString(), e);
        }
    }

    public BasePreferenceController(Context context, String preferenceKey) {
        super(context);
        this.mPreferenceKey = preferenceKey;
        if (TextUtils.isEmpty(this.mPreferenceKey)) {
            throw new IllegalArgumentException("Preference key must be set");
        }
    }

    public String getPreferenceKey() {
        return this.mPreferenceKey;
    }

    public final boolean isAvailable() {
        int availabilityStatus = getAvailabilityStatus();
        return availabilityStatus == 0 || availabilityStatus == 4;
    }

    public final boolean isSupported() {
        return getAvailabilityStatus() != 2;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (getAvailabilityStatus() == 4) {
            Preference preference = screen.findPreference(getPreferenceKey());
            if (preference != null) {
                preference.setEnabled(false);
            }
        }
    }

    public int getSliceType() {
        return 0;
    }

    public IntentFilter getIntentFilter() {
        return null;
    }

    public boolean isSliceable() {
        return false;
    }

    public boolean hasAsyncUpdate() {
        return false;
    }

    public void updateNonIndexableKeys(List<String> keys) {
        if ((this instanceof AbstractPreferenceController) && !isAvailable()) {
            String key = getPreferenceKey();
            if (TextUtils.isEmpty(key)) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Skipping updateNonIndexableKeys due to empty key ");
                stringBuilder.append(toString());
                Log.w(str, stringBuilder.toString());
                return;
            }
            keys.add(key);
        }
    }

    public void updateRawDataToIndex(List<SearchIndexableRaw> list) {
    }

    @Deprecated
    public ResultPayload getResultPayload() {
        return null;
    }
}
