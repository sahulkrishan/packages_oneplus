package com.oneplus.settings.defaultapp.view;

import com.oneplus.settings.defaultapp.DefaultAppUtils;

public class DefaultMusicPicker extends DefaultBasePicker {
    /* Access modifiers changed, original: protected */
    public String getType() {
        return DefaultAppUtils.getKeyTypeString(2);
    }
}