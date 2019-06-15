package com.android.setupwizardlib.template;

import android.view.View;
import com.android.setupwizardlib.R;
import com.android.setupwizardlib.TemplateLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class NavigationBarMixin implements Mixin {
    private TemplateLayout mTemplateLayout;

    public NavigationBarMixin(TemplateLayout layout) {
        this.mTemplateLayout = layout;
    }

    public NavigationBar getNavigationBar() {
        View view = this.mTemplateLayout.findManagedViewById(R.id.suw_layout_navigation_bar);
        return view instanceof NavigationBar ? (NavigationBar) view : null;
    }

    public void setNextButtonText(int text) {
        getNavigationBar().getNextButton().setText(text);
    }

    public void setNextButtonText(CharSequence text) {
        getNavigationBar().getNextButton().setText(text);
    }

    public CharSequence getNextButtonText() {
        return getNavigationBar().getNextButton().getText();
    }

    public void setNavigationBarListener(NavigationBarListener listener) {
        getNavigationBar().setNavigationBarListener(listener);
    }
}
