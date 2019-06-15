package com.android.settings.datausage;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.net.NetworkPolicy;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.format.Time;
import android.util.FeatureFlagUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import com.android.internal.annotations.VisibleForTesting;
import com.android.settings.R;
import com.android.settings.core.FeatureFlags;
import com.android.settings.core.instrumentation.InstrumentedDialogFragment;
import com.android.settingslib.NetworkPolicyEditor;
import com.android.settingslib.net.DataUsageController;

public class BillingCycleSettings extends DataUsageBase implements OnPreferenceChangeListener, DataUsageEditController {
    public static final long GIB_IN_BYTES = 1073741824;
    private static final String KEY_BILLING_CYCLE = "billing_cycle";
    private static final String KEY_DATA_LIMIT = "data_limit";
    private static final String KEY_DATA_WARNING = "data_warning";
    @VisibleForTesting
    static final String KEY_SET_DATA_LIMIT = "set_data_limit";
    private static final String KEY_SET_DATA_WARNING = "set_data_warning";
    private static final boolean LOGD = false;
    private static final long MAX_DATA_LIMIT_BYTES = 53687091200000L;
    public static final long MIB_IN_BYTES = 1048576;
    private static final String TAG = "BillingCycleSettings";
    private static final String TAG_CONFIRM_LIMIT = "confirmLimit";
    private static final String TAG_CYCLE_EDITOR = "cycleEditor";
    private static final String TAG_WARNING_EDITOR = "warningEditor";
    private Preference mBillingCycle;
    private Preference mDataLimit;
    private DataUsageController mDataUsageController;
    private Preference mDataWarning;
    private SwitchPreference mEnableDataLimit;
    private SwitchPreference mEnableDataWarning;
    private NetworkTemplate mNetworkTemplate;

    public static class BytesEditorFragment extends InstrumentedDialogFragment implements OnClickListener {
        private static final String EXTRA_LIMIT = "limit";
        private static final String EXTRA_TEMPLATE = "template";
        private View mView;

        public static void show(DataUsageEditController parent, boolean isLimit) {
            if (parent instanceof Fragment) {
                Fragment targetFragment = (Fragment) parent;
                if (targetFragment.isAdded()) {
                    Bundle args = new Bundle();
                    args.putParcelable(EXTRA_TEMPLATE, parent.getNetworkTemplate());
                    args.putBoolean(EXTRA_LIMIT, isLimit);
                    BytesEditorFragment dialog = new BytesEditorFragment();
                    dialog.setArguments(args);
                    dialog.setTargetFragment(targetFragment, 0);
                    dialog.show(targetFragment.getFragmentManager(), BillingCycleSettings.TAG_WARNING_EDITOR);
                }
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int i;
            Context context = getActivity();
            LayoutInflater dialogInflater = LayoutInflater.from(context);
            boolean isLimit = getArguments().getBoolean(EXTRA_LIMIT);
            this.mView = dialogInflater.inflate(R.layout.data_usage_bytes_editor, null, false);
            setupPicker((EditText) this.mView.findViewById(R.id.bytes), (Spinner) this.mView.findViewById(R.id.size_spinner));
            Builder builder = new Builder(context);
            if (isLimit) {
                i = R.string.data_usage_limit_editor_title;
            } else {
                i = R.string.data_usage_warning_editor_title;
            }
            return builder.setTitle(i).setView(this.mView).setPositiveButton(R.string.data_usage_cycle_editor_positive, this).create();
        }

        private void setupPicker(EditText bytesPicker, Spinner type) {
            long bytes;
            NetworkPolicyEditor editor = ((DataUsageEditController) getTargetFragment()).getNetworkPolicyEditor();
            NetworkTemplate template = (NetworkTemplate) getArguments().getParcelable(EXTRA_TEMPLATE);
            if (getArguments().getBoolean(EXTRA_LIMIT)) {
                bytes = editor.getPolicyLimitBytes(template);
            } else {
                bytes = editor.getPolicyWarningBytes(template);
            }
            String bytesText;
            if (((float) bytes) > 1.61061274E9f) {
                bytesText = formatText(((float) bytes) / 1.07374182E9f);
                bytesPicker.setText(bytesText);
                bytesPicker.setSelection(0, bytesText.length());
                type.setSelection(1);
                return;
            }
            bytesText = formatText(((float) bytes) / 1048576.0f);
            bytesPicker.setText(bytesText);
            bytesPicker.setSelection(0, bytesText.length());
            type.setSelection(0);
        }

        private String formatText(float v) {
            return String.valueOf(((float) Math.round(v * 100.0f)) / 100.0f);
        }

        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                DataUsageEditController target = (DataUsageEditController) getTargetFragment();
                NetworkPolicyEditor editor = target.getNetworkPolicyEditor();
                NetworkTemplate template = (NetworkTemplate) getArguments().getParcelable(EXTRA_TEMPLATE);
                boolean isLimit = getArguments().getBoolean(EXTRA_LIMIT);
                Spinner spinner = (Spinner) this.mView.findViewById(R.id.size_spinner);
                String bytesString = ((EditText) this.mView.findViewById(R.id.bytes)).getText().toString();
                if (bytesString.isEmpty() || bytesString.equals(".")) {
                    bytesString = "0";
                }
                long correctedBytes = Math.min(BillingCycleSettings.MAX_DATA_LIMIT_BYTES, (long) (Float.valueOf(bytesString).floatValue() * ((float) (spinner.getSelectedItemPosition() == 0 ? 1048576 : BillingCycleSettings.GIB_IN_BYTES))));
                if (isLimit) {
                    editor.setPolicyLimitBytes(template, correctedBytes);
                } else {
                    editor.setPolicyWarningBytes(template, correctedBytes);
                }
                target.updateDataUsage();
            }
        }

