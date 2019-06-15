package com.oneplus.lib.menu;

import com.oneplus.lib.menu.MenuPresenter.Callback;

interface MenuHelper {
    void dismiss();

    void setPresenterCallback(Callback callback);
}
