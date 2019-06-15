package com.android.settings.deviceinfo.imei;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.TtsSpan.DigitsBuilder;
import com.android.settings.R;
import java.util.List;

public class ImeiInfoDialogController {
    @VisibleForTesting
    static final int ID_CDMA_SETTINGS = 2131362022;
    @VisibleForTesting
    static final int ID_GSM_SETTINGS = 2131362344;
    @VisibleForTesting
    static final int ID_IMEI_SV_VALUE = 2131362425;
    @VisibleForTesting
    static final int ID_IMEI_VALUE = 2131362426;
    @VisibleForTesting
    static final int ID_MEID_NUMBER_VALUE = 2131362620;
    private static final int ID_MIN_NUMBER_LABEL = 2131362635;
    @VisibleForTesting
    static final int ID_MIN_NUMBER_VALUE = 2131362636;
    @VisibleForTesting
    static final int ID_PRL_VERSION_VALUE = 2131362910;
    private final ImeiInfoDialogFragment mDialog;
    private final int mSlotId;
    private final SubscriptionInfo mSubscriptionInfo;
    private final TelephonyManager mTelephonyManager;

    private static CharSequence getTextAsDigits(CharSequence text) {
        if (!TextUtils.isDigitsOnly(text)) {
            return text;
        }
        Spannable spannable = new SpannableStringBuilder(text);
        spannable.setSpan(new DigitsBuilder(text.toString()).build(), 0, spannable.length(), 33);
        return spannable;
    }

    public ImeiInfoDialogController(@NonNull ImeiInfoDialogFragment dialog, int slotId) {
        this.mDialog = dialog;
        this.mSlotId = slotId;
        Context context = dialog.getContext();
        this.mTelephonyManager = (TelephonyManager) context.getSystemService("phone");
        this.mSubscriptionInfo = getSubscriptionInfo(context, slotId);
    }

    public void populateImeiInfo() {
        if (this.mTelephonyManager.getCurrentPhoneTypeForSlot(this.mSlotId) == 2) {
            updateDialogForCdmaPhone();
        } else {
            updateDialogForGsmPhone();
        }
    }

    private void updateDialogForCdmaPhone() {
        CharSequence cdmaMin;
        Resources res = this.mDialog.getContext().getResources();
        this.mDialog.setText(R.id.meid_number_value, getMeid());
        ImeiInfoDialogFragment imeiInfoDialogFragment = this.mDialog;
        if (this.mSubscriptionInfo != null) {
            cdmaMin = this.mTelephonyManager.getCdmaMin(this.mSubscriptionInfo.getSubscriptionId());
        } else {
            cdmaMin = "";
        }
        imeiInfoDialogFragment.setText(R.id.min_number_value, cdmaMin);
        if (res.getBoolean(R.bool.config_msid_enable)) {
            this.mDialog.setText(R.id.min_number_label, res.getString(R.string.status_msid_number));
        }
        this.mDialog.setText(R.id.prl_version_value, getCdmaPrlVersion());
        if (this.mSubscriptionInfo == null || !isCdmaLteEnabled()) {
            this.mDialog.removeViewFromScreen(R.id.gsm_settings);
            return;
        }
        this.mDialog.setText(R.id.imei_value, getTextAsDigits(this.mTelephonyManager.getImei(this.mSlotId)));
        this.mDialog.setText(R.id.imei_sv_value, getTextAsDigits(this.mTelephonyManager.getDeviceSoftwareVersion(this.mSlotId)));
    }

    private void updateDialogForGsmPhone() {
        this.mDialog.setText(R.id.imei_value, getTextAsDigits(this.mTelephonyManager.getImei(this.mSlotId)));
        this.mDialog.setText(R.id.imei_sv_value, getTextAsDigits(this.mTelephonyManager.getDeviceSoftwareVersion(this.mSlotId)));
        this.mDialog.removeViewFromScreen(R.id.cdma_settings);
    }

    private SubscriptionInfo getSubscriptionInfo(Context context, int slotId) {
        List<SubscriptionInfo> subscriptionInfoList = SubscriptionManager.from(context).getActiveSubscriptionInfoList();
        if (subscriptionInfoList == null) {
            return null;
        }
        for (SubscriptionInfo info : subscriptionInfoList) {
            if (slotId == info.getSimSlotIndex()) {
                return info;
            }
        }
        return null;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getCdmaPrlVersion() {
        return this.mTelephonyManager.getCdmaPrlVersion();
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public boolean isCdmaLteEnabled() {
        return this.mTelephonyManager.getLteOnCdmaMode(this.mSubscriptionInfo.getSubscriptionId()) == 1;
    }

    /* Access modifiers changed, original: 0000 */
    @VisibleForTesting
    public String getMeid() {
        return this.mTelephonyManager.getMeid(0);
    }
}
