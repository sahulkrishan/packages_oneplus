package com.android.setupwizardlib.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Handler;
import android.support.annotation.RequiresPermission;
import android.util.Log;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager.LayoutParams;

public class SystemBarHelper {
    @SuppressLint({"InlinedApi"})
    private static final int DEFAULT_IMMERSIVE_FLAGS = 5634;
    @SuppressLint({"InlinedApi"})
    private static final int DIALOG_IMMERSIVE_FLAGS = 4098;
    private static final int PEEK_DECOR_VIEW_RETRIES = 3;
    private static final int STATUS_BAR_DISABLE_BACK = 4194304;
    private static final String TAG = "SystemBarHelper";

    private static class DecorViewFinder {
        private OnDecorViewInstalledListener mCallback;
        private Runnable mCheckDecorViewRunnable;
        private final Handler mHandler;
        private int mRetries;
        private Window mWindow;

        private DecorViewFinder() {
            this.mHandler = new Handler();
            this.mCheckDecorViewRunnable = new Runnable() {
                public void run() {
                    View decorView = DecorViewFinder.this.mWindow.peekDecorView();
                    if (decorView != null) {
                        DecorViewFinder.this.mCallback.onDecorViewInstalled(decorView);
                        return;
                    }
                    DecorViewFinder.this.mRetries = DecorViewFinder.this.mRetries - 1;
                    if (DecorViewFinder.this.mRetries >= 0) {
                        DecorViewFinder.this.mHandler.post(DecorViewFinder.this.mCheckDecorViewRunnable);
                        return;
                    }
                    String str = SystemBarHelper.TAG;
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append("Cannot get decor view of window: ");
                    stringBuilder.append(DecorViewFinder.this.mWindow);
                    Log.w(str, stringBuilder.toString());
                }
            };
        }

        /* synthetic */ DecorViewFinder(AnonymousClass1 x0) {
            this();
        }

        public void getDecorView(Window window, OnDecorViewInstalledListener callback, int retries) {
            this.mWindow = window;
            this.mRetries = retries;
            this.mCallback = callback;
            this.mCheckDecorViewRunnable.run();
        }
    }

    private interface OnDecorViewInstalledListener {
        void onDecorViewInstalled(View view);
    }

    @TargetApi(21)
    private static class WindowInsetsListener implements OnApplyWindowInsetsListener {
        private int mBottomOffset;
        private boolean mHasCalculatedBottomOffset;

        private WindowInsetsListener() {
            this.mHasCalculatedBottomOffset = false;
        }

        /* synthetic */ WindowInsetsListener(AnonymousClass1 x0) {
            this();
        }

