package com.android.settings.accounts;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.core.AbstractPreferenceController;
import java.util.List;

public class EmergencyInfoPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin, OnPreferenceClickListener {
    private static final String ACTION_EDIT_EMERGENCY_INFO = "android.settings.EDIT_EMERGENCY_INFO";
    private static final String KEY_EMERGENCY_INFO = "emergency_info";
    private static final String PACKAGE_NAME_EMERGENCY = "com.android.emergency";

    public EmergencyInfoPreferenceController(Context context) {
        super(context);
    }

    public void updateRawDataToIndex(List<SearchIndexableRaw> rawData) {
        if (isAvailable()) {
            SearchIndexableRaw data = new SearchIndexableRaw(this.mContext);
            Resources res = this.mContext.getResources();
            data.title = res.getString(R.string.emergency_info_title);
            data.screenTitle = res.getString(R.string.emergency_info_title);
            rawData.add(data);
        }
    }

    public void updateState(Preference preference) {
        UserInfo info = ((UserManager) this.mContext.getSystemService(UserManager.class)).getUserInfo(UserHandle.myUserId());
        preference.setSummary(this.mContext.getString(R.string.emergency_info_summary, new Object[]{info.name}));
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        if (!KEY_EMERGENCY_INFO.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent(ACTION_EDIT_EMERGENCY_INFO);
        intent.setFlags(67108864);
        this.mContext.startActivity(intent);
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!KEY_EMERGENCY_INFO.equals(preference.getKey())) {
            return false;
        }
        Intent intent = new Intent(ACTION_EDIT_EMERGENCY_INFO);
        intent.setFlags(67108864);
        this.mContext.startActivity(intent);
        return true;
    }

    public boolean isAvailable() {
        List<ResolveInfo> infos = this.mContext.getPackageManager().queryIntentActivities(new Intent(ACTION_EDIT_EMERGENCY_INFO).setPackage(PACKAGE_NAME_EMERGENCY), 0);
        if (infos == null || infos.isEmpty()) {
            return false;
        }
        return true;
    }

    public String getPreferenceKey() {
        return KEY_EMERGENCY_INFO;
    }
}
