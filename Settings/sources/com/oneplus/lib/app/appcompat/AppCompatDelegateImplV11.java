package com.oneplus.lib.app.appcompat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.Window;

class AppCompatDelegateImplV11 extends AppCompatDelegateImplV9 {
    AppCompatDelegateImplV11(Context context, Window window, AppCompatCallback callback) {
        super(context, window, callback);
    }

    /* Access modifiers changed, original: 0000 */
    public View callActivityOnCreateView(View parent, String name, Context context, AttributeSet attrs) {
        return null;
    }
}
