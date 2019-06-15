package com.android.settings.applications.appinfo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.support.v4.view.PointerIconCompat;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import com.android.settings.overlay.FeatureFactory;

public class AppSettingPreferenceController extends AppInfoPreferenceControllerBase {
    private String mPackageName;

    public AppSettingPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    public AppSettingPreferenceController setPackageName(String packageName) {
        this.mPackageName = packageName;
        return this;
    }

    public int getAvailabilityStatus() {
        int i = 1;
        if (TextUtils.isEmpty(this.mPackageName) || this.mParent == null) {
            return 1;
        }
        if (resolveIntent(new Intent("android.intent.action.APPLICATION_PREFERENCES").setPackage(this.mPackageName)) != null) {
            i = 0;
        }
        return i;
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!TextUtils.equals(preference.getKey(), getPreferenceKey())) {
            return false;
        }
        Intent intent = resolveIntent(new Intent("android.intent.action.APPLICATION_PREFERENCES").setPackage(this.mPackageName));
        if (intent == null) {
            return false;
        }
        FeatureFactory.getFactory(this.mContext).getMetricsFeatureProvider().actionWithSource(this.mContext, this.mParent.getMetricsCategory(), PointerIconCompat.TYPE_TOP_LEFT_DIAGONAL_DOUBLE_ARROW);
        this.mContext.startActivity(intent);
        return true;
    }

    private Intent resolveIntent(Intent i) {
        ResolveInfo result = this.mContext.getPackageManager().resolveActivity(i, 0);
        if (result != null) {
            return new Intent(i.getAction()).setClassName(result.activityInfo.packageName, result.activityInfo.name);
        }
        return null;
    }
}
