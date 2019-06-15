package com.android.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.Toast;
import com.android.internal.telephony.CommandException;
import com.android.internal.telephony.CommandException.Error;
import com.android.internal.telephony.IccCardConstants.State;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;

public class IccLockSettings extends SettingsPreferenceFragment implements OnPinEnteredListener {
    private static final String CURRENT_TAB = "currentTab";
    private static final boolean DBG = true;
    private static final String DIALOG_ERROR = "dialogError";
    private static final String DIALOG_PIN = "dialogPin";
    private static final String DIALOG_STATE = "dialogState";
    private static final String ENABLE_TO_STATE = "enableState";
    private static final int ICC_LOCK_MODE = 1;
    private static final int ICC_NEW_MODE = 3;
    private static final int ICC_OLD_MODE = 2;
    private static final int ICC_REENTER_MODE = 4;
    private static final int MAX_PIN_LENGTH = 8;
    private static final int MIN_PIN_LENGTH = 4;
    private static final int MSG_CHANGE_ICC_PIN_COMPLETE = 101;
    private static final int MSG_ENABLE_ICC_PIN_COMPLETE = 100;
    private static final int MSG_SIM_STATE_CHANGED = 102;
    private static final String NEW_PINCODE = "newPinCode";
    private static final int OFF_MODE = 0;
    private static final String OLD_PINCODE = "oldPinCode";
    private static final String PIN_DIALOG = "sim_pin";
    private static final String PIN_TOGGLE = "sim_toggle";
    private static final String TAG = "IccLockSettings";
    private int mDialogState = 0;
    private TabContentFactory mEmptyTabContent = new TabContentFactory() {
        public View createTabContent(String tag) {
            return new View(IccLockSettings.this.mTabHost.getContext());
        }
    };
    private String mError;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            AsyncResult ar = msg.obj;
            boolean z = false;
            IccLockSettings iccLockSettings;
            switch (msg.what) {
                case 100:
                    iccLockSettings = IccLockSettings.this;
                    if (ar.exception == null) {
                        z = true;
                    }
                    iccLockSettings.iccLockChanged(z, msg.arg1, ar.exception);
                    return;
                case 101:
                    iccLockSettings = IccLockSettings.this;
                    if (ar.exception == null) {
                        z = true;
                    }
                    iccLockSettings.iccPinChanged(z, msg.arg1);
                    return;
                case 102:
                    IccLockSettings.this.updatePreferences();
                    return;
                default:
                    return;
            }
        }
    };
    private ListView mListView;
    private String mNewPin;
    private String mOldPin;
    private Phone mPhone;
    private String mPin;
    private EditPinPreference mPinDialog;
    private SwitchPreference mPinToggle;
    private Resources mRes;
    private final BroadcastReceiver mSimStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                IccLockSettings.this.mHandler.sendMessage(IccLockSettings.this.mHandler.obtainMessage(102));
            }
        }
    };
    private TabHost mTabHost;
    private OnTabChangeListener mTabListener = new OnTabChangeListener() {
        public void onTabChanged(String tabId) {
            Phone phone;
            SubscriptionInfo sir = SubscriptionManager.from(IccLockSettings.this.getActivity().getBaseContext()).getActiveSubscriptionInfoForSimSlotIndex(Integer.parseInt(tabId));
            IccLockSettings iccLockSettings = IccLockSettings.this;
            if (sir == null) {
                phone = null;
            } else {
                phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(sir.getSubscriptionId()));
            }
            iccLockSettings.mPhone = phone;
            IccLockSettings.this.updatePreferences();
        }
    };
    private TabWidget mTabWidget;
    private boolean mToState;

    static boolean isIccLockEnabled() {
        return PhoneFactory.getDefaultPhone().getIccCard().getIccLockEnabled();
    }

    static String getSummary(Context context) {
        Resources res = context.getResources();
        if (isIccLockEnabled()) {
            return res.getString(R.string.sim_lock_on);
        }
        return res.getString(R.string.sim_lock_off);
    }

    public void onCreate(Bundle savedInstanceState) {
        getActivity().getWindow().getDecorView().setSystemUiVisibility(0);
        super.onCreate(savedInstanceState);
        if (Utils.isMonkeyRunning()) {
            finish();
            return;
        }
        addPreferencesFromResource(R.xml.sim_lock_settings);
        this.mPinDialog = (EditPinPreference) findPreference(PIN_DIALOG);
        this.mPinToggle = (SwitchPreference) findPreference(PIN_TOGGLE);
        if (savedInstanceState != null && savedInstanceState.containsKey(DIALOG_STATE)) {
            this.mDialogState = savedInstanceState.getInt(DIALOG_STATE);
            this.mPin = savedInstanceState.getString(DIALOG_PIN);
            this.mError = savedInstanceState.getString(DIALOG_ERROR);
            this.mToState = savedInstanceState.getBoolean(ENABLE_TO_STATE);
            switch (this.mDialogState) {
                case 3:
                    this.mOldPin = savedInstanceState.getString(OLD_PINCODE);
                    break;
                case 4:
                    this.mOldPin = savedInstanceState.getString(OLD_PINCODE);
                    this.mNewPin = savedInstanceState.getString(NEW_PINCODE);
                    break;
            }
        }
        this.mPinDialog.setOnPinEnteredListener(this);
        getPreferenceScreen().setPersistent(false);
        this.mRes = getResources();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LayoutInflater layoutInflater = inflater;
        ViewGroup viewGroup = container;
        Bundle bundle = savedInstanceState;
        TelephonyManager tm = (TelephonyManager) getContext().getSystemService("phone");
        int numSims = tm.getSimCount();
        int i = 1;
        if (numSims > 1) {
            Phone phone;
            View view = layoutInflater.inflate(R.layout.icc_lock_tabs, viewGroup, false);
            ViewGroup prefs_container = (ViewGroup) view.findViewById(R.id.prefs_container);
            Utils.prepareCustomPreferencesList(viewGroup, view, prefs_container, false);
            prefs_container.addView(super.onCreateView(layoutInflater, prefs_container, bundle));
            this.mTabHost = (TabHost) view.findViewById(16908306);
            this.mTabWidget = (TabWidget) view.findViewById(16908307);
            this.mListView = (ListView) view.findViewById(16908298);
            this.mTabHost.setup();
            this.mTabHost.setOnTabChangedListener(this.mTabListener);
            this.mTabHost.clearAllTabs();
            SubscriptionManager sm = SubscriptionManager.from(getContext());
            int i2 = 0;
            while (i2 < numSims) {
                TelephonyManager tm2;
                SubscriptionInfo subInfo = sm.getActiveSubscriptionInfoForSimSlotIndex(i2);
                TabHost tabHost = this.mTabHost;
                String valueOf = String.valueOf(i2);
                if (subInfo == null) {
                    Context context = getContext();
                    tm2 = tm;
                    tm = new Object[i];
                    tm[0] = Integer.valueOf(i2 + 1);
                    tm = context.getString(R.string.sim_editor_title, tm);
                } else {
                    tm2 = tm;
                    tm = subInfo.getDisplayName();
                }
                tabHost.addTab(buildTabSpec(valueOf, String.valueOf(tm)));
                i2++;
                tm = tm2;
                i = 1;
            }
            tm = sm.getActiveSubscriptionInfoForSimSlotIndex(null);
            if (tm == null) {
                phone = null;
            } else {
                phone = PhoneFactory.getPhone(SubscriptionManager.getPhoneId(tm.getSubscriptionId()));
            }
            this.mPhone = phone;
            if (bundle != null && bundle.containsKey(CURRENT_TAB)) {
                this.mTabHost.setCurrentTabByTag(bundle.getString(CURRENT_TAB));
            }
            return view;
        }
        this.mPhone = PhoneFactory.getDefaultPhone();
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updatePreferences();
    }

    private void updatePreferences() {
        boolean pinToggleState = false;
        boolean pinDialogState = false;
        if (this.mPhone != null) {
            State cardState = this.mPhone.getIccCard().getState();
            if (cardState == State.READY || cardState == State.LOADED) {
                pinToggleState = true;
                pinDialogState = true;
            }
        }
        if (this.mPinDialog != null) {
            this.mPinDialog.setEnabled(pinDialogState);
        }
        if (this.mPinToggle != null) {
            this.mPinToggle.setEnabled(pinToggleState);
            if (this.mPhone != null) {
                this.mPinToggle.setChecked(this.mPhone.getIccCard().getIccLockEnabled());
            }
        }
    }

    public int getMetricsCategory() {
        return 56;
    }

    public void onResume() {
        super.onResume();
        getContext().registerReceiver(this.mSimStateReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
        if (this.mDialogState != 0) {
            showPinDialog();
        } else {
            resetDialogState();
        }
    }

    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(this.mSimStateReceiver);
    }

    public int getHelpResource() {
        return R.string.help_url_icc_lock;
    }

    public void onSaveInstanceState(Bundle out) {
        if (this.mPinDialog.isDialogOpen()) {
            out.putInt(DIALOG_STATE, this.mDialogState);
            out.putString(DIALOG_PIN, this.mPinDialog.getEditText().getText().toString());
            out.putString(DIALOG_ERROR, this.mError);
            out.putBoolean(ENABLE_TO_STATE, this.mToState);
            switch (this.mDialogState) {
                case 3:
                    out.putString(OLD_PINCODE, this.mOldPin);
                    break;
                case 4:
                    out.putString(OLD_PINCODE, this.mOldPin);
                    out.putString(NEW_PINCODE, this.mNewPin);
                    break;
            }
        }
        super.onSaveInstanceState(out);
        if (this.mTabHost != null) {
            out.putString(CURRENT_TAB, this.mTabHost.getCurrentTabTag());
        }
    }

    private void showPinDialog() {
        if (this.mDialogState != 0) {
            setDialogValues();
            this.mPinDialog.showPinDialog();
            EditText editText = this.mPinDialog.getEditText();
            if (!(TextUtils.isEmpty(this.mPin) || editText == null)) {
                String content = editText.getText().toString();
                if (content != null && content.length() >= this.mPin.length()) {
                    editText.setSelection(this.mPin.length());
                }
            }
        }
    }

    private void setDialogValues() {
        this.mPinDialog.setText(this.mPin);
        String message = "";
        switch (this.mDialogState) {
            case 1:
                CharSequence string;
                message = this.mRes.getString(R.string.sim_enter_pin);
                EditPinPreference editPinPreference = this.mPinDialog;
                if (this.mToState) {
                    string = this.mRes.getString(R.string.sim_enable_sim_lock);
                } else {
                    string = this.mRes.getString(R.string.sim_disable_sim_lock);
                }
                editPinPreference.setDialogTitle(string);
                break;
            case 2:
                message = this.mRes.getString(R.string.sim_enter_old);
                this.mPinDialog.setDialogTitle((CharSequence) this.mRes.getString(R.string.sim_change_pin));
                break;
            case 3:
                message = this.mRes.getString(R.string.sim_enter_new);
                this.mPinDialog.setDialogTitle((CharSequence) this.mRes.getString(R.string.sim_change_pin));
                break;
            case 4:
                message = this.mRes.getString(R.string.sim_reenter_new);
                this.mPinDialog.setDialogTitle((CharSequence) this.mRes.getString(R.string.sim_change_pin));
                break;
        }
        if (this.mError != null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(this.mError);
            stringBuilder.append("\n");
            stringBuilder.append(message);
            message = stringBuilder.toString();
            this.mError = null;
        }
        this.mPinDialog.setDialogMessage((CharSequence) message);
    }

    public void onPinEntered(EditPinPreference preference, boolean positiveResult) {
        if (positiveResult) {
            this.mPin = preference.getText();
            if (reasonablePin(this.mPin)) {
                switch (this.mDialogState) {
                    case 1:
                        tryChangeIccLockState();
                        break;
                    case 2:
                        this.mOldPin = this.mPin;
                        this.mDialogState = 3;
                        this.mError = null;
                        this.mPin = null;
                        showPinDialog();
                        break;
                    case 3:
                        this.mNewPin = this.mPin;
                        this.mDialogState = 4;
                        this.mPin = null;
                        showPinDialog();
                        break;
                    case 4:
                        if (!this.mPin.equals(this.mNewPin)) {
                            this.mError = this.mRes.getString(R.string.sim_pins_dont_match);
                            this.mDialogState = 3;
                            this.mPin = null;
                            showPinDialog();
                            break;
                        }
                        this.mError = null;
                        tryChangePin();
                        break;
                }
                return;
            }
            this.mError = this.mRes.getString(R.string.sim_bad_pin);
            showPinDialog();
            return;
        }
        resetDialogState();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (preference == this.mPinToggle) {
            this.mToState = this.mPinToggle.isChecked();
            this.mPinToggle.setChecked(this.mToState ^ 1);
            this.mDialogState = 1;
            showPinDialog();
        } else if (preference == this.mPinDialog) {
            this.mDialogState = 2;
            return false;
        }
        return true;
    }

    private void tryChangeIccLockState() {
        this.mPhone.getIccCard().setIccLockEnabled(this.mToState, this.mPin, Message.obtain(this.mHandler, 100));
        this.mPinToggle.setEnabled(false);
    }

    private void iccLockChanged(boolean success, int attemptsRemaining, Throwable exception) {
        if (success) {
            this.mPinToggle.setChecked(this.mToState);
        } else if (exception instanceof CommandException) {
            if (((CommandException) exception).getCommandError() == Error.PASSWORD_INCORRECT) {
                Toast.makeText(getContext(), getPinPasswordErrorMessage(attemptsRemaining), 1).show();
            } else if (this.mToState) {
                Toast.makeText(getContext(), this.mRes.getString(R.string.sim_pin_enable_failed), 1).show();
            } else {
                Toast.makeText(getContext(), this.mRes.getString(R.string.sim_pin_disable_failed), 1).show();
            }
        }
        this.mPinToggle.setEnabled(true);
        resetDialogState();
    }

    private void iccPinChanged(boolean success, int attemptsRemaining) {
        if (success) {
            Toast.makeText(getContext(), this.mRes.getString(R.string.sim_change_succeeded), 0).show();
        } else {
            Toast.makeText(getContext(), getPinPasswordErrorMessage(attemptsRemaining), 1).show();
        }
        resetDialogState();
    }

    private void tryChangePin() {
        this.mPhone.getIccCard().changeIccLockPassword(this.mOldPin, this.mNewPin, Message.obtain(this.mHandler, 101));
    }

    private String getPinPasswordErrorMessage(int attemptsRemaining) {
        String displayMessage;
        if (attemptsRemaining == 0) {
            displayMessage = this.mRes.getString(R.string.wrong_pin_code_pukked);
        } else if (attemptsRemaining > 0) {
            displayMessage = this.mRes.getQuantityString(R.plurals.wrong_pin_code, attemptsRemaining, new Object[]{Integer.valueOf(attemptsRemaining)});
        } else {
            displayMessage = this.mRes.getString(R.string.pin_failed);
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("getPinPasswordErrorMessage: attemptsRemaining=");
        stringBuilder.append(attemptsRemaining);
        stringBuilder.append(" displayMessage=");
        stringBuilder.append(displayMessage);
        Log.d(str, stringBuilder.toString());
        return displayMessage;
    }

    private boolean reasonablePin(String pin) {
        if (pin == null || pin.length() < 4 || pin.length() > 8) {
            return false;
        }
        return true;
    }

    private void resetDialogState() {
        this.mError = null;
        this.mDialogState = 2;
        this.mPin = "";
        setDialogValues();
        this.mDialogState = 0;
    }

    private TabSpec buildTabSpec(String tag, String title) {
        return this.mTabHost.newTabSpec(tag).setIndicator(title).setContent(this.mEmptyTabContent);
    }
}
