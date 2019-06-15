package com.android.settings.sim;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.SearchIndexableResource;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.PhoneStateListener;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionManager.OnSubscriptionsChangedListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.settings.R;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import java.util.ArrayList;
import java.util.List;

public class SimSettings extends RestrictedSettingsFragment implements Indexable {
    private static final boolean DBG = false;
    private static final String DISALLOW_CONFIG_SIM = "no_config_sim";
    public static final String EXTRA_SLOT_ID = "slot_id";
    private static final String KEY_CALLS = "sim_calls";
    private static final String KEY_CELLULAR_DATA = "sim_cellular_data";
    private static final String KEY_SMS = "sim_sms";
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            ArrayList<SearchIndexableResource> result = new ArrayList();
            if (Utils.showSimCardTile(context)) {
                SearchIndexableResource sir = new SearchIndexableResource(context);
                sir.xmlResId = R.xml.sim_settings;
                result.add(sir);
            }
            return result;
        }
    };
    private static final String SIM_CARD_CATEGORY = "sim_cards";
    private static final String TAG = "SimSettings";
    private List<SubscriptionInfo> mAvailableSubInfos = null;
    private int[] mCallState = new int[this.mPhoneCount];
    private Context mContext;
    private int mNumSlots;
    private final OnSubscriptionsChangedListener mOnSubscriptionsChangeListener = new OnSubscriptionsChangedListener() {
        public void onSubscriptionsChanged() {
            SimSettings.this.updateSubscriptions();
        }
    };
    private int mPhoneCount = TelephonyManager.getDefault().getPhoneCount();
    private PhoneStateListener[] mPhoneStateListener = new PhoneStateListener[this.mPhoneCount];
    private List<SubscriptionInfo> mSelectableSubInfos = null;
    private PreferenceScreen mSimCards = null;
    private List<SubscriptionInfo> mSubInfoList = null;
    private SubscriptionManager mSubscriptionManager;

    private class SimPreference extends Preference {
        Context mContext;
        private int mSlotId;
        private SubscriptionInfo mSubInfoRecord;

        public SimPreference(Context context, SubscriptionInfo subInfoRecord, int slotId) {
            super(context);
            this.mContext = context;
            this.mSubInfoRecord = subInfoRecord;
            this.mSlotId = slotId;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sim");
            stringBuilder.append(this.mSlotId);
            setKey(stringBuilder.toString());
            update();
        }

        public void update() {
            Resources res = this.mContext.getResources();
            setTitle((CharSequence) String.format(this.mContext.getResources().getString(R.string.sim_editor_title), new Object[]{Integer.valueOf(this.mSlotId + 1)}));
            if (this.mSubInfoRecord != null) {
                if (TextUtils.isEmpty(SimSettings.this.getPhoneNumber(this.mSubInfoRecord))) {
                    setSummary(this.mSubInfoRecord.getDisplayName());
                } else {
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(this.mSubInfoRecord.getDisplayName());
                    stringBuilder.append(" - ");
                    stringBuilder.append(PhoneNumberUtils.createTtsSpannable(SimSettings.this.getPhoneNumber(this.mSubInfoRecord)));
                    setSummary((CharSequence) stringBuilder.toString());
                    setEnabled(true);
                }
                setIcon((Drawable) new BitmapDrawable(res, this.mSubInfoRecord.createIconBitmap(this.mContext)));
                return;
            }
            setSummary((int) R.string.sim_slot_empty);
            setFragment(null);
            setEnabled(false);
        }

        private int getSlotId() {
            return this.mSlotId;
        }
    }

    public SimSettings() {
        super(DISALLOW_CONFIG_SIM);
    }

    public int getMetricsCategory() {
        return 88;
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.mContext = getActivity();
        this.mSubscriptionManager = SubscriptionManager.from(getActivity());
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService("phone");
        addPreferencesFromResource(R.xml.sim_settings);
        this.mNumSlots = tm.getSimCount();
        this.mSimCards = (PreferenceScreen) findPreference(SIM_CARD_CATEGORY);
        this.mAvailableSubInfos = new ArrayList(this.mNumSlots);
        this.mSelectableSubInfos = new ArrayList();
        SimSelectNotification.cancelNotification(getActivity());
    }

    private void updateSubscriptions() {
        this.mSubInfoList = this.mSubscriptionManager.getActiveSubscriptionInfoList();
        int i = 0;
        for (int i2 = 0; i2 < this.mNumSlots; i2++) {
            Preference pref = this.mSimCards;
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("sim");
            stringBuilder.append(i2);
            pref = pref.findPreference(stringBuilder.toString());
            if (pref instanceof SimPreference) {
                this.mSimCards.removePreference(pref);
            }
        }
        this.mAvailableSubInfos.clear();
        this.mSelectableSubInfos.clear();
        while (i < this.mNumSlots) {
            SubscriptionInfo sir = this.mSubscriptionManager.getActiveSubscriptionInfoForSimSlotIndex(i);
            SimPreference simPreference = new SimPreference(getPrefContext(), sir, i);
            simPreference.setOrder(i - this.mNumSlots);
            this.mSimCards.addPreference(simPreference);
            this.mAvailableSubInfos.add(sir);
            if (sir != null) {
                this.mSelectableSubInfos.add(sir);
            }
            i++;
        }
        updateAllOptions();
    }

    private void updateAllOptions() {
        updateSimSlotValues();
        updateActivitesCategory();
    }

    private void updateSimSlotValues() {
        int prefSize = this.mSimCards.getPreferenceCount();
        for (int i = 0; i < prefSize; i++) {
            Preference pref = this.mSimCards.getPreference(i);
            if (pref instanceof SimPreference) {
                ((SimPreference) pref).update();
            }
        }
    }

    private void updateActivitesCategory() {
        updateCellularDataValues();
        updateCallValues();
        updateSmsValues();
    }

    private void updateSmsValues() {
        Preference simPref = findPreference(KEY_SMS);
        SubscriptionInfo sir = this.mSubscriptionManager.getDefaultSmsSubscriptionInfo();
        simPref.setTitle((int) R.string.sms_messages_title);
        boolean z = false;
        if (sir != null) {
            simPref.setSummary(sir.getDisplayName());
            if (this.mSelectableSubInfos.size() > 1) {
                z = true;
            }
            simPref.setEnabled(z);
        } else if (sir == null) {
            simPref.setSummary((int) R.string.sim_selection_required_pref);
            if (this.mSelectableSubInfos.size() >= 1) {
                z = true;
            }
            simPref.setEnabled(z);
        }
    }

    private void updateCellularDataValues() {
        Preference simPref = findPreference(KEY_CELLULAR_DATA);
        SubscriptionInfo sir = this.mSubscriptionManager.getDefaultDataSubscriptionInfo();
        simPref.setTitle((int) R.string.cellular_data_title);
        boolean callStateIdle = isCallStateIdle();
        boolean z = false;
        boolean ecbMode = SystemProperties.getBoolean("ril.cdma.inecmmode", false);
        if (sir != null) {
            simPref.setSummary(sir.getDisplayName());
            if (this.mSelectableSubInfos.size() > 1 && callStateIdle && !ecbMode) {
                z = true;
            }
            simPref.setEnabled(z);
        } else if (sir == null) {
            simPref.setSummary((int) R.string.sim_selection_required_pref);
            if (this.mSelectableSubInfos.size() >= 1 && callStateIdle && !ecbMode) {
                z = true;
            }
            simPref.setEnabled(z);
        }
    }

    private void updateCallValues() {
        CharSequence string;
        Preference simPref = findPreference(KEY_CALLS);
        TelecomManager telecomManager = TelecomManager.from(this.mContext);
        PhoneAccountHandle phoneAccount = telecomManager.getUserSelectedOutgoingPhoneAccount();
        List<PhoneAccountHandle> allPhoneAccounts = telecomManager.getCallCapablePhoneAccounts();
        simPref.setTitle((int) R.string.calls_title);
        if (phoneAccount == null) {
            string = this.mContext.getResources().getString(R.string.sim_calls_ask_first_prefs_title);
        } else {
            string = (String) telecomManager.getPhoneAccount(phoneAccount).getLabel();
        }
        simPref.setSummary(string);
        boolean z = true;
        if (allPhoneAccounts.size() <= 1) {
            z = false;
        }
        simPref.setEnabled(z);
    }

    public void onResume() {
        super.onResume();
        this.mSubscriptionManager.addOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        updateSubscriptions();
        TelephonyManager tm = (TelephonyManager) getActivity().getSystemService("phone");
        if (this.mSelectableSubInfos.size() > 1) {
            Log.d(TAG, "Register for call state change");
            for (int i = 0; i < this.mPhoneCount; i++) {
                tm.listen(getPhoneStateListener(i, ((SubscriptionInfo) this.mSelectableSubInfos.get(i)).getSubscriptionId()), 32);
            }
        }
    }

    public void onPause() {
        super.onPause();
        this.mSubscriptionManager.removeOnSubscriptionsChangedListener(this.mOnSubscriptionsChangeListener);
        TelephonyManager tm = (TelephonyManager) getSystemService("phone");
        for (int i = 0; i < this.mPhoneCount; i++) {
            if (this.mPhoneStateListener[i] != null) {
                tm.listen(this.mPhoneStateListener[i], 0);
                this.mPhoneStateListener[i] = null;
            }
        }
    }

    private PhoneStateListener getPhoneStateListener(int phoneId, int subId) {
        final int i = phoneId;
        this.mPhoneStateListener[phoneId] = new PhoneStateListener(Integer.valueOf(subId)) {
            public void onCallStateChanged(int state, String incomingNumber) {
                SimSettings.this.mCallState[i] = state;
                SimSettings.this.updateCellularDataValues();
            }
        };
        return this.mPhoneStateListener[phoneId];
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        Context context = this.mContext;
        Intent intent = new Intent(context, SimDialogActivity.class);
        intent.addFlags(268435456);
        if (preference instanceof SimPreference) {
            Intent newIntent = new Intent(context, SimPreferenceDialog.class);
            newIntent.putExtra(EXTRA_SLOT_ID, ((SimPreference) preference).getSlotId());
            startActivity(newIntent);
        } else if (findPreference(KEY_CELLULAR_DATA) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 0);
            context.startActivity(intent);
        } else if (findPreference(KEY_CALLS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 1);
            context.startActivity(intent);
        } else if (findPreference(KEY_SMS) == preference) {
            intent.putExtra(SimDialogActivity.DIALOG_TYPE_KEY, 2);
            context.startActivity(intent);
        }
        return true;
    }

    private String getPhoneNumber(SubscriptionInfo info) {
        return ((TelephonyManager) this.mContext.getSystemService("phone")).getLine1Number(info.getSubscriptionId());
    }

    private void log(String s) {
        Log.d(TAG, s);
    }

    private boolean isCallStateIdle() {
        boolean callStateIdle = true;
        for (int i : this.mCallState) {
            if (i != 0) {
                callStateIdle = false;
            }
        }
        String str = TAG;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("isCallStateIdle ");
        stringBuilder.append(callStateIdle);
        Log.d(str, stringBuilder.toString());
        return callStateIdle;
    }
}
