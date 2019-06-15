package com.android.settings.deviceinfo.imei;

import android.app.Fragment;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.telephony.TelephonyManager;
import com.android.settings.R;
import com.android.settings.core.PreferenceControllerMixin;
import com.android.settingslib.deviceinfo.AbstractSimStatusImeiInfoPreferenceController;
import java.util.ArrayList;
import java.util.List;

public class ImeiInfoPreferenceController extends AbstractSimStatusImeiInfoPreferenceController implements PreferenceControllerMixin {
    private static final String KEY_IMEI_INFO = "imei_info";
    private final Fragment mFragment;
    private final boolean mIsMultiSim;
    private final List<Preference> mPreferenceList = new ArrayList();
    private final TelephonyManager mTelephonyManager;

    public ImeiInfoPreferenceController(Context context, Fragment fragment) {
        super(context);
        this.mFragment = fragment;
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        boolean z = true;
        if (this.mTelephonyManager.getPhoneCount() <= 1) {
            z = false;
        }
        this.mIsMultiSim = z;
    }

    public String getPreferenceKey() {
        return KEY_IMEI_INFO;
    }

    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        Preference preference = screen.findPreference(getPreferenceKey());
        if (isAvailable() && preference != null && preference.isVisible()) {
            this.mPreferenceList.add(preference);
            updatePreference(preference, 0);
            int imeiPreferenceOrder = preference.getOrder();
            for (int simSlotNumber = 1; simSlotNumber < this.mTelephonyManager.getPhoneCount(); simSlotNumber++) {
                Preference multiSimPreference = createNewPreference(screen.getContext());
                multiSimPreference.setOrder(imeiPreferenceOrder + simSlotNumber);
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(KEY_IMEI_INFO);
                stringBuilder.append(simSlotNumber);
                multiSimPreference.setKey(stringBuilder.toString());
                screen.addPreference(multiSimPreference);
                this.mPreferenceList.add(multiSimPreference);
                updatePreference(multiSimPreference, simSlotNumber);
            }
        }
    }

    public boolean handlePreferenceTreeClick(Preference preference) {
        int simSlot = this.mPreferenceList.indexOf(preference);
        if (simSlot == -1) {
            return false;
        }
        ImeiInfoDialogFragment.show(this.mFragment, simSlot, preference.getTitle().toString());
        return true;
    }

    private void updatePreference(Preference preference, int simSlot) {
        if (this.mTelephonyManager.getCurrentPhoneTypeForSlot(simSlot) == 2) {
            preference.setTitle(getTitleForCdmaPhone(simSlot));
            preference.setSummary(getMeid());
            return;
        }
        preference.setTitle(getTitleForGsmPhone(simSlot));
        preference.setSummary(this.mTelephonyManager.getImei(simSlot));
    }

    private CharSequence getTitleForGsmPhone(int simSlot) {
        if (!this.mIsMultiSim) {
            return this.mContext.getString(R.string.status_imei);
        }
        return this.mContext.getString(R.string.imei_multi_sim, new Object[]{Integer.valueOf(simSlot + 1)});
    }

    private CharSequence getTitleForCdmaPhone(int simSlot) {
        if (!this.mIsMultiSim) {
            return this.mContext.getString(R.string.status_meid_number);
        }
        return this.mContext.getString(R.string.meid_multi_sim, new Object[]{Integer.valueOf(simSlot + 1)});
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getMeid() {
        return this.mTelephonyManager.getMeid(0);
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public Preference createNewPreference(Context context) {
        return new Preference(context);
    }
}
