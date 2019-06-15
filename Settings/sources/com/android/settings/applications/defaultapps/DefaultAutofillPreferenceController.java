package com.android.settings.applications.defaultapps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.view.autofill.AutofillManager;
import com.android.settingslib.applications.DefaultAppInfo;

public class DefaultAutofillPreferenceController extends DefaultAppPreferenceController {
    private final AutofillManager mAutofillManager = ((AutofillManager) this.mContext.getSystemService(AutofillManager.class));

    public DefaultAutofillPreferenceController(Context context) {
        super(context);
    }

    public boolean isAvailable() {
        return this.mAutofillManager != null && this.mAutofillManager.hasAutofillFeature() && this.mAutofillManager.isAutofillSupported();
    }

    public String getPreferenceKey() {
        return "default_autofill";
    }

    /* Access modifiers changed, original: protected */
    public Intent getSettingIntent(DefaultAppInfo info) {
        if (info == null) {
            return null;
        }
        return new AutofillSettingIntentProvider(this.mContext, info.getKey()).getIntent();
    }

    /* Access modifiers changed, original: protected */
    public DefaultAppInfo getDefaultAppInfo() {
        String flattenComponent = Secure.getString(this.mContext.getContentResolver(), "autofill_service");
        if (TextUtils.isEmpty(flattenComponent)) {
            return null;
        }
        return new DefaultAppInfo(this.mContext, this.mPackageManager, this.mUserId, ComponentName.unflattenFromString(flattenComponent));
    }
}
