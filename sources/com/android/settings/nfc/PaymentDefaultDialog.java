package com.android.settings.nfc;

import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.android.settings.R;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;

public final class PaymentDefaultDialog extends AlertActivity implements OnClickListener {
    private static final int PAYMENT_APP_MAX_CAPTION_LENGTH = 40;
    public static final String TAG = "PaymentDefaultDialog";
    private PaymentBackend mBackend;
    private ComponentName mNewDefault;

    /* Access modifiers changed, original: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mBackend = new PaymentBackend(this);
        Intent intent = getIntent();
        ComponentName component = (ComponentName) intent.getParcelableExtra("component");
        String category = intent.getStringExtra("category");
        setResult(0);
        if (!buildDialog(component, category)) {
            finish();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            this.mBackend.setDefaultPaymentApp(this.mNewDefault);
            setResult(-1);
        }
    }

    private boolean buildDialog(ComponentName component, String category) {
        if (component == null || category == null) {
            Log.e(TAG, "Component or category are null");
            return false;
        } else if ("payment".equals(category)) {
            PaymentAppInfo requestedPaymentApp = null;
            PaymentAppInfo defaultPaymentApp = null;
            for (PaymentAppInfo service : this.mBackend.getPaymentAppInfos()) {
                if (component.equals(service.componentName)) {
                    requestedPaymentApp = service;
                }
                if (service.isDefault) {
                    defaultPaymentApp = service;
                }
            }
            if (requestedPaymentApp == null) {
                String str = TAG;
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("Component ");
                stringBuilder.append(component);
                stringBuilder.append(" is not a registered payment service.");
                Log.e(str, stringBuilder.toString());
                return false;
            }
            ComponentName defaultComponent = this.mBackend.getDefaultPaymentApp();
            if (defaultComponent == null || !defaultComponent.equals(component)) {
                this.mNewDefault = component;
                AlertParams p = this.mAlertParams;
                p.mTitle = getString(R.string.nfc_payment_set_default_label);
                if (defaultPaymentApp == null) {
                    p.mMessage = String.format(getString(R.string.nfc_payment_set_default), new Object[]{sanitizePaymentAppCaption(requestedPaymentApp.label.toString())});
                } else {
                    p.mMessage = String.format(getString(R.string.nfc_payment_set_default_instead_of), new Object[]{sanitizePaymentAppCaption(requestedPaymentApp.label.toString()), sanitizePaymentAppCaption(defaultPaymentApp.label.toString())});
                }
                p.mPositiveButtonText = getString(R.string.yes);
                p.mNegativeButtonText = getString(R.string.no);
                p.mPositiveButtonListener = this;
                p.mNegativeButtonListener = this;
                setupAlert();
                return true;
            }
            String str2 = TAG;
            StringBuilder stringBuilder2 = new StringBuilder();
            stringBuilder2.append("Component ");
            stringBuilder2.append(component);
            stringBuilder2.append(" is already default.");
            Log.e(str2, stringBuilder2.toString());
            return false;
        } else {
            String str3 = TAG;
            StringBuilder stringBuilder3 = new StringBuilder();
            stringBuilder3.append("Don't support defaults for category ");
            stringBuilder3.append(category);
            Log.e(str3, stringBuilder3.toString());
            return false;
        }
    }

    private String sanitizePaymentAppCaption(String input) {
        String sanitizedString = input.replace(10, ' ').replace(13, ' ').trim();
        if (sanitizedString.length() > 40) {
            return sanitizedString.substring(0, 40);
        }
        return sanitizedString;
    }
}
