package com.oneplus.custom.utils;

import com.oneplus.custom.utils.OpCustomizeSettings.CUSTOM_TYPE;

public class OpCustomizeSettingsG1 extends OpCustomizeSettings {
    /* Access modifiers changed, original: protected */
    public CUSTOM_TYPE getCustomization() {
        CUSTOM_TYPE result = CUSTOM_TYPE.NONE;
        switch (ParamReader.getCustFlagVal()) {
            case 1:
                return CUSTOM_TYPE.JCC;
            case 2:
                return CUSTOM_TYPE.SW;
            default:
                return result;
        }
    }
}
