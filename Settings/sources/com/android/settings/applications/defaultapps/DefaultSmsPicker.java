package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.text.TextUtils;
import com.android.internal.telephony.SmsApplication;
import com.android.internal.telephony.SmsApplication.SmsApplicationData;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settingslib.applications.DefaultAppInfo;
import com.android.settingslib.widget.CandidateInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DefaultSmsPicker extends DefaultAppPickerFragment {
    private DefaultKeyUpdater mDefaultKeyUpdater = new DefaultKeyUpdater();

    static class DefaultKeyUpdater {
        DefaultKeyUpdater() {
        }

        public String getDefaultApplication(Context context) {
            ComponentName appName = SmsApplication.getDefaultSmsApplication(context, true);
            if (appName != null) {
                return appName.getPackageName();
            }
            return null;
        }

        public void setDefaultApplication(Context context, String key) {
            SmsApplication.setDefaultApplication(key, context);
        }
    }

    public int getMetricsCategory() {
        return 789;
    }

    /* Access modifiers changed, original: protected */
    public int getPreferenceScreenResId() {
        return R.xml.default_sms_settings;
    }

    /* Access modifiers changed, original: protected */
    public List<DefaultAppInfo> getCandidates() {
        Context context = getContext();
        Collection<SmsApplicationData> smsApplications = SmsApplication.getApplicationCollection(context);
        List<DefaultAppInfo> candidates = new ArrayList(smsApplications.size());
        for (SmsApplicationData smsApplicationData : smsApplications) {
            try {
                candidates.add(new DefaultAppInfo(context, this.mPm, this.mPm.getApplicationInfoAsUser(smsApplicationData.mPackageName, 0, this.mUserId)));
            } catch (NameNotFoundException e) {
            }
        }
        return candidates;
    }

    /* Access modifiers changed, original: protected */
    public String getDefaultKey() {
        return this.mDefaultKeyUpdater.getDefaultApplication(getContext());
    }

    /* Access modifiers changed, original: protected */
    public boolean setDefaultKey(String key) {
        if (TextUtils.isEmpty(key) || TextUtils.equals(key, getDefaultKey())) {
            return false;
        }
        this.mDefaultKeyUpdater.setDefaultApplication(getContext(), key);
        return true;
    }

    /* Access modifiers changed, original: protected */
    public String getConfirmationMessage(CandidateInfo info) {
        if (Utils.isPackageDirectBootAware(getContext(), info.getKey())) {
            return null;
        }
        return getContext().getString(R.string.direct_boot_unaware_dialog_message);
    }
}
