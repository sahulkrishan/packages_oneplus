package com.android.settings.support.actionbar;

import com.android.settings.R;

public interface HelpResourceProvider {
    public static final String HELP_URI_RESOURCE_KEY = "help_uri_resource";

    int getHelpResource() {
        return R.string.help_uri_default;
    }
}