        public int getMetricsCategory() {
            return 550;
        }
    }

    public static class ConfirmLimitFragment extends InstrumentedDialogFragment implements OnClickListener {
        @VisibleForTesting
        static final String EXTRA_LIMIT_BYTES = "limitBytes";
        public static final float FLOAT = 1.2f;

        public static void show(BillingCycleSettings parent) {
            if (parent.isAdded()) {
                NetworkPolicy policy = parent.services.mPolicyEditor.getPolicy(parent.mNetworkTemplate);
                if (policy != null) {
                    Resources res = parent.getResources();
                    long limitBytes = Math.max(5368709120L, (long) (((float) policy.warningBytes) * 1.2f));
                    Bundle args = new Bundle();
                    args.putLong(EXTRA_LIMIT_BYTES, limitBytes);
                    ConfirmLimitFragment dialog = new ConfirmLimitFragment();
                    dialog.setArguments(args);
                    dialog.setTargetFragment(parent, 0);
                    dialog.show(parent.getFragmentManager(), BillingCycleSettings.TAG_CONFIRM_LIMIT);
                }
            }
        }

        public int getMetricsCategory() {
            return 551;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new Builder(getActivity()).setTitle(R.string.data_usage_limit_dialog_title).setMessage(R.string.data_usage_limit_dialog_mobile).setPositiveButton(17039370, this).setNegativeButton(17039360, null).create();
        }

        public void onClick(DialogInterface dialog, int which) {
            BillingCycleSettings target = (BillingCycleSettings) getTargetFragment();
            if (which == -1) {
                long limitBytes = getArguments().getLong(EXTRA_LIMIT_BYTES);
                if (target != null) {
                    target.setPolicyLimitBytes(limitBytes);
                    target.getPreferenceManager().getSharedPreferences().edit().putBoolean(BillingCycleSettings.KEY_SET_DATA_LIMIT, true).apply();
                }
            }
        }
    }

    public static class CycleEditorFragment extends InstrumentedDialogFragment implements OnClickListener {
        private static final String EXTRA_TEMPLATE = "template";
        private NumberPicker mCycleDayPicker;

        public static void show(BillingCycleSettings parent) {
            if (parent.isAdded()) {
                Bundle args = new Bundle();
                args.putParcelable(EXTRA_TEMPLATE, parent.mNetworkTemplate);
                CycleEditorFragment dialog = new CycleEditorFragment();
                dialog.setArguments(args);
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), BillingCycleSettings.TAG_CYCLE_EDITOR);
            }
        }

        public int getMetricsCategory() {
            return 549;
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            NetworkPolicyEditor editor = ((DataUsageEditController) getTargetFragment()).getNetworkPolicyEditor();
            Builder builder = new Builder(context);
            View view = LayoutInflater.from(builder.getContext()).inflate(R.layout.data_usage_cycle_editor, null, false);
            this.mCycleDayPicker = (NumberPicker) view.findViewById(R.id.cycle_day);
            int cycleDay = editor.getPolicyCycleDay((NetworkTemplate) getArguments().getParcelable(EXTRA_TEMPLATE));
            this.mCycleDayPicker.setMinValue(1);
            this.mCycleDayPicker.setMaxValue(31);
            this.mCycleDayPicker.setValue(cycleDay);
            this.mCycleDayPicker.setWrapSelectorWheel(true);
            return builder.setTitle(R.string.data_usage_cycle_editor_title).setView(view).setPositiveButton(R.string.data_usage_cycle_editor_positive, this).create();
        }

        public void onClick(DialogInterface dialog, int which) {
            NetworkTemplate template = (NetworkTemplate) getArguments().getParcelable(EXTRA_TEMPLATE);
            DataUsageEditController target = (DataUsageEditController) getTargetFragment();
            NetworkPolicyEditor editor = target.getNetworkPolicyEditor();
            this.mCycleDayPicker.clearFocus();
            editor.setPolicyCycleDay(template, this.mCycleDayPicker.getValue(), new Time().timezone);
            target.updateDataUsage();
        }
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setUpForTest(NetworkPolicyEditor policyEditor, Preference billingCycle, Preference dataLimit, Preference dataWarning, SwitchPreference enableLimit, SwitchPreference enableWarning) {
        this.services.mPolicyEditor = policyEditor;
        this.mBillingCycle = billingCycle;
        this.mDataLimit = dataLimit;
        this.mDataWarning = dataWarning;
        this.mEnableDataLimit = enableLimit;
        this.mEnableDataWarning = enableWarning;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mDataUsageController = new DataUsageController(getContext());
        this.mNetworkTemplate = (NetworkTemplate) getArguments().getParcelable("network_template");
        addPreferencesFromResource(R.xml.billing_cycle);
        this.mBillingCycle = findPreference(KEY_BILLING_CYCLE);
        this.mEnableDataWarning = (SwitchPreference) findPreference(KEY_SET_DATA_WARNING);
        this.mEnableDataWarning.setOnPreferenceChangeListener(this);
        this.mDataWarning = findPreference(KEY_DATA_WARNING);
        this.mEnableDataLimit = (SwitchPreference) findPreference(KEY_SET_DATA_LIMIT);
        this.mEnableDataLimit.setOnPreferenceChangeListener(this);
        this.mDataLimit = findPreference(KEY_DATA_LIMIT);
        this.mFooterPreferenceMixin.createFooterPreference().setTitle((int) R.string.data_warning_footnote);
    }

    public void onResume() {
        super.onResume();
        updatePrefs();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void updatePrefs() {
        int cycleDay = this.services.mPolicyEditor.getPolicyCycleDay(this.mNetworkTemplate);
        if (FeatureFlagUtils.isEnabled(getContext(), FeatureFlags.DATA_USAGE_SETTINGS_V2)) {
            this.mBillingCycle.setSummary(null);
        } else if (cycleDay != -1) {
            this.mBillingCycle.setSummary(getString(R.string.billing_cycle_fragment_summary, new Object[]{Integer.valueOf(cycleDay)}));
        } else {
            this.mBillingCycle.setSummary(null);
        }
        long warningBytes = this.services.mPolicyEditor.getPolicyWarningBytes(this.mNetworkTemplate);
        if (warningBytes != -1) {
            this.mDataWarning.setSummary(DataUsageUtils.formatDataUsage(getContext(), warningBytes));
            this.mDataWarning.setEnabled(true);
            this.mEnableDataWarning.setChecked(true);
        } else {
            this.mDataWarning.setSummary(null);
            this.mDataWarning.setEnabled(false);
            this.mEnableDataWarning.setChecked(false);
        }
        long limitBytes = this.services.mPolicyEditor.getPolicyLimitBytes(this.mNetworkTemplate);
        if (limitBytes != -1) {
            this.mDataLimit.setSummary(DataUsageUtils.formatDataUsage(getContext(), limitBytes));
            this.mDataLimit.setEnabled(true);
            this.mEnableDataLimit.setChecked(true);
            return;
        }
        this.mDataLimit.setSummary(null);
        this.mDataLimit.setEnabled(false);
        this.mEnableDataLimit.setChecked(false);
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mBillingCycle) {
            CycleEditorFragment.show(this);
            return true;
        } else if (preference == this.mDataWarning) {
            BytesEditorFragment.show(this, false);
            return true;
        } else if (preference != this.mDataLimit) {
            return super.onPreferenceTreeClick(preference);
        } else {
            BytesEditorFragment.show(this, true);
            return true;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mEnableDataLimit == preference) {
            if (((Boolean) newValue).booleanValue()) {
                ConfirmLimitFragment.show(this);
                return false;
            }
            setPolicyLimitBytes(-1);
            return true;
        } else if (this.mEnableDataWarning != preference) {
            return false;
        } else {
            if (((Boolean) newValue).booleanValue()) {
                setPolicyWarningBytes(this.mDataUsageController.getDefaultWarningLevel());
            } else {
                setPolicyWarningBytes(-1);
            }
            return true;
        }
    }

    public int getMetricsCategory() {
        return 342;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public void setPolicyLimitBytes(long limitBytes) {
        this.services.mPolicyEditor.setPolicyLimitBytes(this.mNetworkTemplate, limitBytes);
        updatePrefs();
    }

    private void setPolicyWarningBytes(long warningBytes) {
        this.services.mPolicyEditor.setPolicyWarningBytes(this.mNetworkTemplate, warningBytes);
        updatePrefs();
    }

    public NetworkPolicyEditor getNetworkPolicyEditor() {
        return this.services.mPolicyEditor;
    }

    public NetworkTemplate getNetworkTemplate() {
        return this.mNetworkTemplate;
    }

    public void updateDataUsage() {
        updatePrefs();
    }
}
