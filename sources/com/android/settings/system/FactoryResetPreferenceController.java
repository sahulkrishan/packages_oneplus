package com.android.settings.system;

import android.content.Context;
import android.os.UserManager;
import com.android.settings.Utils;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.core.AbstractPreferenceController;

public class FactoryResetPreferenceController extends AbstractPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_FACTORY_RESET = "factory_reset";
    private final UserManager mUm;

    public FactoryResetPreferenceController(Context context) {
        super(context);
        this.mUm = (UserManager) context.getSystemService("user");
    }

    public boolean isAvailable() {
        return this.mUm.isAdminUser() || Utils.isDemoUser(this.mContext);
    }

    public String getPreferenceKey() {
        return KEY_FACTORY_RESET;
    }
}
