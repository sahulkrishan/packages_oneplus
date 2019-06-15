package com.android.settings.core;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import com.android.settings.SettingsActivity;
import com.android.settings.SubSettings;
import com.android.settingslib.core.instrumentation.VisibilityLoggerMixin;
import com.oneplus.settings.SettingsBaseApplication;

public class SubSettingLauncher {
    private final Context mContext;
    private final LaunchRequest mLaunchRequest;
    private boolean mLaunched;

    static class LaunchRequest {
        Bundle arguments;
        String destinationName;
        int flags;
        boolean isShortCut;
        int mRequestCode;
        Fragment mResultListener;
        int sourceMetricsCategory = -100;
        CharSequence title;
        int titleResId;
        String titleResPackageName;
        UserHandle userHandle;

        LaunchRequest() {
        }
    }

    public SubSettingLauncher(Context context) {
        if (context == null) {
            context = SettingsBaseApplication.mApplication.getApplicationContext();
        }
        this.mContext = context;
        this.mLaunchRequest = new LaunchRequest();
    }

    public SubSettingLauncher setDestination(String fragmentName) {
        this.mLaunchRequest.destinationName = fragmentName;
        return this;
    }

    public SubSettingLauncher setTitle(int titleResId) {
        return setTitle(null, titleResId);
    }

    public SubSettingLauncher setTitle(String titlePackageName, int titleResId) {
        this.mLaunchRequest.titleResPackageName = titlePackageName;
        this.mLaunchRequest.titleResId = titleResId;
        this.mLaunchRequest.title = null;
        return this;
    }

    public SubSettingLauncher setTitle(CharSequence title) {
        this.mLaunchRequest.title = title;
        return this;
    }

    public SubSettingLauncher setIsShortCut(boolean isShortCut) {
        this.mLaunchRequest.isShortCut = isShortCut;
        return this;
    }

    public SubSettingLauncher setArguments(Bundle arguments) {
        this.mLaunchRequest.arguments = arguments;
        return this;
    }

    public SubSettingLauncher setSourceMetricsCategory(int sourceMetricsCategory) {
        this.mLaunchRequest.sourceMetricsCategory = sourceMetricsCategory;
        return this;
    }

    public SubSettingLauncher setResultListener(Fragment listener, int resultRequestCode) {
        this.mLaunchRequest.mRequestCode = resultRequestCode;
        this.mLaunchRequest.mResultListener = listener;
        return this;
    }

    public SubSettingLauncher addFlags(int flags) {
        LaunchRequest launchRequest = this.mLaunchRequest;
        launchRequest.flags |= flags;
        return this;
    }

    public SubSettingLauncher setUserHandle(UserHandle userHandle) {
        this.mLaunchRequest.userHandle = userHandle;
        return this;
    }

    public void launch() {
        if (this.mLaunched) {
            throw new IllegalStateException("This launcher has already been executed. Do not reuse");
        }
        boolean launchForResult = true;
        this.mLaunched = true;
        Intent intent = toIntent();
        boolean launchAsUser = (this.mLaunchRequest.userHandle == null || this.mLaunchRequest.userHandle.getIdentifier() == UserHandle.myUserId()) ? false : true;
        if (this.mLaunchRequest.mResultListener == null) {
            launchForResult = false;
        }
        if (launchAsUser && launchForResult) {
            launchForResultAsUser(intent, this.mLaunchRequest.userHandle, this.mLaunchRequest.mResultListener, this.mLaunchRequest.mRequestCode);
        } else if (launchAsUser && !launchForResult) {
            launchAsUser(intent, this.mLaunchRequest.userHandle);
        } else if (launchAsUser || !launchForResult) {
            launch(intent);
        } else {
            launchForResult(this.mLaunchRequest.mResultListener, intent, this.mLaunchRequest.mRequestCode);
        }
    }

    public void launch(String action) {
        toIntent().setAction(action);
        launch();
    }

    public Intent toIntent() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setClass(this.mContext, SubSettings.class);
        if (TextUtils.isEmpty(this.mLaunchRequest.destinationName)) {
            throw new IllegalArgumentException("Destination fragment must be set");
        }
        intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, this.mLaunchRequest.destinationName);
        if (this.mLaunchRequest.sourceMetricsCategory >= 0) {
            intent.putExtra(VisibilityLoggerMixin.EXTRA_SOURCE_METRICS_CATEGORY, this.mLaunchRequest.sourceMetricsCategory);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_ARGUMENTS, this.mLaunchRequest.arguments);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RES_PACKAGE_NAME, this.mLaunchRequest.titleResPackageName);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID, this.mLaunchRequest.titleResId);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE, this.mLaunchRequest.title);
            intent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT_AS_SHORTCUT, this.mLaunchRequest.isShortCut);
            intent.addFlags(this.mLaunchRequest.flags);
            return intent;
        }
        throw new IllegalArgumentException("Source metrics category must be set");
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void launch(Intent intent) {
        this.mContext.startActivity(intent);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void launchAsUser(Intent intent, UserHandle userHandle) {
        intent.addFlags(268435456);
        intent.addFlags(32768);
        this.mContext.startActivityAsUser(intent, userHandle);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void launchForResultAsUser(Intent intent, UserHandle userHandle, Fragment resultListener, int requestCode) {
        resultListener.getActivity().startActivityForResultAsUser(intent, requestCode, userHandle);
    }

    private void launchForResult(Fragment listener, Intent intent, int requestCode) {
        listener.startActivityForResult(intent, requestCode);
    }
}
