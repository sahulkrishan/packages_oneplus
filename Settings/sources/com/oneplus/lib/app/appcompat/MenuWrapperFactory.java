package com.oneplus.lib.app.appcompat;

import android.content.Context;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import com.oneplus.lib.menu.SupportMenu;
import com.oneplus.lib.menu.SupportMenuItem;
import com.oneplus.lib.menu.SupportSubMenu;

@RestrictTo({Scope.GROUP_ID})
public final class MenuWrapperFactory {
    private MenuWrapperFactory() {
    }

    public static Menu wrapSupportMenu(Context context, SupportMenu supportMenu) {
        return supportMenu;
    }

    public static MenuItem wrapSupportMenuItem(Context context, SupportMenuItem supportMenuItem) {
        return supportMenuItem;
    }

    public static SubMenu wrapSupportSubMenu(Context context, SupportSubMenu supportSubMenu) {
        return supportSubMenu;
    }
}
