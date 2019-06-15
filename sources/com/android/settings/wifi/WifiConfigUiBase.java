package com.android.settings.wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;

public interface WifiConfigUiBase {
    public static final int MODE_CONNECT = 1;
    public static final int MODE_MODIFY = 2;
    public static final int MODE_VIEW = 0;

    void dispatchSubmit();

    Button getCancelButton();

    Context getContext();

    WifiConfigController getController();

    Button getForgetButton();

    LayoutInflater getLayoutInflater();

    int getMode();

    Button getSubmitButton();

    void setCancelButton(CharSequence charSequence);

    void setForgetButton(CharSequence charSequence);

    void setSubmitButton(CharSequence charSequence);

    void setTitle(int i);

    void setTitle(CharSequence charSequence);
}
