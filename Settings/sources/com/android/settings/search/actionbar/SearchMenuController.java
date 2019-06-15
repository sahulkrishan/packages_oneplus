package com.android.settings.search.actionbar;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.overlay.FeatureFactory;
import com.android.settings.search.SearchFeatureProvider;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.ObservableFragment;
import com.android.settingslib.core.lifecycle.ObservablePreferenceFragment;
import com.android.settingslib.core.lifecycle.events.OnCreateOptionsMenu;

public class SearchMenuController implements LifecycleObserver, OnCreateOptionsMenu {
    public static final String NEED_SEARCH_ICON_IN_ACTION_BAR = "need_search_icon_in_action_bar";
    private final Fragment mHost;

    public static void init(ObservablePreferenceFragment host) {
        host.getLifecycle().addObserver(new SearchMenuController(host));
    }

    public static void init(ObservableFragment host) {
        host.getLifecycle().addObserver(new SearchMenuController(host));
    }

    private SearchMenuController(Fragment host) {
        this.mHost = host;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (Utils.isDeviceProvisioned(this.mHost.getContext()) && menu != null) {
            Bundle arguments = this.mHost.getArguments();
            if (arguments == null || arguments.getBoolean(NEED_SEARCH_ICON_IN_ACTION_BAR, true)) {
                MenuItem searchItem = menu.add(0, 0, 0, R.string.search_menu);
                searchItem.setIcon(R.drawable.ic_menu_search_material);
                searchItem.setShowAsAction(2);
                searchItem.setOnMenuItemClickListener(new -$$Lambda$SearchMenuController$5lHWir39yWMPpFtqgtH1CYNgf1M(this));
            }
        }
    }

    public static /* synthetic */ boolean lambda$onCreateOptionsMenu$0(SearchMenuController searchMenuController, MenuItem target) {
        Intent intent = SearchFeatureProvider.SEARCH_UI_INTENT;
        intent.setPackage(FeatureFactory.getFactory(searchMenuController.mHost.getContext()).getSearchFeatureProvider().getSettingsIntelligencePkgName());
        searchMenuController.mHost.startActivityForResult(intent, 0);
        return true;
    }
}
