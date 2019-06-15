package com.android.settings.support.actionbar;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.ObservableFragment;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;

public class HelpMenuController implements LifecycleObserver, OnCreateOptionsMenu {
    private final Fragment mHost;

    public static void init(ObservablePreferenceFragment host) {
        host.getLifecycle().addObserver(new HelpMenuController(host));
    }

    public static void init(ObservableFragment host) {
        host.getLifecycle().addObserver(new HelpMenuController(host));
    }

    private HelpMenuController(Fragment host) {
        this.mHost = host;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Bundle arguments = this.mHost.getArguments();
        int helpResourceId = 0;
        if (arguments != null && arguments.containsKey(HelpResourceProvider.HELP_URI_RESOURCE_KEY)) {
            helpResourceId = arguments.getInt(HelpResourceProvider.HELP_URI_RESOURCE_KEY);
        } else if (this.mHost instanceof HelpResourceProvider) {
            helpResourceId = ((HelpResourceProvider) this.mHost).getHelpResource();
        }
        String helpUri = null;
        if (helpResourceId != 0) {
            helpUri = this.mHost.getContext().getString(helpResourceId);
        }
        Activity activity = this.mHost.getActivity();
        if (helpUri != null && activity != null) {
            HelpUtils.prepareHelpMenuItem(activity, menu, helpUri, this.mHost.getClass().getName());
        }
    }
}
