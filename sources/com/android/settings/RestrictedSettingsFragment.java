package com.android.settings;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.annotation.VisibleForTesting;
import android.view.View;
import android.widget.TextView;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

@Deprecated
public abstract class RestrictedSettingsFragment extends SettingsPreferenceFragment {
    private static final String KEY_CHALLENGE_REQUESTED = "chrq";
    private static final String KEY_CHALLENGE_SUCCEEDED = "chsc";
    @VisibleForTesting
    static final int REQUEST_PIN_CHALLENGE = 12309;
    protected static final String RESTRICT_IF_OVERRIDABLE = "restrict_if_overridable";
    @VisibleForTesting
    AlertDialog mActionDisabledDialog;
    private boolean mChallengeRequested;
    private boolean mChallengeSucceeded;
    private TextView mEmptyTextView;
    private EnforcedAdmin mEnforcedAdmin;
    private boolean mIsAdminUser;
    private boolean mOnlyAvailableForAdmins = false;
    private final String mRestrictionKey;
    private RestrictionsManager mRestrictionsManager;
    private BroadcastReceiver mScreenOffReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (!RestrictedSettingsFragment.this.mChallengeRequested) {
                RestrictedSettingsFragment.this.mChallengeSucceeded = false;
                RestrictedSettingsFragment.this.mChallengeRequested = false;
            }
        }
    };
    private UserManager mUserManager;

    public RestrictedSettingsFragment(String restrictionKey) {
        this.mRestrictionKey = restrictionKey;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mRestrictionsManager = (RestrictionsManager) getSystemService("restrictions");
        this.mUserManager = (UserManager) getSystemService("user");
        this.mIsAdminUser = this.mUserManager.isAdminUser();
        if (icicle != null) {
            this.mChallengeSucceeded = icicle.getBoolean(KEY_CHALLENGE_SUCCEEDED, false);
            this.mChallengeRequested = icicle.getBoolean(KEY_CHALLENGE_REQUESTED, false);
        }
        IntentFilter offFilter = new IntentFilter("android.intent.action.SCREEN_OFF");
        offFilter.addAction("android.intent.action.USER_PRESENT");
        getActivity().registerReceiver(this.mScreenOffReceiver, offFilter);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mEmptyTextView = initEmptyTextView();
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (getActivity().isChangingConfigurations()) {
            outState.putBoolean(KEY_CHALLENGE_REQUESTED, this.mChallengeRequested);
            outState.putBoolean(KEY_CHALLENGE_SUCCEEDED, this.mChallengeSucceeded);
        }
    }

    public void onResume() {
        super.onResume();
        if (shouldBeProviderProtected(this.mRestrictionKey)) {
            ensurePin();
        }
    }

    public void onDestroy() {
        getActivity().unregisterReceiver(this.mScreenOffReceiver);
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_PIN_CHALLENGE) {
            if (resultCode == -1) {
                this.mChallengeSucceeded = true;
                this.mChallengeRequested = false;
                if (this.mActionDisabledDialog != null && this.mActionDisabledDialog.isShowing()) {
                    this.mActionDisabledDialog.setOnDismissListener(null);
                    this.mActionDisabledDialog.dismiss();
                }
            } else {
                this.mChallengeSucceeded = false;
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void ensurePin() {
        if (!this.mChallengeSucceeded && !this.mChallengeRequested && this.mRestrictionsManager.hasRestrictionsProvider()) {
            Intent intent = this.mRestrictionsManager.createLocalApprovalIntent();
            if (intent != null) {
                this.mChallengeRequested = true;
                this.mChallengeSucceeded = false;
                PersistableBundle request = new PersistableBundle();
                request.putString("android.request.mesg", getResources().getString(R.string.restr_pin_enter_admin_pin));
                intent.putExtra("android.content.extra.REQUEST_BUNDLE", request);
                startActivityForResult(intent, REQUEST_PIN_CHALLENGE);
            }
        }
    }

    /* Access modifiers changed, original: protected */
    public boolean isRestrictedAndNotProviderProtected() {
        boolean z = false;
        if (this.mRestrictionKey == null || RESTRICT_IF_OVERRIDABLE.equals(this.mRestrictionKey)) {
            return false;
        }
        if (this.mUserManager.hasUserRestriction(this.mRestrictionKey) && !this.mRestrictionsManager.hasRestrictionsProvider()) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public boolean hasChallengeSucceeded() {
        return (this.mChallengeRequested && this.mChallengeSucceeded) || !this.mChallengeRequested;
    }

    /* Access modifiers changed, original: protected */
    public boolean shouldBeProviderProtected(String restrictionKey) {
        boolean z = false;
        if (restrictionKey == null) {
            return false;
        }
        boolean restricted = RESTRICT_IF_OVERRIDABLE.equals(restrictionKey) || this.mUserManager.hasUserRestriction(this.mRestrictionKey);
        if (restricted && this.mRestrictionsManager.hasRestrictionsProvider()) {
            z = true;
        }
        return z;
    }

    /* Access modifiers changed, original: protected */
    public TextView initEmptyTextView() {
        return (TextView) getActivity().findViewById(16908292);
    }

    public EnforcedAdmin getRestrictionEnforcedAdmin() {
        this.mEnforcedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), this.mRestrictionKey, UserHandle.myUserId());
        if (this.mEnforcedAdmin != null && this.mEnforcedAdmin.userId == -10000) {
            this.mEnforcedAdmin.userId = UserHandle.myUserId();
        }
        return this.mEnforcedAdmin;
    }

    public TextView getEmptyTextView() {
        return this.mEmptyTextView;
    }

    /* Access modifiers changed, original: protected */
    public void onDataSetChanged() {
        highlightPreferenceIfNeeded();
        if (isUiRestrictedByOnlyAdmin() && (this.mActionDisabledDialog == null || !this.mActionDisabledDialog.isShowing())) {
            this.mActionDisabledDialog = new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder(this.mRestrictionKey, getRestrictionEnforcedAdmin()).setOnDismissListener(new -$$Lambda$RestrictedSettingsFragment$LUdTuWQX3d8kcdKiPapl2FlA0-c(this)).show();
            setEmptyView(new View(getContext()));
        } else if (this.mEmptyTextView != null) {
            setEmptyView(this.mEmptyTextView);
        }
        super.onDataSetChanged();
    }

    public void setIfOnlyAvailableForAdmins(boolean onlyForAdmins) {
        this.mOnlyAvailableForAdmins = onlyForAdmins;
    }

    /* Access modifiers changed, original: protected */
    public boolean isUiRestricted() {
        return isRestrictedAndNotProviderProtected() || !hasChallengeSucceeded() || (!this.mIsAdminUser && this.mOnlyAvailableForAdmins);
    }

    /* Access modifiers changed, original: protected */
    public boolean isUiRestrictedByOnlyAdmin() {
        return isUiRestricted() && !this.mUserManager.hasBaseUserRestriction(this.mRestrictionKey, UserHandle.of(UserHandle.myUserId())) && (this.mIsAdminUser || !this.mOnlyAvailableForAdmins);
    }
}
