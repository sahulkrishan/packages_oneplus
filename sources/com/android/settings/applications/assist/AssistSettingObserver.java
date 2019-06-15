package com.android.settings.applications.assist;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.provider.Settings.Secure;
import android.support.annotation.MainThread;
import com.android.settingslib.utils.ThreadUtils;
import java.util.List;

public abstract class AssistSettingObserver extends ContentObserver {
    private final Uri ASSIST_URI = Secure.getUriFor("assistant");

    public abstract List<Uri> getSettingUris();

    @MainThread
    public abstract void onSettingChange();

    public AssistSettingObserver() {
        super(null);
    }

    public void register(ContentResolver cr, boolean register) {
        if (register) {
            cr.registerContentObserver(this.ASSIST_URI, false, this);
            List<Uri> settingUri = getSettingUris();
            if (settingUri != null) {
                for (Uri uri : settingUri) {
                    cr.registerContentObserver(uri, false, this);
                }
                return;
            }
            return;
        }
        cr.unregisterContentObserver(this);
    }

    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        boolean shouldUpdatePreference = false;
        List<Uri> settingUri = getSettingUris();
        if (this.ASSIST_URI.equals(uri) || (settingUri != null && settingUri.contains(uri))) {
            shouldUpdatePreference = true;
        }
        if (shouldUpdatePreference) {
            ThreadUtils.postOnMainThread(new -$$Lambda$AssistSettingObserver$iBFvDXS30QMXzEK-zAgHqcs78mE(this));
        }
    }
}
