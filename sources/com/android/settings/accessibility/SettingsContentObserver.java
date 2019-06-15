package com.android.settings.accessibility;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.Secure;
import java.util.ArrayList;
import java.util.List;

abstract class SettingsContentObserver extends ContentObserver {
    private final List<String> mKeysToObserve;

    public abstract void onChange(boolean z, Uri uri);

    public SettingsContentObserver(Handler handler) {
        super(handler);
        this.mKeysToObserve = new ArrayList(2);
        this.mKeysToObserve.add("accessibility_enabled");
        this.mKeysToObserve.add("enabled_accessibility_services");
    }

    public SettingsContentObserver(Handler handler, List<String> keysToObserve) {
        this(handler);
        this.mKeysToObserve.addAll(keysToObserve);
    }

    public void register(ContentResolver contentResolver) {
        for (int i = 0; i < this.mKeysToObserve.size(); i++) {
            contentResolver.registerContentObserver(Secure.getUriFor((String) this.mKeysToObserve.get(i)), false, this);
        }
    }

    public void unregister(ContentResolver contentResolver) {
        contentResolver.unregisterContentObserver(this);
    }
}
