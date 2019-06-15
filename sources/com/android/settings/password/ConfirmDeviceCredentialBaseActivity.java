package com.android.settings.password;

import android.app.Fragment;
import android.app.KeyguardManager;
import android.app.admin.DevicePolicyManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.view.MenuItem;
import android.widget.LinearLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.R;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.password.ConfirmLockPassword.InternalActivity;
import com.oneplus.settings.utils.OPUtils;

public abstract class ConfirmDeviceCredentialBaseActivity extends SettingsActivity {
    private static final String STATE_IS_KEYGUARD_LOCKED = "STATE_IS_KEYGUARD_LOCKED";
    private ConfirmCredentialTheme mConfirmCredentialTheme;
    protected DevicePolicyManager mDevicePolicyManager;
    protected int mEffectiveUserId;
    private boolean mEnterAnimationPending;
    private boolean mFirstTimeVisible = true;
    protected boolean mFrp;
    private boolean mIsKeyguardLocked = false;
    protected LockPatternUtils mLockPatternUtils;
    private boolean mRestoring;
    protected boolean mReturnCredentials = false;
    protected int mUserId;
    protected UserManager mUserManager;

    enum ConfirmCredentialTheme {
        NORMAL,
        DARK,
        WORK
    }

    private boolean isInternalActivity() {
        return (this instanceof InternalActivity) || (this instanceof ConfirmLockPattern.InternalActivity);
    }

    /* Access modifiers changed, original: protected */
    public boolean isFingerprintNeedShowDarkTheme() {
        return getIntent().getBooleanExtra(ConfirmDeviceCredentialBaseFragment.ALLOW_FP_AUTHENTICATION, false);
    }

    /* Access modifiers changed, original: protected */
    public boolean isStrongAuthRequired() {
        return (!this.mFrp && this.mLockPatternUtils.isFingerprintAllowedForUser(this.mEffectiveUserId) && this.mUserManager.isUserUnlocked(this.mUserId)) ? false : true;
    }

    /* Access modifiers changed, original: protected */
    public boolean isFingerprintDisabledByAdmin() {
        return (this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mEffectiveUserId) & 32) != 0;
    }

    /* Access modifiers changed, original: protected */
    public void setDarkThemeIfNeeded() {
        Intent intent = getIntent();
        boolean z = false;
        this.mReturnCredentials = intent.getBooleanExtra(ChooseLockSettingsHelper.EXTRA_KEY_RETURN_CREDENTIALS, false);
        this.mUserId = Utils.getUserIdFromBundle(this, intent.getExtras(), isInternalActivity());
        if (this.mUserId == -9999) {
            z = true;
        }
        this.mFrp = z;
        this.mUserManager = UserManager.get(this);
        this.mEffectiveUserId = this.mUserManager.getCredentialOwnerProfile(this.mUserId);
        this.mLockPatternUtils = new LockPatternUtils(this);
        this.mDevicePolicyManager = (DevicePolicyManager) getSystemService("device_policy");
        if (OPUtils.isSupportCustomFingerprint() && isFingerprintNeedShowDarkTheme()) {
            setTheme(R.style.OnePlusPasswordDarkTheme);
        }
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedState) {
        boolean isKeyguardLocked;
        boolean z = false;
        if (UserManager.get(this).isManagedProfile(Utils.getCredentialOwnerUserId(this, Utils.getUserIdFromBundle(this, getIntent().getExtras(), isInternalActivity())))) {
            setTheme(R.style.f921Theme.ConfirmDeviceCredentialsWork);
            this.mConfirmCredentialTheme = ConfirmCredentialTheme.WORK;
        } else if (getIntent().getBooleanExtra(ConfirmDeviceCredentialBaseFragment.DARK_THEME, false)) {
            setTheme(R.style.f920Theme.ConfirmDeviceCredentialsDark);
            this.mConfirmCredentialTheme = ConfirmCredentialTheme.DARK;
        } else {
            setTheme(R.style.OnePlusPasswordTheme);
            this.mConfirmCredentialTheme = ConfirmCredentialTheme.NORMAL;
        }
        setDarkThemeIfNeeded();
        super.onCreate(savedState);
        if (OPUtils.isSupportCustomFingerprint() && isFingerprintNeedShowDarkTheme()) {
            getWindow().getDecorView().setSystemUiVisibility(0);
        }
        if (this.mConfirmCredentialTheme == ConfirmCredentialTheme.NORMAL) {
            ((LinearLayout) findViewById(R.id.content_parent)).setFitsSystemWindows(true);
        }
        getWindow().addFlags(8192);
        if (savedState == null) {
            isKeyguardLocked = ((KeyguardManager) getSystemService(KeyguardManager.class)).isKeyguardLocked();
        } else {
            isKeyguardLocked = savedState.getBoolean(STATE_IS_KEYGUARD_LOCKED, false);
        }
        this.mIsKeyguardLocked = isKeyguardLocked;
        if (this.mIsKeyguardLocked && getIntent().getBooleanExtra(ConfirmDeviceCredentialBaseFragment.SHOW_WHEN_LOCKED, false)) {
            getWindow().addFlags(524288);
        }
        setTitle(getIntent().getStringExtra(ConfirmDeviceCredentialBaseFragment.TITLE_TEXT));
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            getActionBar().setHomeButtonEnabled(true);
        }
        if (savedState != null) {
            z = true;
        }
        this.mRestoring = z;
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(STATE_IS_KEYGUARD_LOCKED, this.mIsKeyguardLocked);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    public void onResume() {
        super.onResume();
        if (!isChangingConfigurations() && !this.mRestoring && this.mConfirmCredentialTheme == ConfirmCredentialTheme.DARK && this.mFirstTimeVisible) {
            this.mFirstTimeVisible = false;
            prepareEnterAnimation();
            this.mEnterAnimationPending = true;
        }
    }

    private ConfirmDeviceCredentialBaseFragment getFragment() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.main_content);
        if (fragment == null || !(fragment instanceof ConfirmDeviceCredentialBaseFragment)) {
            return null;
        }
        return (ConfirmDeviceCredentialBaseFragment) fragment;
    }

    public void onEnterAnimationComplete() {
        super.onEnterAnimationComplete();
        if (this.mEnterAnimationPending) {
            startEnterAnimation();
            this.mEnterAnimationPending = false;
        }
    }

    public void prepareEnterAnimation() {
        getFragment().prepareEnterAnimation();
    }

    public void startEnterAnimation() {
        getFragment().startEnterAnimation();
    }

    public ConfirmCredentialTheme getConfirmCredentialTheme() {
        return this.mConfirmCredentialTheme;
    }
}
