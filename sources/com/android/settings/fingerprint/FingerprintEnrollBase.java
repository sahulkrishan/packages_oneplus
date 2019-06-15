package com.android.settings.fingerprint;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.core.InstrumentedActivity;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.setupwizardlib.GlifLayout;
import com.oneplus.settings.utils.OPUtils;

public abstract class FingerprintEnrollBase extends InstrumentedActivity implements OnClickListener {
    public static final int RESULT_FINISHED = 1;
    static final int RESULT_SKIP = 2;
    static final int RESULT_TIMEOUT = 3;
    protected boolean isSetupPage;
    protected byte[] mToken;
    protected int mUserId;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (applyActionBarTitle() != -1) {
                actionBar.setTitle(applyActionBarTitle());
            } else {
                actionBar.setTitle("");
            }
        }
        setAppropriateStatusBar();
        this.mToken = getIntent().getByteArrayExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        if (savedInstanceState != null && this.mToken == null) {
            this.mToken = savedInstanceState.getByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN);
        }
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
    }

    /* Access modifiers changed, original: protected */
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
    }

    /* Access modifiers changed, original: protected */
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initViews();
    }

    /* Access modifiers changed, original: protected */
    public void initViews() {
        Button nextButton = getNextButton();
        if (nextButton != null) {
            nextButton.setOnClickListener(this);
        }
    }

    /* Access modifiers changed, original: protected */
    public GlifLayout getLayout() {
        return (GlifLayout) findViewById(R.id.setup_wizard_layout);
    }

    /* Access modifiers changed, original: protected */
    public void setHeaderText(int resId, boolean force) {
        TextView layoutTitle = getLayout().getHeaderTextView();
        layoutTitle.setTextAppearance(this, R.style.OnePlusSuwGlifHeaderTitle);
        CharSequence previousTitle = layoutTitle.getText();
        CharSequence title = getText(resId);
        if (previousTitle != title || force) {
            if (!TextUtils.isEmpty(previousTitle)) {
                layoutTitle.setAccessibilityLiveRegion(1);
            }
            getLayout().setHeaderText(title);
            setTitle(title);
        }
    }

    /* Access modifiers changed, original: protected */
    public void setHeaderText(int resId) {
        setHeaderText(resId, false);
    }

    /* Access modifiers changed, original: protected */
    public Button getNextButton() {
        return (Button) findViewById(R.id.next_button);
    }

    public void onClick(View v) {
        if (v == getNextButton()) {
            onNextButtonClick();
        }
    }

    /* Access modifiers changed, original: protected */
    public void onNextButtonClick() {
    }

    /* Access modifiers changed, original: protected */
    public Intent getEnrollingIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
        intent.putExtra(ChooseLockSettingsHelper.EXTRA_KEY_CHALLENGE_TOKEN, this.mToken);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        return intent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isSetupWizard() {
        return false;
    }

    private boolean needSetWhiteStatusBar() {
        return isSetupWizard() && !OPUtils.isO2();
    }

    private boolean needSetTransparentStatusBar() {
        return isSetupWizard() && OPUtils.isO2();
    }

    private void setAppropriateStatusBar() {
        int systemUiVisibility;
        if (isSetupWizard() ? needSetWhiteStatusBar() : OPUtils.isWhiteModeOn(getContentResolver())) {
            systemUiVisibility = 9472;
        } else {
            systemUiVisibility = 1280;
        }
        getWindow().getDecorView().setSystemUiVisibility(systemUiVisibility);
        if (needSetTransparentStatusBar()) {
            getWindow().setStatusBarColor(0);
        }
    }

    /* Access modifiers changed, original: protected */
    public int applyActionBarTitle() {
        return -1;
    }
}
