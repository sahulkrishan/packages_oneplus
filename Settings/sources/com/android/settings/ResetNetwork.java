package com.android.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.euicc.EuiccManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settings.core.InstrumentedFragment;
import com.android.settings.core.SubSettingLauncher;
import com.android.settings.enterprise.ActionDisabledByAdminDialogHelper;
import com.android.settings.password.ChooseLockSettingsHelper;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.oneplus.settings.utils.OPSNSUtils;
import java.util.ArrayList;
import java.util.List;

public class ResetNetwork extends InstrumentedFragment {
    private static final int KEYGUARD_REQUEST = 55;
    private static final String TAG = "ResetNetwork";
    private View mContentView;
    private CheckBox mEsimCheckbox;
    private View mEsimContainer;
    private Button mInitiateButton;
    private final OnClickListener mInitiateListener = new OnClickListener() {
        public void onClick(View v) {
            if (!ResetNetwork.this.runKeyguardConfirmation(55)) {
                ResetNetwork.this.showFinalConfirmation();
            }
        }
    };
    private Spinner mSubscriptionSpinner;
    private List<SubscriptionInfo> mSubscriptions;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.reset_network_title);
    }

    private boolean runKeyguardConfirmation(int request) {
        return new ChooseLockSettingsHelper(getActivity(), this).launchConfirmationActivity(request, getActivity().getResources().getText(R.string.reset_network_title));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 55) {
            if (resultCode == -1) {
                showFinalConfirmation();
            } else {
                establishInitialState();
            }
        }
    }

    private void showFinalConfirmation() {
        Bundle args = new Bundle();
        if (this.mSubscriptions != null && this.mSubscriptions.size() > 0) {
            args.putInt("subscription", ((SubscriptionInfo) this.mSubscriptions.get(this.mSubscriptionSpinner.getSelectedItemPosition())).getSubscriptionId());
        }
        args.putBoolean("erase_esim", this.mEsimCheckbox.isChecked());
        new SubSettingLauncher(getContext()).setDestination(ResetNetworkConfirm.class.getName()).setArguments(args).setTitle((int) R.string.reset_network_confirm_title).setSourceMetricsCategory(getMetricsCategory()).launch();
    }

    private void establishInitialState() {
        this.mSubscriptionSpinner = (Spinner) this.mContentView.findViewById(R.id.reset_network_subscription);
        this.mEsimContainer = this.mContentView.findViewById(R.id.erase_esim_container);
        this.mEsimCheckbox = (CheckBox) this.mContentView.findViewById(R.id.erase_esim);
        this.mSubscriptions = SubscriptionManager.from(getActivity()).getActiveSubscriptionInfoList();
        if (this.mSubscriptions == null || this.mSubscriptions.size() <= 0) {
            this.mSubscriptionSpinner.setVisibility(4);
        } else {
            int defaultSubscription = SubscriptionManager.getDefaultDataSubscriptionId();
            if (!SubscriptionManager.isUsableSubIdValue(defaultSubscription)) {
                defaultSubscription = SubscriptionManager.getDefaultVoiceSubscriptionId();
            }
            if (!SubscriptionManager.isUsableSubIdValue(defaultSubscription)) {
                defaultSubscription = SubscriptionManager.getDefaultSmsSubscriptionId();
            }
            if (!SubscriptionManager.isUsableSubIdValue(defaultSubscription)) {
                defaultSubscription = SubscriptionManager.getDefaultSubscriptionId();
            }
            int selectedIndex = 0;
            int size = this.mSubscriptions.size();
            List<String> subscriptionNames = new ArrayList();
            for (SubscriptionInfo record : this.mSubscriptions) {
                if (record.getSubscriptionId() == defaultSubscription) {
                    selectedIndex = subscriptionNames.size();
                }
                String name = record.getDisplayName().toString();
                if (TextUtils.isEmpty(name)) {
                    name = record.getNumber();
                }
                if (TextUtils.isEmpty(name)) {
                    name = record.getCarrierName().toString();
                }
                if (TextUtils.isEmpty(name)) {
                    name = String.format("MCC:%s MNC:%s Slot:%s Id:%s", new Object[]{Integer.valueOf(record.getMcc()), Integer.valueOf(record.getMnc()), Integer.valueOf(record.getSimSlotIndex()), Integer.valueOf(record.getSubscriptionId())});
                }
                String Simname = null;
                if (subscriptionNames.size() < 2) {
                    Simname = OPSNSUtils.getSimName(getActivity(), record.getSimSlotIndex());
                }
                if (Simname != null) {
                    subscriptionNames.add(Simname);
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), 17367048, subscriptionNames);
            adapter.setDropDownViewResource(17367049);
            this.mSubscriptionSpinner.setAdapter(adapter);
            this.mSubscriptionSpinner.setSelection(selectedIndex);
            if (this.mSubscriptions.size() > 1) {
                this.mSubscriptionSpinner.setVisibility(0);
            } else {
                this.mSubscriptionSpinner.setVisibility(4);
            }
        }
        this.mInitiateButton = (Button) this.mContentView.findViewById(R.id.initiate_reset_network);
        this.mInitiateButton.setOnClickListener(this.mInitiateListener);
        if (showEuiccSettings(getContext())) {
            this.mEsimContainer.setVisibility(0);
            ((TextView) this.mContentView.findViewById(R.id.erase_esim_title)).setText(R.string.reset_esim_title);
            this.mEsimContainer.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ResetNetwork.this.mEsimCheckbox.toggle();
                }
            });
            return;
        }
        this.mEsimCheckbox.setChecked(false);
    }

    private boolean showEuiccSettings(Context context) {
        boolean z = false;
        if (!((EuiccManager) context.getSystemService("euicc")).isEnabled()) {
            return false;
        }
        ContentResolver resolver = context.getContentResolver();
        if (!(Global.getInt(resolver, "euicc_provisioned", 0) == 0 && Global.getInt(resolver, "development_settings_enabled", 0) == 0)) {
            z = true;
        }
        return z;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        UserManager um = UserManager.get(getActivity());
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_network_reset", UserHandle.myUserId());
        if (!um.isAdminUser() || RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_network_reset", UserHandle.myUserId())) {
            return inflater.inflate(R.layout.network_reset_disallowed_screen, null);
        }
        if (admin != null) {
            new ActionDisabledByAdminDialogHelper(getActivity()).prepareDialogBuilder("no_network_reset", admin).setOnDismissListener(new -$$Lambda$ResetNetwork$sNSFVrhYYO7NxbKY35cdb4I6sYI(this)).show();
            return new View(getContext());
        }
        this.mContentView = inflater.inflate(R.layout.reset_network, null);
        establishInitialState();
        return this.mContentView;
    }

    public int getMetricsCategory() {
        return 83;
    }
}