        public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
            if (!this.mHasCalculatedBottomOffset) {
                this.mBottomOffset = SystemBarHelper.getBottomDistance(view);
                this.mHasCalculatedBottomOffset = true;
            }
            int bottomInset = insets.getSystemWindowInsetBottom();
            int bottomMargin = Math.max(insets.getSystemWindowInsetBottom() - this.mBottomOffset, 0);
            MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
            if (bottomMargin < lp.bottomMargin + view.getHeight()) {
                lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomMargin);
                view.setLayoutParams(lp);
                bottomInset = 0;
            }
            return insets.replaceSystemWindowInsets(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), bottomInset);
        }
    }

    public static void hideSystemBars(Dialog dialog) {
        if (VERSION.SDK_INT >= 21) {
            Window window = dialog.getWindow();
            temporarilyDisableDialogFocus(window);
            addVisibilityFlag(window, 4098);
            addImmersiveFlagsToDecorView(window, 4098);
            window.setNavigationBarColor(0);
            window.setStatusBarColor(0);
        }
    }

    public static void hideSystemBars(Window window) {
        if (VERSION.SDK_INT >= 21) {
            addVisibilityFlag(window, (int) DEFAULT_IMMERSIVE_FLAGS);
            addImmersiveFlagsToDecorView(window, DEFAULT_IMMERSIVE_FLAGS);
            window.setNavigationBarColor(0);
            window.setStatusBarColor(0);
        }
    }

    public static void showSystemBars(Dialog dialog, Context context) {
        showSystemBars(dialog.getWindow(), context);
    }

    /* JADX WARNING: Incorrect type for fill-array insn 0x0013, element type: int, insn element type: null */
    public static void showSystemBars(android.view.Window r4, android.content.Context r5) {
        /*
        r0 = android.os.Build.VERSION.SDK_INT;
        r1 = 21;
        if (r0 < r1) goto L_0x002d;
    L_0x0006:
        r0 = 5634; // 0x1602 float:7.895E-42 double:2.7836E-320;
        removeVisibilityFlag(r4, r0);
        removeImmersiveFlagsFromDecorView(r4, r0);
        if (r5 == 0) goto L_0x002d;
    L_0x0010:
        r0 = 2;
        r0 = new int[r0];
        r0 = {16843857, 16843858};
        r0 = r5.obtainStyledAttributes(r0);
        r1 = 0;
        r2 = r0.getColor(r1, r1);
        r3 = 1;
        r1 = r0.getColor(r3, r1);
        r4.setStatusBarColor(r2);
        r4.setNavigationBarColor(r1);
        r0.recycle();
    L_0x002d:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.setupwizardlib.util.SystemBarHelper.showSystemBars(android.view.Window, android.content.Context):void");
    }

    public static void addVisibilityFlag(View view, int flag) {
        if (VERSION.SDK_INT >= 11) {
            view.setSystemUiVisibility(view.getSystemUiVisibility() | flag);
        }
    }

    public static void addVisibilityFlag(Window window, int flag) {
        if (VERSION.SDK_INT >= 11) {
            LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility |= flag;
            window.setAttributes(attrs);
        }
    }

    public static void removeVisibilityFlag(View view, int flag) {
        if (VERSION.SDK_INT >= 11) {
            view.setSystemUiVisibility((~flag) & view.getSystemUiVisibility());
        }
    }

    public static void removeVisibilityFlag(Window window, int flag) {
        if (VERSION.SDK_INT >= 11) {
            LayoutParams attrs = window.getAttributes();
            attrs.systemUiVisibility &= ~flag;
            window.setAttributes(attrs);
        }
    }

    @RequiresPermission("android.permission.STATUS_BAR")
    public static void setBackButtonVisible(Window window, boolean visible) {
        if (VERSION.SDK_INT < 11) {
            return;
        }
        if (visible) {
            removeVisibilityFlag(window, 4194304);
            removeImmersiveFlagsFromDecorView(window, 4194304);
            return;
        }
        addVisibilityFlag(window, 4194304);
        addImmersiveFlagsToDecorView(window, 4194304);
    }

    public static void setImeInsetView(View view) {
        if (VERSION.SDK_INT >= 21) {
            view.setOnApplyWindowInsetsListener(new WindowInsetsListener());
        }
    }

    @TargetApi(11)
    private static void addImmersiveFlagsToDecorView(Window window, final int vis) {
        getDecorView(window, new OnDecorViewInstalledListener() {
            public void onDecorViewInstalled(View decorView) {
                SystemBarHelper.addVisibilityFlag(decorView, vis);
            }
        });
    }

    @TargetApi(11)
    private static void removeImmersiveFlagsFromDecorView(Window window, final int vis) {
        getDecorView(window, new OnDecorViewInstalledListener() {
            public void onDecorViewInstalled(View decorView) {
                SystemBarHelper.removeVisibilityFlag(decorView, vis);
            }
        });
    }

    private static void getDecorView(Window window, OnDecorViewInstalledListener callback) {
        new DecorViewFinder().getDecorView(window, callback, 3);
    }

    private static void temporarilyDisableDialogFocus(final Window window) {
        window.setFlags(8, 8);
        window.setSoftInputMode(256);
        new Handler().post(new Runnable() {
            public void run() {
                window.clearFlags(8);
            }
        });
    }

    private static int getBottomDistance(View view) {
        int[] coords = new int[2];
        view.getLocationInWindow(coords);
        return (view.getRootView().getHeight() - coords[1]) - view.getHeight();
    }
}
