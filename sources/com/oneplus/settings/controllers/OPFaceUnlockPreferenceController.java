package com.oneplus.settings.controllers;

import android.content.Context;
import com.android.settings.core.BasePreferenceController;
import com.oneplus.settings.utils.OPUtils;

public class OPFaceUnlockPreferenceController extends BasePreferenceController {
    private static final String KEY_FACEUNLOCK = "oneplus_face_unlock";

    public OPFaceUnlockPreferenceController(Context context) {
        super(context, KEY_FACEUNLOCK);
    }

    public String getPreferenceKey() {
        return KEY_FACEUNLOCK;
    }

    public int getAvailabilityStatus() {
        if (OPUtils.isGuestMode()) {
            return 3;
        }
        return 0;
    }
}
