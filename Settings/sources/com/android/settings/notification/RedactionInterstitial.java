package com.android.settings.notification;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import com.android.settings.R;
import com.android.settings.RestrictedRadioButton;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.SetupRedactionInterstitial;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;

public class RedactionInterstitial extends SettingsActivity {

    public static class RedactionInterstitialFragment extends SettingsPreferenceFragment implements OnCheckedChangeListener, OnClickListener {
        private RadioGroup mRadioGroup;
        private RestrictedRadioButton mRedactSensitiveButton;
        private RestrictedRadioButton mShowAllButton;
        private int mUserId;

        public int getMetricsCategory() {
            return 74;
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.redaction_interstitial, container, false);
        }

        public void onViewCreated(View view, Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            this.mRadioGroup = (RadioGroup) view.findViewById(R.id.radio_group);
            this.mShowAllButton = (RestrictedRadioButton) view.findViewById(R.id.show_all);
            this.mRedactSensitiveButton = (RestrictedRadioButton) view.findViewById(R.id.redact_sensitive);
            this.mRadioGroup.setOnCheckedChangeListener(this);
            this.mUserId = Utils.getUserIdFromBundle(getContext(), getActivity().getIntent().getExtras());
            if (UserManager.get(getContext()).isManagedProfile(this.mUserId)) {
                ((TextView) view.findViewById(R.id.message)).setText(R.string.lock_screen_notifications_interstitial_message_profile);
                this.mShowAllButton.setText(R.string.lock_screen_notifications_summary_show_profile);
                this.mRedactSensitiveButton.setText(R.string.lock_screen_notifications_summary_hide_profile);
                ((RadioButton) view.findViewById(R.id.hide_all)).setVisibility(8);
            }
            ((Button) view.findViewById(R.id.redaction_done_button)).setOnClickListener(this);
        }

        public void onClick(View v) {
            if (v.getId() == R.id.redaction_done_button) {
                SetupRedactionInterstitial.setEnabled(getContext(), false);
                RedactionInterstitial activity = (RedactionInterstitial) getActivity();
                if (activity != null) {
                    activity.setResult(-1, null);
                    finish();
                }
            }
        }

        public void onResume() {
            super.onResume();
            checkNotificationFeaturesAndSetDisabled(this.mShowAllButton, 12);
            checkNotificationFeaturesAndSetDisabled(this.mRedactSensitiveButton, 4);
            loadFromSettings();
        }

        private void checkNotificationFeaturesAndSetDisabled(RestrictedRadioButton button, int keyguardNotifications) {
            button.setDisabledByAdmin(RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(getActivity(), keyguardNotifications, this.mUserId));
        }

        private void loadFromSettings() {
            boolean showUnredacted = true;
            boolean showNotifications = UserManager.get(getContext()).isManagedProfile(this.mUserId) || Secure.getIntForUser(getContentResolver(), "lock_screen_show_notifications", 0, this.mUserId) != 0;
            if (Secure.getIntForUser(getContentResolver(), "lock_screen_allow_private_notifications", 1, this.mUserId) == 0) {
                showUnredacted = false;
            }
            int checkedButtonId = R.id.hide_all;
            if (showNotifications) {
                if (showUnredacted && !this.mShowAllButton.isDisabledByAdmin()) {
                    checkedButtonId = R.id.show_all;
                } else if (!this.mRedactSensitiveButton.isDisabledByAdmin()) {
                    checkedButtonId = R.id.redact_sensitive;
                }
            }
            this.mRadioGroup.check(checkedButtonId);
        }

        public void onCheckedChanged(RadioGroup group, int checkedId) {
            int i = 0;
            boolean show = checkedId == R.id.show_all;
            boolean enabled = checkedId != R.id.hide_all;
            Secure.putIntForUser(getContentResolver(), "lock_screen_allow_private_notifications", show ? 1 : 0, this.mUserId);
            Secure.putIntForUser(getContentResolver(), "lock_screen_show_notifications", enabled ? 1 : 0, this.mUserId);
            Secure.putIntForUser(getContentResolver(), "lock_screen_allow_private_notifications", show ? 1 : 0, 999);
            ContentResolver contentResolver = getContentResolver();
            String str = "lock_screen_show_notifications";
            if (enabled) {
                i = 1;
            }
            Secure.putIntForUser(contentResolver, str, i, 999);
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(SettingsActivity.EXTRA_SHOW_FRAGMENT, RedactionInterstitialFragment.class.getName());
        return modIntent;
    }

    /* Access modifiers changed, original: protected */
    public boolean isValidFragment(String fragmentName) {
        return RedactionInterstitialFragment.class.getName().equals(fragmentName);
    }

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        ((LinearLayout) findViewById(R.id.content_parent)).setFitsSystemWindows(true);
    }

    public static Intent createStartIntent(Context ctx, int userId) {
        int i;
        Intent intent = new Intent(ctx, RedactionInterstitial.class);
        String str = SettingsActivity.EXTRA_SHOW_FRAGMENT_TITLE_RESID;
        if (UserManager.get(ctx).isManagedProfile(userId)) {
            i = R.string.lock_screen_notifications_interstitial_title_profile;
        } else {
            i = R.string.lock_screen_notifications_interstitial_title;
        }
        return intent.putExtra(str, i).putExtra("android.intent.extra.USER_ID", userId);
    }
}
